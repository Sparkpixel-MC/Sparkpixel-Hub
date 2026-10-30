package top.sparkpixel.hub.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class InventoryBuilder implements InventoryHolder {

	private final Map<Integer, InventoryItem> icons = new HashMap<>();
	private final int size;
	private final Component title;

	public InventoryBuilder(int rows, Component title) {
		int clampedRows = Math.max(1, Math.min(6, rows));
		this.size = clampedRows * 9;
		this.title = title;
	}

	public void setItem(int slot, InventoryItem item) {
		if (slot >= 0 && slot < size) icons.put(slot, item);
	}

	public InventoryItem getIcon(int slot) {
		return icons.get(slot);
	}

	public Map<Integer, InventoryItem> getIcons() {
		return icons;
	}

	@Override
	public Inventory getInventory() {
		Inventory inventory = Bukkit.createInventory(this, size, title);
		for (Map.Entry<Integer, InventoryItem> entry : icons.entrySet()) {
			inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
		}
		return inventory;
	}
}
