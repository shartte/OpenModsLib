package openmods.network;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

/**
 * Handles incoming {@link openmods.network.EventMessageAdapter} messages from either client or server.
 * This class handles both cases.
 */
public class EventMessageHandler implements IMessageHandler<EventMessageAdapter, EventMessageAdapter> {

  @Override
  public EventMessageAdapter onMessage(EventMessageAdapter message, MessageContext ctx) {

    EventPacket event = message.event;

    if (event != null) {
      // TODO This is very questionable architecture. Messages handled on the client shouldn't need this context...
      if (ctx.side == Side.SERVER) {
        event.player = ctx.getServerHandler().playerEntity;
      } else {
        event.player = Minecraft.getMinecraft().thePlayer;
      }

      MinecraftForge.EVENT_BUS.post(event);
    }

    return null;
  }

}
