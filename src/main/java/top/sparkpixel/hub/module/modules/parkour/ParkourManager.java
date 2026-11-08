package top.sparkpixel.hub.module.modules.parkour;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.text.Text;
import top.sparkpixel.hub.utility.Locations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParkourManager extends Module {

	private final Map<UUID, Run> runs = new HashMap<>();
	private Location start;
	private Location finish;
	private Location exit;
	private double radius = 2.0;
	private double minY;
	private int rewardCoins;
	private int rewardXp;
	private final List<Location> checkpoints = new ArrayList<>();

	public ParkourManager(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.PARKOUR);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		start = Locations.parse(config.getString("parkour.start"));
		finish = Locations.parse(config.getString("parkour.finish"));
		exit = Locations.parse(config.getString("parkour.exit"));
		radius = config.getDouble("parkour.radius", 2.0);
		minY = config.getDouble("parkour.min-y", 0);
		rewardCoins = config.getInt("parkour.reward-coins", 100);
		rewardXp = config.getInt("parkour.reward-xp", 50);
		checkpoints.clear();
		for (String value : config.getStringList("parkour.checkpoints")) {
			Location location = Locations.parse(value);
			if (location != null) checkpoints.add(location);
		}
	}

	@Override
	public void onDisable() {
		runs.clear();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Run run = runs.get(player.getUniqueId());
		if (run == null || event.getTo() == null) return;

		if (event.getTo().getY() < minY) {
			Location checkpoint = run.checkpoint == null ? start : run.checkpoint;
			if (checkpoint != null) player.teleport(checkpoint);
			Text.actionBar(player, "<red>你掉出了赛道，已回到检查点。");
			return;
		}

		for (Location checkpoint : checkpoints) {
			if (checkpoint.getWorld() != null && checkpoint.getWorld().equals(player.getWorld())
					&& checkpoint.distanceSquared(player.getLocation()) <= radius * radius) {
				run.checkpoint = checkpoint;
			}
		}

		if (finish != null && finish.getWorld() != null && finish.getWorld().equals(player.getWorld())
				&& finish.distanceSquared(player.getLocation()) <= radius * radius) {
			finish(player, run);
		}
	}

	private void finish(Player player, Run run) {
		runs.remove(player.getUniqueId());
		long elapsed = System.currentTimeMillis() - run.startedAt;
		var profile = getPlugin().getProfileManager().get(player);
		profile.setParkourRuns(profile.getParkourRuns() + 1);
		boolean best = profile.getParkourBest() == 0 || elapsed < profile.getParkourBest();
		if (best) profile.setParkourBest(elapsed);
		profile.addCoins(rewardCoins);
		profile.addXp(rewardXp);
		getPlugin().getProfileManager().save(profile);

		Text.send(player, "<gradient:#57F287:#00AAFF><bold>跑酷完成!</bold></gradient> <gray>用时 <white>"
				+ format(elapsed) + "</white> " + (best ? "<green>新纪录!" : ""));
		Text.send(player, "<gray>奖励: <gold>" + rewardCoins + " 金币</gold>, <aqua>" + rewardXp + " 经验</aqua>");
		if (exit != null) player.teleport(exit);
		if (getPlugin().getModuleManager().getModule(ModuleType.ENGAGEMENT) != null) {
			((top.sparkpixel.hub.module.modules.engagement.EngagementManager)
					getPlugin().getModuleManager().getModule(ModuleType.ENGAGEMENT)).progress(player, "PARKOUR_FINISH", 1);
		}
	}

	public boolean start(Player player) {
		if (start == null || finish == null) {
			Messages.PARKOUR_NOT_CONFIGURED.send(player);
			return false;
		}
		runs.put(player.getUniqueId(), new Run(System.currentTimeMillis()));
		player.teleport(start);
		Text.actionBar(player, "<green>跑酷开始！到达终点即可完成。");
		return true;
	}

	public void stop(Player player) {
		runs.remove(player.getUniqueId());
		Text.actionBar(player, "<yellow>已退出跑酷。");
	}

	public boolean isRunning(Player player) {
		return runs.containsKey(player.getUniqueId());
	}

	public String best(Player player) {
		long best = getPlugin().getProfileManager().get(player).getParkourBest();
		return best == 0 ? "无" : format(best);
	}

	public static String format(long millis) {
		long seconds = millis / 1000;
		return String.format("%02d:%02d.%03d", seconds / 60, seconds % 60, millis % 1000);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		runs.remove(event.getPlayer().getUniqueId());
	}

	private static class Run {
		private final long startedAt;
		private Location checkpoint;

		private Run(long startedAt) {
			this.startedAt = startedAt;
		}
	}
}
