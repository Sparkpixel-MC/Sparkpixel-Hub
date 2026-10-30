package top.sparkpixel.hub.utility;

import org.bukkit.NamespacedKey;

public class NamespacedKeys {

	public enum Keys {
		PLAYER_HEAD("cgui.playerhead"),
		PVP_MODE_ITEM("pvpmode.item"),
		PVP_MODE_SWITCHER("pvpmode.switcher"),
		PVP_MODE_SWITCHER_STATE("pvpmode.switcher.state"),
		TELEPORTATION_BOW_ITEM("teleportationbow");

		private final String key;

		Keys(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public NamespacedKey get() {
			return NamespacedKey.fromString("sparkpixelhub:" + key);
		}
	}

	public static void registerKeys() {
		// Keys are created lazily; this method is kept for plugin startup ordering.
	}
}
