package top.sparkpixel.hub.module.modules.drift;

import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.util.Vector;

/**
 * Estimates the water flow direction from the vanilla water level gradient.
 * Water levels increase downstream, so the gradient points toward the lower
 * level (the direction the water is travelling).
 */
public final class WaterFlow {

	private WaterFlow() {
	}

	public static Block findWater(Location location) {
		if (location == null || location.getWorld() == null) return null;
		Block origin = location.getBlock();
		Block found = waterAt(origin);
		if (found != null) return found;
		for (int y = 1; y <= 2; y++) {
			found = waterAt(origin.getRelative(0, -y, 0));
			if (found != null) return found;
		}
		for (int y = 1; y <= 1; y++) {
			found = waterAt(origin.getRelative(0, y, 0));
			if (found != null) return found;
		}
		return null;
	}

	private static Block waterAt(Block block) {
		return block.getType() == Material.WATER ? block : null;
	}

	public static Vector flowAt(Location location) {
		Block water = findWater(location);
		if (water == null) return null;

		int west = level(water.getRelative(BlockFace.WEST));
		int east = level(water.getRelative(BlockFace.EAST));
		int north = level(water.getRelative(BlockFace.NORTH));
		int south = level(water.getRelative(BlockFace.SOUTH));

		double x = east - west;
		double z = south - north;
		Vector vector = new Vector(x, 0, z);
		if (vector.lengthSquared() < 0.0001) return null;
		return vector.normalize();
	}

	private static int level(Block block) {
		if (block.getType() != Material.WATER) return 8;
		if (block.getBlockData() instanceof Levelled levelled) return levelled.getLevel();
		return 0;
	}
}