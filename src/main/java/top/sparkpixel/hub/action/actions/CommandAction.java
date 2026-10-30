package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import org.bukkit.entity.Player;

public class CommandAction implements Action {

	@Override
	public String getIdentifier() {
		return "COMMAND";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		player.chat(data.contains("/") ? data : "/" + data);
	}
}
