package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleCommandAction implements Action {

	@Override
	public String getIdentifier() {
		return "CONSOLE";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), data);
	}
}
