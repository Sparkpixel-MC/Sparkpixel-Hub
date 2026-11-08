package top.sparkpixel.hub.utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Locations {

	private Locations() {
	}

	public static Location parse(String value) {
		if (value == null) return null;
		String[] parts = value.split(",");
		if (parts.length < 4) return null;
		World world = Bukkit.getWorld(parts[0].trim());
		if (world == null) return null;
		try {
			return new Location(world,
					Double.parseDouble(parts[1].trim()),
					Double.parseDouble(parts[2].trim()),
					Double.parseDouble(parts[3].trim()));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String format(Location location) {
		return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
	}
}
