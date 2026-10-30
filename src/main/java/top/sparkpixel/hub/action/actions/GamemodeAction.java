package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeAction implements Action {

	@Override
	public String getIdentifier() {
		return "GAMEMODE";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		try {
			player.setGameMode(GameMode.valueOf(data.toUpperCase()));
			if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL) {
				player.getPlayer().setAllowFlight(true);
			}
		} catch (IllegalArgumentException ex) {
			Bukkit.getLogger().warning("[Sparkpixel-Hub Action] Invalid gamemode name: " + data.toUpperCase());
		}
	}
}
