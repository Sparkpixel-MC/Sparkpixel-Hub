package top.sparkpixel.hub.module.modules.player;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.module.modules.world.BuildMode;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerOffHandSwap extends Module {

	public PlayerOffHandSwap(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.PLAYER_OFFHAND_LISTENER);
	}

	@Override
	public void onEnable() {
	}

	@Override
	public void onDisable() {
	}

	@EventHandler
	public void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {
		if (BuildMode.getInstance().isPresent(event.getPlayer().getUniqueId())) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (BuildMode.getInstance().isPresent(event.getWhoClicked().getUniqueId())) return;
		if (event.getRawSlot() != event.getSlot() && event.getCursor() != null && event.getSlot() == 40) {
			event.setCancelled(true);
		}
	}
}
