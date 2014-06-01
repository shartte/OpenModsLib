package openmods.sync;

import com.google.common.base.Throwables;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.world.World;
import openmods.OpenMods;

import java.io.IOException;
import java.util.Set;

/**
 * Handles incoming sync messages and dispatches them to the {@link openmods.sync.ISyncHandler} identified
 * in the message.
 */
public abstract class AbstractSyncMessageHandler {

  public void onMessage(AbstractSyncMessage message, MessageContext ctx) {

    World world = getWorld(message);

    ISyncHandler handler = message.getSyncHandler(world);
    if (handler != null) {
      final Set<ISyncableObject> changes;
      try {
        changes = handler.getSyncMap().readFromMessage(message);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
      handler.onSynced(changes);
    }

  }

  private World getWorld(AbstractSyncMessage message) {
    // TODO: This is shoddy. We could register a separate server/client message handler instead
    if (message.toServer) {
      return OpenMods.proxy.getServerWorld(message.dimensionId);
    } else {
      return OpenMods.proxy.getClientWorld();
    }
  }

}
