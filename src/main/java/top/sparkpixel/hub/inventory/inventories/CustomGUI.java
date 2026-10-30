package top.sparkpixel.hub.inventory.inventories;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.inventory.AbstractInventory;
import top.sparkpixel.hub.inventory.InventoryBuilder;
import top.sparkpixel.hub.inventory.InventoryItem;
import top.sparkpixel.hub.text.Text;
import top.sparkpixel.hub.utility.ItemStackBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomGUI extends AbstractInventory {

	private final FileConfiguration config;

	public CustomGUI(SparkpixelHubPlugin plugin, FileConfiguration config) {
		super(plugin);
		this.config = config;
	}

	@Override
	public void onEnable() {
		if (config.getConfigurationSection("items") == null && config.getConfigurationSection("entries") == null) {
			getPlugin().getLogger().warning("Menu has no items section: " + config.getString("title", "unknown"));
		}
		if (config.getBoolean("refresh.enabled", false)) {
			setInventoryRefresh(Math.max(1, config.getLong("refresh.rate", 40)));
		}
	}

	@Override
	protected Inventory build(Player player) {
		int rows = config.getInt("rows", Math.max(1, config.getInt("slots", 27) / 9));
		InventoryBuilder inventory = new InventoryBuilder(rows, Text.parse(config.getString("title", "<dark_gray>Menu")));

		ConfigurationSection items = config.getConfigurationSection("items");
		if (items == null) items = config.getConfigurationSection("entries");

		if (items != null) {
			for (String entry : items.getKeys(false)) {
				ConfigurationSection section = items.getConfigurationSection(entry);
				if (section == null) continue;

				String permission = section.getString("permission");
				if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
					continue;
				}

				try {
					ItemStack item = ItemStackBuilder.getItemStack(section, player).build();
					InventoryItem menuItem = new InventoryItem(item);
					if (permission != null && !permission.isBlank()) menuItem.permission(permission);

					List<String> actions = section.getStringList("actions");
					if (!actions.isEmpty()) {
						menuItem.addClickAction(p -> getPlugin().getActionManager().executeActions(p, actions));
					}

					setSlots(inventory, section, menuItem);
				} catch (Exception e) {
					getPlugin().getLogger().warning("Could not load menu item '" + entry + "': " + e.getMessage());
				}
			}
		}

		applyFiller(inventory);
		return inventory.getInventory();
	}

	private void setSlots(InventoryBuilder inventory, ConfigurationSection section, InventoryItem item) {
		if (section.isList("slots")) {
			for (String raw : section.getStringList("slots")) {
				try {
					inventory.setItem(Integer.parseInt(raw.trim()), item);
				} catch (NumberFormatException ignored) {
				}
			}
			return;
		}

		int slot = section.getInt("slot", -1);
		if (slot >= 0) {
			inventory.setItem(slot, item);
			return;
		}

		List<Integer> free = freeSlots(inventory);
		if (slot == -1) {
			for (int freeSlot : free) inventory.setItem(freeSlot, item);
		}
	}

	private void applyFiller(InventoryBuilder inventory) {
		ConfigurationSection filler = config.getConfigurationSection("filler");
		if (filler == null) return;
		ItemStack fillerItem;
		try {
			fillerItem = ItemStackBuilder.getItemStack(filler).build();
		} catch (Exception e) {
			return;
		}
		InventoryItem fillerIcon = new InventoryItem(fillerItem);
		for (int slot : freeSlots(inventory)) inventory.setItem(slot, fillerIcon);
	}

	private List<Integer> freeSlots(InventoryBuilder inventory) {
		int size = Math.max(9, Math.min(54, config.getInt("rows", Math.max(1, config.getInt("slots", 27) / 9)) * 9));
		Set<Integer> occupied = new HashSet<>(inventory.getIcons().keySet());
		List<Integer> free = new ArrayList<>();
		for (int slot = 0; slot < size; slot++) {
			if (!occupied.contains(slot)) free.add(slot);
		}
		return free;
	}
}
