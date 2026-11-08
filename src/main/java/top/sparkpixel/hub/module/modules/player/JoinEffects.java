package top.sparkpixel.hub.module.modules.player;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.text.Colors;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public class JoinEffects extends Module {

	private boolean firstJoinOnly;
	private boolean soundEnabled;
	private Sound sound;
	private float volume;
	private float pitch;
	private boolean fireworkEnabled;
	private FireworkEffect.Type fireworkType;
	private int fireworkPower;
	private boolean fireworkFlicker;
	private boolean fireworkTrail;
	private List<Color> fireworkColors;
	private boolean particlesEnabled;
	private Particle particle;
	private int particleCount;
	private double particleOffset;
	private double particleSpeed;

	public JoinEffects(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.JOIN_EFFECTS);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		firstJoinOnly = config.getBoolean("join_effects.first-join-only", false);

		soundEnabled = config.getBoolean("join_effects.sound.enabled", true);
		volume = (float) config.getDouble("join_effects.sound.volume", 1.0);
		pitch = (float) config.getDouble("join_effects.sound.pitch", 1.0);
		sound = resolveSound(config.getString("join_effects.sound.key", "minecraft:entity.player.levelup"));

		fireworkEnabled = config.getBoolean("join_effects.firework.enabled", true);
		fireworkPower = config.getInt("join_effects.firework.power", 1);
		fireworkFlicker = config.getBoolean("join_effects.firework.flicker", true);
		fireworkTrail = config.getBoolean("join_effects.firework.trail", true);
		fireworkColors = new ArrayList<>();
		for (String name : config.getStringList("join_effects.firework.colors")) {
			Color color = Colors.parse(name);
			if (color != null) fireworkColors.add(color);
		}
		try {
			fireworkType = FireworkEffect.Type.valueOf(config.getString("join_effects.firework.type", "BALL_LARGE").toUpperCase());
		} catch (IllegalArgumentException e) {
			fireworkType = FireworkEffect.Type.BALL_LARGE;
		}

		particlesEnabled = config.getBoolean("join_effects.particles.enabled", true);
		particleCount = config.getInt("join_effects.particles.count", 30);
		particleOffset = config.getDouble("join_effects.particles.offset", 1.0);
		particleSpeed = config.getDouble("join_effects.particles.speed", 0.1);
		try {
			particle = Particle.valueOf(config.getString("join_effects.particles.type", "HAPPY_VILLAGER").toUpperCase());
		} catch (IllegalArgumentException e) {
			particle = Particle.HAPPY_VILLAGER;
		}
	}

	private Sound resolveSound(String key) {
		try {
			Sound resolved = Registry.SOUNDS.get(NamespacedKey.fromString(key));
			if (resolved != null) return resolved;
		} catch (Exception ignored) {
		}
		getPlugin().getLogger().warning("Invalid join effect sound: " + key + ", using entity.player.levelup");
		return Sound.ENTITY_PLAYER_LEVELUP;
	}

	@Override
	public void onDisable() {
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (inDisabledWorld(player.getLocation())) return;
		if (firstJoinOnly && player.hasPlayedBefore()) return;
		if (!getPlugin().getProfileManager().get(player).isJoinEffects()) return;

		Location location = player.getLocation();
		getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
			if (!player.isOnline()) return;
			if (soundEnabled) player.playSound(location, sound, volume, pitch);
			if (particlesEnabled) player.getWorld().spawnParticle(particle, location.clone().add(0, 1, 0),
					particleCount, particleOffset, particleOffset, particleOffset, particleSpeed);
			if (fireworkEnabled) spawnFirework(player, location);
		}, 3L);
	}

	private void spawnFirework(Player player, Location location) {
		Firework firework = player.getWorld().spawn(location, Firework.class);
		FireworkMeta meta = firework.getFireworkMeta();
		FireworkEffect.Builder builder = FireworkEffect.builder()
				.flicker(fireworkFlicker)
				.trail(fireworkTrail)
				.with(fireworkType);
		if (!fireworkColors.isEmpty()) builder.withColor(fireworkColors);
		meta.addEffect(builder.build());
		meta.setPower(fireworkPower);
		firework.setFireworkMeta(meta);
	}
}