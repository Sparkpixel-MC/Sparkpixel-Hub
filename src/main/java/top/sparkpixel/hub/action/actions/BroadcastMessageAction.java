package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import top.sparkpixel.hub.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastMessageAction implements Action {

	@Override
	public String getIdentifier() {
		return "BROADCAST";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			Text.send(p, data);
		}
	}
}
