package top.sparkpixel.hub.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ConfigMigrator {

	private final JavaPlugin plugin;
	private final Set<String> excludedPaths;

	public ConfigMigrator(JavaPlugin plugin) {
		this.plugin = plugin;
		this.excludedPaths = new HashSet<>();
		registerExcludedPaths();
	}

	/**
	 * User owned content is never touched while adding new default options.
	 */
	private void registerExcludedPaths() {
		excludedPaths.add("announcements");
		excludedPaths.add("custom_join_items");
		excludedPaths.add("join_events");
		excludedPaths.add("custom_commands");
		excludedPaths.add("commands");
		excludedPaths.add("command_block");
		excludedPaths.add("anti_swear");
		excludedPaths.add("join_settings");
		excludedPaths.add("pvp");
		excludedPaths.add("content");
	}

	/**
	 * Adds missing options from the bundled default file while preserving every
	 * value that already exists in the user's file.
	 */
	public boolean migrateConfig(ConfigHandler configHandler) {
		String resourcePath = configHandler.getResourcePath();
		InputStream defaultConfigStream = plugin.getResource(resourcePath);
		if (defaultConfigStream == null) {
			plugin.getLogger().warning("Could not find default configuration for: " + resourcePath);
			return false;
		}

		FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
				new InputStreamReader(defaultConfigStream));
		FileConfiguration currentConfig = configHandler.getRawConfig();

		boolean changes = addMissingEntries(defaultConfig, currentConfig, "");

		if ("modules/join.yml".equals(resourcePath)) {
			for (String removed : new String[]{"join_leave_messages", "join_events", "join_settings.firework"}) {
				if (currentConfig.contains(removed)) {
					currentConfig.set(removed, null);
					changes = true;
				}
			}
		}

		if ("data.yml".equals(resourcePath) && currentConfig.contains("chat_locked")) {
			currentConfig.set("chat_locked", null);
			changes = true;
		}

		if ("config.yml".equals(resourcePath)) {
			// Removed features: scoreboard, tablist, chat management and join messages.
			for (String removed : new String[]{"scoreboard", "check-updates", "tablist", "join_leave_messages", "join_events", "anti_swear", "command_block", "pvp_mode"}) {
				if (currentConfig.contains(removed)) {
					currentConfig.set(removed, null);
					changes = true;
				}
			}

			String currentVersion = currentConfig.getString("version", "0.0.0");
			String pluginVersion = plugin.getPluginMeta().getVersion();
			if (!pluginVersion.equals(currentVersion)) {
				currentConfig.set("version", pluginVersion);
				changes = true;
			}
		}

		if (changes) {
			try {
				configHandler.save();
				plugin.getLogger().info("Updated configuration file: " + resourcePath);
			} catch (Exception e) {
				plugin.getLogger().log(Level.SEVERE, "Could not save updated configuration for: " + resourcePath, e);
			}
		}

		return changes;
	}

	private boolean addMissingEntries(FileConfiguration defaultConfig, FileConfiguration currentConfig, String path) {
		boolean changes = false;

		for (String excludedPath : excludedPaths) {
			if (path.equals(excludedPath) || path.startsWith(excludedPath + ".")) {
				return false;
			}
		}

		if (!path.isEmpty() && !defaultConfig.isConfigurationSection(path)) return false;

		Set<String> defaultKeys = path.isEmpty()
				? defaultConfig.getKeys(false)
				: defaultConfig.getConfigurationSection(path).getKeys(false);

		for (String key : defaultKeys) {
			String fullPath = path.isEmpty() ? key : path + "." + key;

			boolean shouldSkip = false;
			for (String excludedPath : excludedPaths) {
				if (fullPath.equals(excludedPath) || fullPath.startsWith(excludedPath + ".")) {
					shouldSkip = true;
					break;
				}
			}
			if (shouldSkip) continue;

			if (!currentConfig.contains(fullPath)) {
				currentConfig.set(fullPath, defaultConfig.get(fullPath));
				changes = true;
				plugin.getLogger().info("Added missing config option: " + fullPath);
			} else if (defaultConfig.isConfigurationSection(fullPath)) {
				changes |= addMissingEntries(defaultConfig, currentConfig, fullPath);
			}
		}

		return changes;
	}
}