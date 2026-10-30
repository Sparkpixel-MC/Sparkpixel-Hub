package top.sparkpixel.hub.text;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

public final class Sounds {

	private Sounds() {
	}

	public static Sound resolve(String key, Sound fallback) {
		if (key == null || key.isBlank()) return fallback;
		try {
			Sound sound = Registry.SOUNDS.get(NamespacedKey.fromString(key));
			return sound != null ? sound : fallback;
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
