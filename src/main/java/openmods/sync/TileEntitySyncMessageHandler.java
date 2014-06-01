package openmods.sync;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * This handler is needed due to the strictness of FML's typing for registerMessage.
 */
public class TileEntitySyncMessageHandler extends AbstractSyncMessageHandler implements IMessageHandler<TileEntitySyncMessage, TileEntitySyncMessage> {

  @Override
  public TileEntitySyncMessage onMessage(TileEntitySyncMessage message, MessageContext ctx) {
    super.onMessage(message, ctx);
    return null;
  }

}
