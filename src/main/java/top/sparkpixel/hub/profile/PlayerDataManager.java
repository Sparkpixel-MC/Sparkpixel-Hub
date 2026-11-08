package top.sparkpixel.hub.profile;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import top.sparkpixel.hub.SparkpixelHubPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

	private final SparkpixelHubPlugin plugin;
	private final File directory;
	private final Map<UUID, PlayerProfile> cache = new HashMap<>();

	public PlayerDataManager(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
		this.directory = new File(plugin.getDataFolder(), "data/players");
		if (!directory.exists()) directory.mkdirs();
	}

	public PlayerProfile get(Player player) {
		return get(player.getUniqueId(), player.getName());
	}

	public PlayerProfile get(UUID uuid, String name) {
		PlayerProfile profile = cache.get(uuid);
		if (profile != null) {
			if (name != null && !name.equals(profile.getName())) profile.setName(name);
			return profile;
		}
		profile = load(uuid);
		if (profile == null) {
			profile = new PlayerProfile(uuid, name == null ? "Unknown" : name);
		}
		cache.put(uuid, profile);
		return profile;
	}

	public PlayerProfile get(UUID uuid) {
		return get(uuid, null);
	}

	private PlayerProfile load(UUID uuid) {
		File file = new File(directory, uuid + ".yml");
		if (!file.exists()) return null;
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		PlayerProfile profile = new PlayerProfile(uuid, config.getString("name", "Unknown"));
		profile.setCoins(config.getLong("coins", 0));
		profile.setXp(config.getLong("xp", 0));
		profile.setLevel(config.getInt("level", 1));
		profile.setFirstJoin(config.getLong("first-join", System.currentTimeMillis()));
		profile.setLastSeen(config.getLong("last-seen", 0));
		profile.setPlaytimeSeconds(config.getLong("playtime-seconds", 0));
		profile.setLoginStreak(config.getInt("login-streak", 0));
		profile.setLastLoginDate(config.getString("last-login-date", ""));
		profile.setJoinEffects(config.getBoolean("settings.join-effects", true));
		profile.setAnnouncements(config.getBoolean("settings.announcements", true));
		profile.setParticles(config.getBoolean("settings.particles", true));
		profile.setPlayerVisibility(config.getBoolean("settings.player-visibility", true));
		profile.setDailyDate(config.getString("daily.date", ""));
		profile.setWeeklyKey(config.getString("weekly.key", ""));
		copyMap(config.getConfigurationSection("daily.progress"), profile.getDailyProgress());
		copyMap(config.getConfigurationSection("weekly.progress"), profile.getWeeklyProgress());
		profile.getDailyClaimed().addAll(config.getStringList("daily.claimed"));
		profile.getWeeklyClaimed().addAll(config.getStringList("weekly.claimed"));
		profile.getAchievements().addAll(config.getStringList("achievements"));
		profile.getCollectibles().addAll(config.getStringList("collectibles"));
		profile.getOwnedCosmetics().addAll(config.getStringList("cosmetics.owned"));
		profile.setSelectedTrail(config.getString("cosmetics.trail", ""));
		profile.setParkourBest(config.getLong("parkour.best", 0));
		profile.setParkourRuns(config.getInt("parkour.runs", 0));
		return profile;
	}

	private void copyMap(ConfigurationSection section, Map<String, Integer> target) {
		if (section == null) return;
		for (String key : section.getKeys(false)) target.put(key, section.getInt(key));
	}

	public void save(PlayerProfile profile) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("name", profile.getName());
		config.set("coins", profile.getCoins());
		config.set("xp", profile.getXp());
		config.set("level", profile.getLevel());
		config.set("first-join", profile.getFirstJoin());
		config.set("last-seen", profile.getLastSeen());
		config.set("playtime-seconds", profile.getPlaytimeSeconds());
		config.set("login-streak", profile.getLoginStreak());
		config.set("last-login-date", profile.getLastLoginDate());
		config.set("settings.join-effects", profile.isJoinEffects());
		config.set("settings.announcements", profile.isAnnouncements());
		config.set("settings.particles", profile.isParticles());
		config.set("settings.player-visibility", profile.isPlayerVisibility());
		config.set("daily.date", profile.getDailyDate());
		config.set("weekly.key", profile.getWeeklyKey());
		profile.getDailyProgress().forEach((key, value) -> config.set("daily.progress." + key, value));
		profile.getWeeklyProgress().forEach((key, value) -> config.set("weekly.progress." + key, value));
		config.set("daily.claimed", new ArrayList<>(profile.getDailyClaimed()));
		config.set("weekly.claimed", new ArrayList<>(profile.getWeeklyClaimed()));
		config.set("achievements", new ArrayList<>(profile.getAchievements()));
		config.set("collectibles", new ArrayList<>(profile.getCollectibles()));
		config.set("cosmetics.owned", new ArrayList<>(profile.getOwnedCosmetics()));
		config.set("cosmetics.trail", profile.getSelectedTrail());
		config.set("parkour.best", profile.getParkourBest());
		config.set("parkour.runs", profile.getParkourRuns());
		try {
			config.save(new File(directory, profile.getUuid() + ".yml"));
		} catch (IOException e) {
			plugin.getLogger().warning("Could not save profile for " + profile.getName() + ": " + e.getMessage());
		}
	}

	public void saveAll() {
		for (PlayerProfile profile : cache.values()) save(profile);
	}

	public Collection<PlayerProfile> cached() {
		return cache.values();
	}

	public List<PlayerProfile> all() {
		List<PlayerProfile> profiles = new ArrayList<>();
		File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
		if (files == null) return profiles;
		for (File file : files) {
			try {
				UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
				PlayerProfile profile = cache.get(uuid);
				if (profile == null) {
					profile = load(uuid);
					if (profile != null) cache.put(uuid, profile);
				}
				if (profile != null) profiles.add(profile);
			} catch (IllegalArgumentException ignored) {
			}
		}
		return profiles;
	}
}
