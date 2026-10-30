package top.sparkpixel.hub.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem {

	private final ItemStack itemStack;
	private final List<ClickAction> clickActions = new ArrayList<>();
	private String permission;

	public InventoryItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public InventoryItem addClickAction(ClickAction clickAction) {
		this.clickActions.add(clickAction);
		return this;
	}

	public InventoryItem permission(String permission) {
		this.permission = permission;
		return this;
	}

	public String getPermission() {
		return permission;
	}

	public List<ClickAction> getClickActions() {
		return clickActions;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}
}
