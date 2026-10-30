package top.sparkpixel.hub.module.modules.announcement;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.text.Sounds;
import top.sparkpixel.hub.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AutoBroadcast extends Module implements Runnable {

	private final List<Announcement> announcements = new ArrayList<>();
	private int broadcastTask = 0;
	private int count = 0;
	private int requiredPlayers = 0;

	private Sound defaultSound;
	private float defaultVolume = 1.0f;
	private float defaultPitch = 1.0f;

	public AutoBroadcast(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.ANNOUNCEMENTS);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		announcements.clear();

		defaultSound = config.getBoolean("announcements.sound.enabled", true)
				? Sounds.resolve(config.getString("announcements.sound.key",
				config.getString("announcements.sound.value", "minecraft:block.note_block.pling")), Sound.BLOCK_NOTE_BLOCK_PLING)
				: null;
		defaultVolume = (float) config.getDouble("announcements.sound.volume", 1.0);
		defaultPitch = (float) config.getDouble("announcements.sound.pitch", 1.0);

		ConfigurationSection list = config.getConfigurationSection("announcements.list");
		if (list == null) list = config.getConfigurationSection("announcements.announcements");
		if (list != null) {
			for (String key : list.getKeys(false)) {
				ConfigurationSection entry = list.getConfigurationSection(key);
				List<String> lines;
				Sound sound = defaultSound;
				float volume = defaultVolume;
				float pitch = defaultPitch;

				if (entry != null && entry.isList("lines")) {
					lines = entry.getStringList("lines");
					ConfigurationSection soundSection = entry.getConfigurationSection("sound");
					if (soundSection != null && soundSection.getBoolean("enabled", true)) {
						sound = Sounds.resolve(soundSection.getString("key", "minecraft:block.note_block.pling"), Sound.BLOCK_NOTE_BLOCK_PLING);
						volume = (float) soundSection.getDouble("volume", defaultVolume);
						pitch = (float) soundSection.getDouble("pitch", defaultPitch);
					}
				} else {
					lines = list.getStringList(key);
				}
				if (!lines.isEmpty()) announcements.add(new Announcement(lines, sound, volume, pitch));
			}
		}

		requiredPlayers = config.getInt("announcements.required-players",
				config.getInt("announcements.required_players", 0));

		if (!announcements.isEmpty()) {
			broadcastTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), this,
					60L, Math.max(1, config.getInt("announcements.delay", 60)) * 20L);
		}
	}

	@Override
	public void onDisable() {
		if (broadcastTask != 0) Bukkit.getScheduler().cancelTask(broadcastTask);
	}

	@Override
	public void run() {
		if (announcements.isEmpty() || Bukkit.getOnlinePlayers().size() < requiredPlayers) return;
		Announcement announcement = announcements.get(count % announcements.size());

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (inDisabledWorld(player.getLocation())) continue;
			for (String line : announcement.lines()) {
				Text.send(player, line);
			}
			if (announcement.sound() != null) {
				player.playSound(player.getLocation(), announcement.sound(), announcement.volume(), announcement.pitch());
			}
		}
		count = (count + 1) % announcements.size();
	}

	private record Announcement(List<String> lines, Sound sound, float volume, float pitch) {
	}
}
