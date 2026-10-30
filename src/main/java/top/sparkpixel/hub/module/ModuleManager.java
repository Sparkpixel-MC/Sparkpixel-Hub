package top.sparkpixel.hub.module;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.command.CustomCommandHandler;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.module.modules.announcement.AutoBroadcast;
import top.sparkpixel.hub.module.modules.drift.DriftManager;
import top.sparkpixel.hub.module.modules.hologram.HologramManager;
import top.sparkpixel.hub.module.modules.hotbar.HotbarManager;
import top.sparkpixel.hub.module.modules.player.*;
import top.sparkpixel.hub.module.modules.world.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ModuleManager {

	private SparkpixelHubPlugin plugin;
	private List<String> disabledWorlds;
	private final Map<ModuleType, Module> modules = new HashMap<>();

	public void loadModules(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;

		if (!modules.isEmpty()) unloadModules();

		FileConfiguration config = plugin.getConfigManager().getFile(ConfigType.SETTINGS).getConfig();
		disabledWorlds = config.getStringList("disabled-worlds.worlds");

		if (config.getBoolean("disabled-worlds.invert")) {
			disabledWorlds = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
			for (String world : config.getStringList("disabled-worlds.worlds")) disabledWorlds.remove(world);
		}

		registerModule(new PvPMode(plugin), "pvp.enabled");
		if (!config.getBoolean("multiple_worlds")) {
			registerModule(new TeleportationBow(plugin), "teleportation_bow.enabled");
		} else {
			Bukkit.getLogger().log(Level.WARNING, "Multiple worlds is enabled (This deactivates the teleportation bow module)");
		}
		registerModule(new StaticTime(plugin), "static_time.enabled");
		registerModule(new AntiWorldDownloader(plugin), "anti_wdl.enabled");
		registerModule(new DoubleJump(plugin), "double_jump.enabled");
		registerModule(new Launchpad(plugin), "launchpad.enabled");
		registerModule(new AutoBroadcast(plugin), "announcements.enabled");
		registerModule(new JoinEffects(plugin), "join_effects.enabled");
		registerModule(new CustomCommandHandler(plugin));
		registerModule(new PlayerListener(plugin));
		registerModule(new HotbarManager(plugin));
		registerModule(new WorldProtect(plugin));
		registerModule(new LobbySpawn(plugin));
		registerModule(new PlayerVanish(plugin));
		registerModule(new HologramManager(plugin));
		registerModule(new Trampoline(plugin), "trampoline.enabled");
		registerModule(new DriftManager(plugin), "drift.enabled");

		for (Module module : modules.values()) {
			try {
				module.setDisabledWorlds(disabledWorlds);
				module.onEnable();
			} catch (Exception e) {
				e.printStackTrace();
				plugin.getLogger().severe("============= SPARKPIXEL HUB MODULE LOAD ERROR =============");
				plugin.getLogger().severe("There was an error loading the " + module.getModuleType() + " module");
				plugin.getLogger().severe("The plugin will now disable..");
				plugin.getLogger().severe("============= SPARKPIXEL HUB MODULE LOAD ERROR =============");
				plugin.getServer().getPluginManager().disablePlugin(plugin);
				break;
			}
		}

		plugin.getLogger().info("Loaded " + modules.size() + " plugin modules.");
	}

	public void unloadModules() {
		for (Module module : modules.values()) {
			try {
				HandlerList.unregisterAll(module);
				module.onDisable();
			} catch (Exception e) {
				e.printStackTrace();
				plugin.getLogger().severe("There was an error unloading the " + module.getModuleType().toString() + " module.");
			}
		}
		modules.clear();
	}

	public Module getModule(ModuleType type) {
		return modules.get(type);
	}

	public void registerModule(Module module) {
		registerModule(module, null);
	}

	public void registerModule(Module module, String isEnabledPath) {
		SparkpixelHubPlugin plugin = module.getPlugin();
		if (isEnabledPath != null && !plugin.getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getBoolean(isEnabledPath, false))
			return;

		plugin.getServer().getPluginManager().registerEvents(module, plugin);
		modules.put(module.getModuleType(), module);
	}

	public boolean isEnabled(ModuleType type) {
		return modules.containsKey(type);
	}

	public List<String> getDisabledWorlds() {
		return disabledWorlds;
	}
}