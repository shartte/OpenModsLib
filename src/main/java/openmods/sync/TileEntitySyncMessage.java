package openmods.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import openmods.Log;

/**
 * This message is used to sync properties of tile entities between client/server.
 * It uses the tile entities location (x,y,z) to identify which tile entity the updates
 * are meant for.
 */
public class TileEntitySyncMessage extends AbstractSyncMessage {

  private int x;
  private int y;
  private int z;

  /**
   * Creates a sync message for a specific tile entity.
   */
  public static TileEntitySyncMessage fromTileEntity(TileEntity te) {
    TileEntitySyncMessage message = new TileEntitySyncMessage();
    message.x = te.xCoord;
    message.y = te.yCoord;
    message.z = te.zCoord;
    return message;
  }

  @Override
  public ISyncHandler getSyncHandler(World world) {
    if (world.blockExists(x, y, z)) {
      TileEntity tile = world.getTileEntity(x, y, z);
      if (tile instanceof ISyncHandler)
        return (ISyncHandler)tile;
    }

    Log.warn("Invalid handler info: can't find ISyncHandler TE @ (%d,%d,%d)", x, y, z);
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    x = buf.readInt();
    y = buf.readInt();
    z = buf.readInt();
    super.fromBytes(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(x);
    buf.writeInt(y);
    buf.writeInt(z);
    super.toBytes(buf);
  }

}
