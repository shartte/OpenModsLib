package openmods.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collection;
import java.util.Map;

public class EventPacketManager {

  private static final Map<Integer, IEventPacketType> TYPES = Maps.newHashMap();

  /**
   * This can be considered to be a simple channel we use to communicate between client and server.
   */
  private static final SimpleNetworkWrapper EVENT_CHANNEL = new SimpleNetworkWrapper("OpenMods");

  /**
   * Register our handler class for the message channel. Since we dispatch incoming messages to the event bus
   * anyway, we use the same handler class for both server&client.
   */
  static {
    EVENT_CHANNEL.registerMessage(EventMessageHandler.class, EventMessageAdapter.class, 0, Side.CLIENT);
    EVENT_CHANNEL.registerMessage(EventMessageHandler.class, EventMessageAdapter.class, 0, Side.SERVER);
  }

  public static void registerType(IEventPacketType type) {
    final int typeId = type.getId();
    IEventPacketType prev = TYPES.put(typeId, type);
    Preconditions.checkState(prev == null, "Trying to re-register event type id %s with %s, prev %s", typeId, type, prev);
  }

  public static IEventPacketType getTypeFromId(int id) {
    IEventPacketType type = TYPES.get(id);
    Preconditions.checkNotNull(type, "Unknown type id: %s", id);
    return type;
  }

  public static void sendToServer(EventPacket event) {
    // TODO
    EVENT_CHANNEL.sendToServer(new EventMessageAdapter(event));
  }

  public static void sendToPlayer(EntityPlayerMP player, EventPacket event) {
    // TODO
    EVENT_CHANNEL.sendTo(new EventMessageAdapter(event), player);
  }

  public static void sendToPlayers(Collection<EntityPlayer> players, EventPacket eventPacket) {
    // TODO This incurs the penalty of serializing this packet over and over again...
    EventMessageAdapter message = new EventMessageAdapter(eventPacket);
    for (EntityPlayer player : players) {
      EVENT_CHANNEL.sendTo(message, (EntityPlayerMP) player);
    }
  }

}
