package top.sparkpixel.hub.module.modules.world;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class StaticTime extends Module {
	private int _time;

	public StaticTime(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.STATIC_TIME);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		_time = config.getInt("static_time.time");
		runScheduler();
	}

	private void runScheduler() {
		final SparkpixelHubPlugin plugin = SparkpixelHubPlugin.getPlugin(SparkpixelHubPlugin.class);
		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.getWorlds().forEach(world -> {
					if (plugin.getModuleManager().getDisabledWorlds().contains(world.getName())) return;
					if (world.getTime() != _time) world.setTime(_time);
				});
			}
		}.runTaskTimer(plugin, 2L, 2L);
	}

	@Override
	public void onDisable() {
	}
}
