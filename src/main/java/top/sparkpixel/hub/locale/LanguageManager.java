package top.sparkpixel.hub.locale;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves player-facing text from lang/<locale>.yml. The locale comes from the
 * Minecraft client (Player#locale), so an English client and a Chinese client can
 * receive completely different messages on the same server.
 */
public class LanguageManager {

	private static final String[] BUNDLED_LANGUAGES = {"en_us", "zh_cn"};

	private final SparkpixelHubPlugin plugin;
	private final Map<String, Language> languages = new LinkedHashMap<>();
	private Language defaultLanguage;
	private Language fallbackLanguage;
	private boolean enabled;
	private boolean localizeContent;

	public LanguageManager(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
	}

	public void load() {
		languages.clear();

		File directory = new File(plugin.getDataFolder(), "lang");
		if (!directory.exists() && !directory.mkdirs()) {
			plugin.getLogger().warning("Could not create language directory.");
		}

		for (String bundled : BUNDLED_LANGUAGES) {
			File file = new File(directory, bundled + ".yml");
			if (!file.exists()) {
				try {
					plugin.saveResource("lang/" + bundled + ".yml", false);
				} catch (IllegalArgumentException ignored) {
					// Resource not bundled, custom builds may remove a language.
				}
			}
		}

		File[] files = directory.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
		if (files != null) {
			for (File file : files) {
				try {
					FileConfiguration config = YamlConfiguration.loadConfiguration(file);
					String locale = normalize(config.getString("locale", file.getName().replace(".yml", "")));
					Language language = new Language(locale, config);
					languages.put(locale, language);

					for (String alias : config.getStringList("aliases")) {
						languages.putIfAbsent(normalize(alias), language);
					}
				} catch (Exception e) {
					plugin.getLogger().warning("Could not load language file " + file.getName() + ": " + e.getMessage());
				}
			}
		}

		FileConfiguration settings = plugin.getConfigManager().getFile(ConfigType.SETTINGS).getConfig();
		enabled = settings.getBoolean("language.enabled", true);
		localizeContent = settings.getBoolean("language.localize-content", true);

		String defaultLocale = normalize(settings.getString("language.default", "en_us"));
		String fallbackLocale = normalize(settings.getString("language.fallback", defaultLocale));

		defaultLanguage = resolveLocale(defaultLocale);
		fallbackLanguage = resolveLocale(fallbackLocale);

		if (defaultLanguage == null) defaultLanguage = fallbackLanguage;
		if (defaultLanguage == null) defaultLanguage = firstLanguage();
		if (fallbackLanguage == null) fallbackLanguage = defaultLanguage;
	}

	private Language firstLanguage() {
		Collection<Language> values = languages.values();
		return values.isEmpty() ? null : values.iterator().next();
	}

	private Language resolveLocale(String locale) {
		if (locale == null || locale.isEmpty()) return null;
		Language exact = languages.get(locale);
		if (exact != null) return exact;

		String languageOnly = locale.split("_")[0];
		Language language = languages.get(languageOnly);
		if (language != null) return language;

		for (Map.Entry<String, Language> entry : languages.entrySet()) {
			if (entry.getKey().startsWith(languageOnly + "_")) return entry.getValue();
		}
		return null;
	}

	public Language resolve(CommandSender sender) {
		if (!enabled || !(sender instanceof Player player)) return defaultLanguage;

		String locale = normalize(player.locale().toString());
		Language language = languages.get(locale);
		if (language != null) return language;

		language = resolveLocale(locale);
		if (language != null) return language;

		return fallbackLanguage != null ? fallbackLanguage : defaultLanguage;
	}

	public String getMessage(CommandSender sender, String path) {
		return getMessage(resolve(sender), path);
	}

	public String getDefaultMessage(String path) {
		return getMessage(defaultLanguage, path);
	}

	private String getMessage(Language language, String path) {
		if (language != null) {
			String message = language.getMessage(path);
			if (message != null) return message;
		}
		if (fallbackLanguage != null && fallbackLanguage != language) {
			String message = fallbackLanguage.getMessage(path);
			if (message != null) return message;
		}
		return null;
	}

	public Object getContent(CommandSender sender, String path) {
		if (!localizeContent) return null;

		Language language = resolve(sender);
		Object value = language == null ? null : language.getContent(path);
		if (value != null) return value;

		if (fallbackLanguage != null && fallbackLanguage != language) {
			return fallbackLanguage.getContent(path);
		}
		return null;
	}

	public String getContentString(CommandSender sender, String path, String fallback) {
		Object value = getContent(sender, path);
		return value instanceof String ? (String) value : fallback;
	}

	public List<String> getContentList(CommandSender sender, String path, List<String> fallback) {
		Object value = getContent(sender, path);
		if (!(value instanceof List<?> list)) return fallback;

		List<String> result = new ArrayList<>();
		for (Object entry : list) result.add(String.valueOf(entry));
		return result;
	}

	public List<List<String>> getContentMessages(CommandSender sender, String path) {
		Object value = getContent(sender, path);
		if (!(value instanceof List<?> list)) return null;

		List<List<String>> result = new ArrayList<>();
		for (Object entry : list) {
			if (!(entry instanceof List<?> lines)) continue;
			List<String> message = new ArrayList<>();
			for (Object line : lines) message.add(String.valueOf(line));
			result.add(message);
		}
		return result.isEmpty() ? null : result;
	}

	private static String normalize(String locale) {
		if (locale == null) return "";
		return locale.trim().toLowerCase(Locale.ROOT).replace('-', '_');
	}

	public boolean isEnabled() {
		return enabled;
	}

	public static class Language {
		private final String locale;
		private final FileConfiguration config;

		private Language(String locale, FileConfiguration config) {
			this.locale = locale;
			this.config = config;
		}

		private String getMessage(String path) {
			Object value = config.get("Messages." + path);
			if (value == null) return null;
			if (value instanceof List<?> list) {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < list.size(); i++) {
					if (i > 0) builder.append('\n');
					builder.append(list.get(i));
				}
				return builder.toString();
			}
			return value.toString();
		}

		private Object getContent(String path) {
			return config.get("content." + path);
		}

		public String getLocale() {
			return locale;
		}
	}
}