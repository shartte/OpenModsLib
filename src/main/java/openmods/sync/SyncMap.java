package openmods.sync;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.base.Throwables;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import openmods.Log;
import openmods.utils.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

public abstract class SyncMap<H extends ISyncHandler> {

  /**
   * We use this channel to exchange sync messages between client and server.
   */
  private static final SimpleNetworkWrapper SYNC_CHANNEL = new SimpleNetworkWrapper("OpenModsSync");

	private static final int MAX_OBJECT_NUM = 16;

  static {
    SYNC_CHANNEL.registerMessage(EntitySyncMessageHandler.class, EntitySyncMessage.class, 1, Side.CLIENT);
    SYNC_CHANNEL.registerMessage(EntitySyncMessageHandler.class, EntitySyncMessage.class, 1, Side.SERVER);
    SYNC_CHANNEL.registerMessage(TileEntitySyncMessageHandler.class, TileEntitySyncMessage.class, 2, Side.CLIENT);
    SYNC_CHANNEL.registerMessage(TileEntitySyncMessageHandler.class, TileEntitySyncMessage.class, 2, Side.SERVER);
  }

	public enum HandlerType {
		TILE_ENTITY {
			@Override
      public AbstractSyncMessage createMessage(ISyncHandler handler) {
				try {
					return TileEntitySyncMessage.fromTileEntity((TileEntity)handler);
				} catch (ClassCastException e) {
					throw new RuntimeException("Invalid usage of handler type", e);
				}
			}
		},
		ENTITY {
			@Override
			public AbstractSyncMessage createMessage(ISyncHandler handler) {
				try {
					return EntitySyncMessage.fromEntity((Entity)handler);
				} catch (ClassCastException e) {
					throw new RuntimeException("Invalid usage of handler type", e);
				}
			}
		};

    public abstract AbstractSyncMessage createMessage(ISyncHandler handler);
	}

	protected final H handler;

	private Set<Integer> knownUsers = new HashSet<Integer>();

	protected ISyncableObject[] objects = new ISyncableObject[16];
	protected HashMap<String, Integer> nameMap = new HashMap<String, Integer>();

	private int index = 0;

	protected SyncMap(H handler) {
		this.handler = handler;
	}

	public void put(String name, ISyncableObject value) {
		Preconditions.checkState(index < MAX_OBJECT_NUM, "Can't add more than %s objects", MAX_OBJECT_NUM);
		nameMap.put(name, index);
		objects[index++] = value;
	}

	public ISyncableObject get(String name) {
		if (nameMap.containsKey(name)) { return objects[nameMap.get(name)]; }
		return null;
	}

	public int size() {
		return index;
	}

	public Set<ISyncableObject> readFromMessage(AbstractSyncMessage message) throws IOException {

    DataInput dis = new DataInputStream(new ByteArrayInputStream(message.syncData));

    short mask = dis.readShort();
    Set<ISyncableObject> changes = Sets.newIdentityHashSet();
    int currentBit = 0;

    while (mask != 0) {
      if ((mask & 1) != 0) {
        final ISyncableObject object = objects[currentBit];
        if (object != null) {
          object.readFromStream(dis);
          changes.add(object);
          object.resetChangeTimer(getWorld());
        }
      }
      currentBit++;
      mask >>= 1;
    }
    return changes;
	}

	public void markAllAsClean() {
		for (int i = 0; i < index; i++) {
			if (objects[i] != null) {
				objects[i].markClean();
			}
		}
	}

	protected abstract HandlerType getHandlerType();

	protected abstract Set<EntityPlayer> getPlayersWatching();

	protected abstract World getWorld();

	protected abstract boolean isInvalid();

	public Set<ISyncableObject> sync() {
		if (isInvalid()) return ImmutableSet.of();

		Set<EntityPlayer> players = getPlayersWatching();
		Set<ISyncableObject> changes = listChanges();
		final boolean hasChanges = !changes.isEmpty();

		if (!getWorld().isRemote) {
			Packet changePacket = null;
			Packet fullPacket = null;

			try {
				for (EntityPlayer player : players) {
					if (knownUsers.contains(player.getEntityId())) {
						if (hasChanges) {
							if (changePacket == null) changePacket = createPacket(false, false);
              ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(changePacket);
						}
					} else {
						knownUsers.add(player.getEntityId());
						if (fullPacket == null) fullPacket = createPacket(true, false);
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(fullPacket);
					}
				}
			} catch (IOException e) {
				Log.warn(e, "IOError during downstream sync");
			}
		} else if (hasChanges) {
			try {
        Packet packet = createPacket(false, true);
        FMLClientHandler.instance().getClient().thePlayer.sendQueue.addToSendQueue(packet);
			} catch (IOException e) {
				Log.warn(e, "IOError during upstream sync");
			}
			knownUsers.clear();
		}

		markAllAsClean();
		return changes;
	}

