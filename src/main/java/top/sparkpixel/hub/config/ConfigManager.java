package top.sparkpixel.hub.config;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.locale.LanguageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

	/**
	 * Every feature module owns one file. The files keep their original root
	 * sections, so the rest of the plugin can keep reading the same paths while
	 * administrators get a much smaller and easier to navigate configuration.
	 */
	private static final String[] MODULE_FILES = {
			"modules/build-mode",
			"modules/static-time",
			"modules/anti-wdl",
			"modules/announcements",
			"modules/launchpad",
			"modules/double-jump",
			"modules/world-settings",
			"modules/join",
			"modules/join-effects",
			"modules/join-items",
			"modules/player-hider",
			"modules/pvp",
			"modules/teleportation-bow",
			"modules/trampoline",
			"modules/drift"
	};

	private final Map<ConfigType, ConfigHandler> configurations;
	private final List<ConfigHandler> moduleConfigs;
	private ConfigMigrator migrator;
	private LanguageManager languageManager;

	public ConfigManager() {
		configurations = new LinkedHashMap<>();
		moduleConfigs = new ArrayList<>();
	}

	public void loadFiles(SparkpixelHubPlugin plugin) {
		configurations.clear();
		moduleConfigs.clear();

		// Initialize the config migrator
		migrator = new ConfigMigrator(plugin);

		// Register all config files
		registerFile(ConfigType.SETTINGS, new ConfigHandler(plugin, "config"));
		registerFile(ConfigType.DATA, new ConfigHandler(plugin, "data"));
		registerFile(ConfigType.COMMANDS, new ConfigHandler(plugin, "commands"));

		for (String module : MODULE_FILES) {
			moduleConfigs.add(new ConfigHandler(plugin, module));
		}

		// Load all configs
		configurations.values().forEach(ConfigHandler::saveDefaultConfig);
		moduleConfigs.forEach(ConfigHandler::saveDefaultConfig);

		// Perform migrations if needed
		migrateConfigs();

		// Build the merged settings view used by all modules
		rebuildSettingsView();

		// Load languages (depends on the merged settings view)
		languageManager = new LanguageManager(plugin);
		languageManager.load();

		// Set the Messages configuration
		Messages.setLanguageManager(languageManager);
	}

	/**
	 * Migrates all configurations that need updating
	 */
	private void migrateConfigs() {
		for (Map.Entry<ConfigType, ConfigHandler> entry : configurations.entrySet()) {
			ConfigHandler handler = entry.getValue();
			if (handler.needsMigration() && migrator.migrateConfig(handler)) {
				handler.setMigrated();
			}
		}
		for (ConfigHandler handler : moduleConfigs) {
			if (handler.needsMigration() && migrator.migrateConfig(handler)) {
				handler.setMigrated();
			}
		}
	}

	private void rebuildSettingsView() {
		ConfigHandler settings = configurations.get(ConfigType.SETTINGS);
		FileConfiguration merged = new YamlConfiguration();

		// Module files provide the new layout, while config.yml is applied last so
		// an existing (pre-split) config.yml keeps winning during upgrades.
		for (ConfigHandler handler : moduleConfigs) {
			copyValues(merged, handler.getRawConfig());
		}
		copyValues(merged, settings.getRawConfig());

		settings.setRuntimeConfig(merged);
	}

	private void copyValues(FileConfiguration target, FileConfiguration source) {
		for (String key : source.getKeys(true)) {
			if (source.isConfigurationSection(key)) continue;
			target.set(key, source.get(key));
		}
	}

	public ConfigHandler getFile(ConfigType type) {
		return configurations.get(type);
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public ConfigHandler getModuleFile(String name) {
		String resourcePath = "modules/" + name + ".yml";
		for (ConfigHandler handler : moduleConfigs) {
			if (handler.getResourcePath().equals(resourcePath)) return handler;
		}
		return null;
	}

	public void reloadFiles() {
		configurations.values().forEach(ConfigHandler::reload);
		moduleConfigs.forEach(ConfigHandler::reload);

		// Perform migrations after reload in case plugin has been updated
		migrateConfigs();
		rebuildSettingsView();

		languageManager.load();
		Messages.setLanguageManager(languageManager);
	}

	public void saveFiles() {
		configurations.values().forEach(ConfigHandler::save);
		moduleConfigs.forEach(ConfigHandler::save);
	}

	public void registerFile(ConfigType type, ConfigHandler config) {
		configurations.put(type, config);
	}

	public FileConfiguration getFileConfiguration(File file) {
		return YamlConfiguration.loadConfiguration(file);
	}
}