package top.sparkpixel.hub.hook;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Optional SparkChess bridge. Everything is reflective so Sparkpixel-Hub does
 * not need SparkChess at compile time and still loads when SparkChess is absent.
 */
public final class SparkChessHook {

	private static final String PLUGIN = "top.sparkpixel.sparkchess.SparkChess";
	private static final String GAME_UTILS = "top.sparkpixel.sparkchess.GameUtils";
	private static Boolean available;

	private SparkChessHook() {
	}

	public static boolean isAvailable() {
		if (available == null) {
			try {
				Class.forName(PLUGIN);
				available = true;
			} catch (Throwable ignored) {
				available = false;
			}
		}
		return available;
	}

	public static boolean isInActiveGame(Player player) {
		if (player == null || !isAvailable()) return false;
		try {
			Class<?> utils = Class.forName(GAME_UTILS);
			Method method = utils.getMethod("isPlayerInActiveGame", Player.class, String.class);
			return Boolean.TRUE.equals(method.invoke(null, player, null));
		} catch (Throwable ignored) {
			return false;
		}
	}

	public static boolean isProtectedLocation(Location location) {
		if (location == null || location.getWorld() == null || !isAvailable()) return false;
		try {
			Class<?> pluginClass = Class.forName(PLUGIN);
			Object plugin = pluginClass.getMethod("getInstance").invoke(null);
			if (plugin == null) return false;

			if (checkMap(plugin, "getChessboards", "isInBoardArea", location)) return true;
			if (checkMap(plugin, "getXiangqiBoards", "isInBoardArea", location)) return true;
			if (checkMap(plugin, "getArmychessBoards", "isInBoardArea", location)) return true;

			if (nearMap(plugin, "getChessboards", location)) return true;
			if (nearMap(plugin, "getXiangqiBoards", location)) return true;
			if (nearMap(plugin, "getArmychessBoards", location)) return true;

			if (checkCurrent("top.sparkpixel.sparkchess.card.DdzGame", "isInTableArea", location)) return true;
			if (checkCurrent("top.sparkpixel.sparkchess.guandan.GuandanGame", "isInTableArea", location)) return true;
			if (checkCurrent("top.sparkpixel.sparkchess.card.UnoGame", "isInTableArea", location)) return true;

			if (nearCurrent("top.sparkpixel.sparkchess.card.DdzGame", location)) return true;
			if (nearCurrent("top.sparkpixel.sparkchess.guandan.GuandanGame", location)) return true;
			if (nearCurrent("top.sparkpixel.sparkchess.card.UnoGame", location)) return true;
			if (checkCurrent("top.sparkpixel.sparkchess.aeroplane.AeroplaneChess", "isInBoardArea", location)) return true;
			if (nearCurrent("top.sparkpixel.sparkchess.aeroplane.AeroplaneChess", location)) return true;
			if (nearStaticTables("top.sparkpixel.sparkchess.mahjong.MahjongTestTable", location)) return true;
			if (nearStaticTables("top.sparkpixel.sparkchess.hainanmahjong.HainanMahjongTable", location)) return true;
			return false;
		} catch (Throwable ignored) {
			return false;
		}
	}

	private static boolean checkMap(Object plugin, String getter, String methodName, Location location) throws Exception {
		Object value = plugin.getClass().getMethod(getter).invoke(plugin);
		if (!(value instanceof Map<?, ?> map)) return false;
		for (Object board : map.values()) {
			if (!sameWorld(board, location.getWorld())) continue;
			try {
				Method method = board.getClass().getMethod(methodName, int.class, int.class, int.class);
				if (Boolean.TRUE.equals(method.invoke(board, location.getBlockX(), location.getBlockY(), location.getBlockZ()))) {
					return true;
				}
			} catch (NoSuchMethodException ignored) {
			}
		}
		return false;
	}

	private static boolean nearMap(Object plugin, String getter, Location location) throws Exception {
		Object value = plugin.getClass().getMethod(getter).invoke(plugin);
		if (!(value instanceof Map<?, ?> map)) return false;
		for (Object board : map.values()) {
			if (nearCorner(board, location, 1, 9)) return true;
		}
		return false;
	}

	private static boolean nearStaticTables(String className, Location location) throws Exception {
		Object value = Class.forName(className).getMethod("getTables").invoke(null);
		if (!(value instanceof Map<?, ?> map)) return false;
		for (Object table : map.values()) {
			if (nearCorner(table, location, 1, 9)) return true;
		}
		return false;
	}

	private static boolean nearCurrent(String className, Location location) throws Exception {
		Object current = Class.forName(className).getMethod("current").invoke(null);
		return current != null && nearCorner(current, location, 1, 9);
	}

	private static boolean nearCorner(Object board, Location location, int margin, int size) {
		try {
			Object corner = board.getClass().getMethod("getCorner").invoke(board);
			if (corner instanceof Location c && sameWorld(board, location.getWorld())) {
				return location.getBlockX() >= c.getBlockX() - margin
						&& location.getBlockX() <= c.getBlockX() + size
						&& location.getBlockZ() >= c.getBlockZ() - margin
						&& location.getBlockZ() <= c.getBlockZ() + size
						&& location.getBlockY() >= c.getBlockY() - 1
						&& location.getBlockY() <= c.getBlockY() + 4;
			}
		} catch (Throwable ignored) {
		}
		try {
			Object c1 = board.getClass().getMethod("getCorner1").invoke(board);
			Object c2 = board.getClass().getMethod("getCorner2").invoke(board);
			if (c1 instanceof Location a && c2 instanceof Location b && sameWorld(board, location.getWorld())) {
				int minX = Math.min(a.getBlockX(), b.getBlockX());
				int maxX = Math.max(a.getBlockX(), b.getBlockX());
				int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
				int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());
				int minY = Math.min(a.getBlockY(), b.getBlockY());
				int maxY = Math.max(a.getBlockY(), b.getBlockY());
				return location.getBlockX() >= minX - margin && location.getBlockX() <= maxX + margin
						&& location.getBlockZ() >= minZ - margin && location.getBlockZ() <= maxZ + margin
						&& location.getBlockY() >= minY - 1 && location.getBlockY() <= maxY + 3;
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	private static boolean checkCurrent(String className, String methodName, Location location) throws Exception {
		Class<?> type = Class.forName(className);
		Object current = type.getMethod("current").invoke(null);
		if (current == null || !sameWorld(current, location.getWorld())) return false;
		try {
			Method method = current.getClass().getMethod(methodName, int.class, int.class, int.class);
			return Boolean.TRUE.equals(method.invoke(current, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
		} catch (NoSuchMethodException ignored) {
			return false;
		}
	}

	private static boolean sameWorld(Object board, World world) {
		try {
			Object boardWorld = board.getClass().getMethod("getWorld").invoke(board);
			if (boardWorld instanceof World w) return w.equals(world);
		} catch (Throwable ignored) {
		}
		try {
			Object worldName = board.getClass().getMethod("getWorldName").invoke(board);
			if (worldName instanceof String name) return name.equals(world.getName());
		} catch (Throwable ignored) {
		}
		return true; // coordinate-only board implementations are treated as global.
	}

	public static boolean isProtectedBlock(Block block) {
		return block != null && isProtectedLocation(block.getLocation());
	}
}