package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import top.sparkpixel.hub.inventory.AbstractInventory;
import org.bukkit.entity.Player;

public class MenuAction implements Action {

	@Override
	public String getIdentifier() {
		return "MENU";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		AbstractInventory inventory = plugin.getInventoryManager().getInventory(data);

		if (inventory != null) {
			inventory.openInventory(player);
		} else {
			plugin.getLogger().warning("[MENU] Action Failed: Menu '" + data + "' not found.");
		}
	}
}
