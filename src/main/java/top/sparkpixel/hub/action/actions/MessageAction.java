package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import top.sparkpixel.hub.text.Text;
import org.bukkit.entity.Player;

public class MessageAction implements Action {

	@Override
	public String getIdentifier() {
		return "MESSAGE";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		Text.send(player, data);
	}
}
