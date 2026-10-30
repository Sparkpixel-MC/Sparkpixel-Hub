package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

public class SoundAction implements Action {

	@Override
	public String getIdentifier() {
		return "SOUND";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		try {
			player.playSound(player.getLocation(), Registry.SOUNDS.getOrThrow(NamespacedKey.minecraft(data.toLowerCase().replaceFirst("^_", ".").replaceFirst("_$", ".").replaceAll("_(?=.*_)", "."))), 1L, 1L);
		} catch (Exception ex) {
			Bukkit.getLogger().warning("[Sparkpixel-Hub Action] Invalid sound name: " + data.toUpperCase());
		}
	}
}