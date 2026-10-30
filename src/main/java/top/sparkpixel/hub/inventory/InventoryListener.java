package top.sparkpixel.hub.inventory;

import top.sparkpixel.hub.module.modules.world.BuildMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (BuildMode.getInstance().isPresent(event.getWhoClicked().getUniqueId())) return;
		if (!(event.getView().getTopInventory().getHolder() instanceof InventoryBuilder holder)) return;

		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player player)) return;

		ItemStack itemStack = event.getCurrentItem();
		if (itemStack == null || itemStack.getType() == Material.AIR) return;

		InventoryItem item = holder.getIcon(event.getRawSlot());
		if (item == null) return;
		if (item.getPermission() != null && !player.hasPermission(item.getPermission())) return;

		for (ClickAction clickAction : item.getClickActions()) {
			clickAction.execute(player);
		}
	}
}
