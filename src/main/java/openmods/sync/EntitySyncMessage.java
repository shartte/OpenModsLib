package openmods.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import openmods.Log;

/**
 * This message is used to sync properties of tile entities between client/server.
 * It uses the {@link net.minecraft.entity.Entity#getEntityId() entity id} to identify the entity to which the
 * properties included in this message belong.
 */
public class EntitySyncMessage extends AbstractSyncMessage {

  private int entityId;

  /**
   * Creates a sync message for the given entity.
   */
  public static EntitySyncMessage fromEntity(Entity entity) {
    EntitySyncMessage message = new EntitySyncMessage();
    message.entityId = entity.getEntityId();
    return message;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    entityId = buf.readInt();
    super.fromBytes(buf);
  }

  public ISyncHandler getSyncHandler(World world) {
    Entity entity = world.getEntityByID(entityId);
    if (entity instanceof ISyncHandler)
      return (ISyncHandler)entity;

    Log.warn("Invalid handler info: can't find ISyncHandler entity id %d", entityId);
    return null;
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(entityId);
    super.toBytes(buf);
  }

}
