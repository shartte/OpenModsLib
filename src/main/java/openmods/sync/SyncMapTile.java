package openmods.sync;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import openmods.network.PacketHandler;

import com.google.common.collect.ImmutableSet;

public class SyncMapTile<H extends TileEntity & ISyncHandler> extends SyncMap<H> {

	public SyncMapTile(H handler) {
		super(handler);
	}

	@Override
	protected SyncMap.HandlerType getHandlerType() {
		return HandlerType.TILE_ENTITY;
	}

	@Override
	protected Set<EntityPlayer> getPlayersWatching() {
		if (handler.getWorldObj() instanceof WorldServer) { return PacketHandler.getPlayersWatchingBlock((WorldServer)handler.getWorldObj(), handler.xCoord, handler.zCoord); }
		return ImmutableSet.of();
	}

	@Override
	protected World getWorld() {
		return handler.getWorldObj();
	}

	@Override
	protected boolean isInvalid() {
		return handler.isInvalid();
	}
}
