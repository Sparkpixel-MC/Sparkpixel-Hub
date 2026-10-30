package top.sparkpixel.hub.action;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import org.bukkit.entity.Player;

public interface Action {

	String getIdentifier();

	void execute(SparkpixelHubPlugin plugin, Player player, String data);

}
