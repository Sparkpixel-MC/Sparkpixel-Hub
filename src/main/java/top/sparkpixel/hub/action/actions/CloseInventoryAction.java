package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import org.bukkit.entity.Player;

public class CloseInventoryAction implements Action {

	@Override
	public String getIdentifier() {
		return "CLOSE";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		player.closeInventory();
	}
}
