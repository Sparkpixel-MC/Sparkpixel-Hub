package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import top.sparkpixel.hub.text.Text;
import org.bukkit.entity.Player;

public class ActionbarAction implements Action {

	@Override
	public String getIdentifier() {
		return "ACTIONBAR";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		player.sendActionBar(Text.parse(data));
	}
}