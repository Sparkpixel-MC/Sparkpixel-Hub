package top.sparkpixel.hub.module.modules.player;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.command.commands.FlyCommand;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener extends Module {

	private boolean spawnHeal;
	private boolean extinguish;

	public PlayerListener(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.PLAYER_LISTENER);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		spawnHeal = config.getBoolean("join_settings.heal", false);
		extinguish = config.getBoolean("join_settings.extinguish", false);
	}

	@Override
	public void onDisable() {
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (inDisabledWorld(player.getLocation())) return;

		if (spawnHeal) {
			player.setFoodLevel(20);
			player.setHealth(player.getMaxHealth());
		}
		if (extinguish) player.setFireTicks(0);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		FlyCommand.allowPlayerFly.remove(player.getUniqueId());
		player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if (inDisabledWorld(player.getLocation()))
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
	}
}