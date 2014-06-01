package openmods.sync;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * This handler is needed due to the strictness of FML's typing for registerMessage.
 */
public class EntitySyncMessageHandler extends AbstractSyncMessageHandler implements IMessageHandler<EntitySyncMessage, EntitySyncMessage> {

  @Override
  public EntitySyncMessage onMessage(EntitySyncMessage message, MessageContext ctx) {
    super.onMessage(message, ctx);
    return null;
  }

}
