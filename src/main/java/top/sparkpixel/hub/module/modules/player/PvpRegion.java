package top.sparkpixel.hub.module.modules.player;

import org.bukkit.Location;
import org.bukkit.World;

public record PvpRegion(String world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

	public static PvpRegion from(String world, double x1, double y1, double z1, double x2, double y2, double z2) {
		return new PvpRegion(world,
				Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
				Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
	}

	public boolean contains(Location location) {
		if (location == null || location.getWorld() == null) return false;
		World current = location.getWorld();
		if (!current.getName().equalsIgnoreCase(world)) return false;
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
	}

	public boolean isValid() {
		return world != null && !world.isBlank()
				&& minX < maxX && minY < maxY && minZ < maxZ;
	}
}