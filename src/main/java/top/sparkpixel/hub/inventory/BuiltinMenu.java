package top.sparkpixel.hub.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import top.sparkpixel.hub.SparkpixelHubPlugin;

import java.util.function.Function;

public class BuiltinMenu extends AbstractInventory {

	private final Function<Player, Inventory> builder;

	public BuiltinMenu(SparkpixelHubPlugin plugin, Function<Player, Inventory> builder) {
		super(plugin);
		this.builder = builder;
	}

	@Override
	public void onEnable() {
	}

	@Override
	protected Inventory build(Player player) {
		return builder.apply(player);
	}
}
