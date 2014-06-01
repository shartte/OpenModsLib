package openmods.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockProperties {


	public static Block getBlock(Coord c, World world) {
		return world.getBlock(c.x, c.y, c.z);
	}

	public static int getBlockMetadata(Coord c, World world) {
		return world.getBlockMetadata(c.x, c.y, c.z);
	}

	public static boolean isAirBlock(Coord c, World world) {
		return world.isAirBlock(c.x, c.y, c.z);
	}

	public static boolean isFlower(Coord c, World world) {
		Block block = getBlock(c, world);
		return block instanceof BlockFlower;
	}

	public static boolean isBlockNormalCube(Coord c, World world) {
    return getBlock(c, world).isBlockNormalCube();
	}

	public static boolean isWood(Coord c, World world) {
		Block block = getBlock(c, world);
		return block != null && block.isWood(world, c.x, c.y, c.z);
	}

	public static boolean isLeaves(Coord c, World world) {
		Block block = getBlock(c, world);
		return block != null && block.isLeaves(world, c.x, c.y, c.z);
	}

	public static BiomeGenBase getBiomeGenBase(Coord c, World world) {
		return world.getBiomeGenForCoords(c.x, c.z);
	}
}
