package top.sparkpixel.hub.inventory;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractInventory implements Listener {

	private final SparkpixelHubPlugin plugin;
	private final List<UUID> openInventories = new ArrayList<>();
	private boolean refreshEnabled;

	public AbstractInventory(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void setInventoryRefresh(long ticks) {
		if (ticks <= 0) return;
		plugin.getServer().getScheduler().runTaskTimer(plugin, new InventoryTask(this), ticks, ticks);
		refreshEnabled = true;
	}

	public abstract void onEnable();

	protected abstract Inventory build(Player player);

	protected SparkpixelHubPlugin getPlugin() {
		return plugin;
	}

	public Inventory refreshInventory(Player player, Inventory inventory) {
		Inventory fresh = build(player);
		if (fresh == null) return inventory;
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = fresh.getItem(i);
			inventory.setItem(i, item == null || item.getType() == Material.AIR ? null : item);
		}
		return inventory;
	}

	public void openInventory(Player player) {
		Inventory inventory = build(player);
		if (inventory == null) return;
		player.openInventory(inventory);
		if (refreshEnabled && !openInventories.contains(player.getUniqueId())) {
			openInventories.add(player.getUniqueId());
		}
	}

	public List<UUID> getOpenInventories() {
		return openInventories;
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof InventoryBuilder) {
			openInventories.remove(event.getPlayer().getUniqueId());
		}
	}
}