	private Set<ISyncableObject> listChanges() {
		Set<ISyncableObject> changes = Sets.newIdentityHashSet();
		for (ISyncableObject obj : objects) {
			if (obj != null && obj.isDirty()) changes.add(obj);
		}

		return changes;
	}

	public Packet createPacket(boolean fullPacket, boolean toServer) throws IOException {
    AbstractSyncMessage message = createMessage(fullPacket, toServer);
		return SYNC_CHANNEL.getPacketFrom(message);
	}

  /*
    Creates a byte array that describes the changes to this object.
   */
  private byte[] createChangeData(boolean fullData) {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DataOutput dout = new DataOutputStream(bout);

    try {
      short mask = 0;
      for (int i = 0; i < index; i++) {
        final ISyncableObject object = objects[i];
        mask = ByteUtils.set(mask, i, object != null
            && (fullData || object.isDirty()));
      }
      dout.writeShort(mask);
      for (int i = 0; i < index; i++) {
        final ISyncableObject object = objects[i];
        if (object != null && (fullData || object.isDirty())) {
          object.writeToStream(dout, fullData);
        }
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    return bout.toByteArray();
  }

  private AbstractSyncMessage createMessage(boolean fullPacket, boolean toServer) {
    HandlerType type = getHandlerType();
    AbstractSyncMessage message = type.createMessage(handler);

    message.toServer = toServer;
    if (toServer) {
      message.dimensionId = getWorld().provider.dimensionId;
		}
    message.fullData = fullPacket;
    message.syncData = createChangeData(fullPacket);

    resetChangeTimers(fullPacket);
    return message;
  }

  private void resetChangeTimers(boolean fullData) {
    for (int i = 0; i < index; i++) {
      final ISyncableObject object = objects[i];
      if (object != null && (fullData || object.isDirty())) {
        object.resetChangeTimer(getWorld());
      }
    }
  }

	private static final Map<Class<? extends ISyncHandler>, List<Field>> syncedFields = Maps.newIdentityHashMap();

	private static final Comparator<Field> FIELD_NAME_COMPARATOR = new Comparator<Field>() {
		@Override
		public int compare(Field o1, Field o2) {
			// No need to worry about nulls
			return o1.getName().compareTo(o2.getName());
		}
	};

	public void writeToNBT(NBTTagCompound tag) {
		for (Entry<String, Integer> entry : nameMap.entrySet()) {
			int index = entry.getValue();
			String name = entry.getKey();
			if (objects[index] != null) {
				objects[index].writeToNBT(tag, name);
			}
		}
	}

	public void readFromNBT(NBTTagCompound tag) {
		for (Entry<String, Integer> entry : nameMap.entrySet()) {
			int index = entry.getValue();
			String name = entry.getKey();
			if (objects[index] != null) {
				objects[index].readFromNBT(tag, name);
			}
		}
	}

	private static List<Field> getSyncedFields(ISyncHandler handler) {
		Class<? extends ISyncHandler> handlerCls = handler.getClass();
		List<Field> result = syncedFields.get(handlerCls);

		if (result == null) {
			Set<Field> fields = Sets.newTreeSet(FIELD_NAME_COMPARATOR);
			for (Field field : handlerCls.getDeclaredFields()) {
				if (ISyncableObject.class.isAssignableFrom(field.getType())) {
					fields.add(field);
					field.setAccessible(true);
				}
			}
			result = ImmutableList.copyOf(fields);
			syncedFields.put(handlerCls, result);
		}

		return result;
	}

	public void autoregister() {
		for (Field field : getSyncedFields(handler)) {
			try {
				put(field.getName(), (ISyncableObject)field.get(handler));
			} catch (Exception e) {
				Log.severe(e, "Exception while registering synce field '%s'", field);
			}
		}
	}
}
