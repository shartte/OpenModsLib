package openmods.utils;

import net.minecraft.world.World;

public interface ITester<T> {
	public enum Result {
		ACCEPT,
		REJECT,
		CONTINUE;
	}

	Result test(World world, int x, int y, int z, T o);
}