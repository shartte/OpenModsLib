package openmods.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import openmods.Log;
import openmods.network.events.TileEntityMessageEventPacket;

public abstract class EventPacket extends Event {

	public enum CoreEventTypes implements IEventPacketType {
		TILE_ENTITY_NOTIFY {

			@Override
			public EventPacket createPacket() {
				return new TileEntityMessageEventPacket();
			}

			@Override
			public PacketDirection getDirection() {
				return PacketDirection.ANY;
			}
		};

		@Override
		public abstract EventPacket createPacket();

		@Override
		public abstract PacketDirection getDirection();

		@Override
		public boolean isCompressed() {
			return false;
		}

		@Override
		public int getId() {
			return EventIdRanges.BASE_ID_START + ordinal();
		}
	}

	public static void registerCorePackets() {
		for (IEventPacketType type : CoreEventTypes.values())
			EventPacketManager.registerType(type);
	}

	public EntityPlayer player;

	public abstract IEventPacketType getType();

	protected abstract void readFromStream(DataInput input) throws IOException;

	protected abstract void writeToStream(DataOutput output) throws IOException;

	protected void appendLogInfo(List<String> info) {}

	public void reply(EventPacket reply) {
		boolean isRemote = !(player instanceof EntityPlayerMP);
		if (!getType().getDirection().validateSend(isRemote)) {
      if (isRemote) {
        EventPacketManager.sendToServer(reply);
      } else {
        EventPacketManager.sendToPlayer((EntityPlayerMP) player, reply);
      }
		}
		else Log.warn("Invalid sent direction for packet '%s'", this);
	}

	protected boolean checkSendToClient() {
		if (!getType().getDirection().toClient) {
			Log.warn("Trying to sent message '%s' to client", this);
			return false;
		}
		return true;
	}

	protected boolean checkSendToServer() {
		if (!getType().getDirection().toServer) {
			Log.warn("Trying to sent message '%s' to server", this);
			return false;
		}
		return true;
	}

	public void sendToPlayer(EntityPlayer player) {
		if (checkSendToClient()) {
      EventPacketManager.sendToPlayer((EntityPlayerMP) player, this);
		}
	}

	public void sendToPlayers(Collection<EntityPlayer> players) {
		if (checkSendToClient()) {
      EventPacketManager.sendToPlayers(players, this);
		}
	}

	public void sendToServer() {
		if (checkSendToServer()) {
      EventPacketManager.sendToServer(this);
    }
	}
}
