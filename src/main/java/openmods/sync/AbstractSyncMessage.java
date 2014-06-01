package openmods.sync;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.world.World;
import openmods.utils.ByteUtils;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

/**
 * Server to Client message that transfers the state of a {@link openmods.sync.SyncMap}.
 */
public abstract class AbstractSyncMessage implements IMessage {

  public boolean toServer;

  public int dimensionId; // Only applicable when sending to server

  public byte[] syncData; // TODO This is not the most efficient way possible

  public boolean fullData;

  /**
   * Given a World, determine the sync handler from the properties found in this message.
   *
   * @param world The world the player is in currently.
   * @return Null if not found.
   */
  public abstract ISyncHandler getSyncHandler(World world);

  @Override
  public void fromBytes(ByteBuf buf) {
    int syncDataSize = buf.readInt();
    syncData = new byte[syncDataSize];
    buf.readBytes(syncData);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeBoolean(toServer);
    if (toServer) {
      buf.writeInt(dimensionId);
    }
    buf.writeInt(syncData.length);
    buf.writeBytes(syncData);
  }

}
