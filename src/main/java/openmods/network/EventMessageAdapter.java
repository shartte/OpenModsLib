package openmods.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.entity.player.EntityPlayer;
import openmods.LibConfig;
import openmods.utils.ByteUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encapsulates our custom cross-network events in a FML IMessage.
 */
public class EventMessageAdapter implements IMessage {

  /**
   * The event packet we read or shall write to the network connection.
   * For incoming messages, the context-information (player, etc.) will be missing.
   * It's set in {@link openmods.network.EventPacketManager}
   */
  public EventPacket event;

  public EventMessageAdapter(EventPacket event) {
    this.event = event;
  }

  public EventMessageAdapter() {
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    try {
      int initialPos = buf.readerIndex(); // Save to calculate size
      ByteBufInputStream input = new ByteBufInputStream(buf);
      final IEventPacketType type = readType(input);

      final DataInput data;
      if (type.isCompressed()) {
        data = new DataInputStream(new GZIPInputStream(input));
      } else {
        data = input; // ByteBufInputStream already implements DataInput
      }

      event = type.createPacket();
      event.readFromStream(data);

      int size = buf.readerIndex() - initialPos;

      if (LibConfig.logPackets) PacketLogger.log(getClass(), size, true, createLogInfo(event, 0, 0));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {

    Preconditions.checkNotNull(event, "Cannot send event message without an event.");

    try {
      final IEventPacketType type = event.getType();

      byte[] bytes = serializeToBytes(event); // TODO: It would be more efficient to write to the output stream directly

      int initalPos = buf.writerIndex();
      ByteBufOutputStream output = new ByteBufOutputStream(buf);
      ByteUtils.writeVLI(output, type.getId());
      output.write(bytes);

      if (LibConfig.logPackets) {
        int size = buf.writerIndex() - initalPos;
        PacketLogger.log(getClass(), size, false, createLogInfo(event, 0, 0));
      }

    } catch (Exception e) {
      throw Throwables.propagate(e);
    }

  }

  private static byte[] serializeToBytes(EventPacket event) throws IOException {
    ByteArrayOutputStream payload = new ByteArrayOutputStream();

    OutputStream stream = event.getType().isCompressed() ? new GZIPOutputStream(payload) : payload;
    DataOutputStream output = new DataOutputStream(stream);
    event.writeToStream(output);
    stream.close();

    return payload.toByteArray();
  }

  private static IEventPacketType readType(InputStream bytes) {
    DataInput input = new DataInputStream(bytes);
    int id = ByteUtils.readVLI(input);
    return EventPacketManager.getTypeFromId(id);
  }


  private static List<String> createLogInfo(EventPacket event, int chunkId, int chunkLength) {
    List<String> info = Lists.newArrayList();
    info.add(String.format("%d/%d", chunkId, chunkLength));
    addTypeInfo(info, event.getType());
    addPlayerInfo(info, event.player);
    event.appendLogInfo(info);
    return info;
  }

  private static List<String> createUnfinishedLogInfo(IEventPacketType type, EntityPlayer player) {
    List<String> info = Lists.newArrayList();
    addTypeInfo(info, type);
    info.add("?/?");
    addPlayerInfo(info, player);
    info.add("non-final chunk");
    return info;
  }

  private static void addTypeInfo(List<String> info, final IEventPacketType type) {
    info.add(Integer.toString(type.getId()));
    info.add(type.toString());
    info.add(type.isCompressed()? "packed" : "raw");
  }

  private static void addPlayerInfo(List<String> info, EntityPlayer player) {
    info.add(ObjectUtils.toString(player));
  }

}
