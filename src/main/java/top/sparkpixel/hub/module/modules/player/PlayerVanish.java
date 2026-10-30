package top.sparkpixel.hub.module.modules.player;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerVanish extends Module {

	private List<UUID> vanished;

	public PlayerVanish(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.VANISH);
	}

	@Override
	public void onEnable() {
		vanished = new ArrayList<>();
	}

	@Override
	public void onDisable() {
		vanished.clear();
	}

	public void toggleVanish(Player player) {
		if (isVanished(player)) {
			vanished.remove(player.getUniqueId());
			Bukkit.getOnlinePlayers().forEach(pl -> pl.showPlayer(player));

			Messages.VANISH_DISABLE.send(player);
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);

		} else {
			vanished.add(player.getUniqueId());
			Bukkit.getOnlinePlayers().forEach(pl -> pl.hidePlayer(player));

			Messages.VANISH_ENABLE.send(player);
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 1));
		}
	}

	public boolean isVanished(Player player) {
		return vanished.contains(player.getUniqueId());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		vanished.forEach(hidden -> event.getPlayer().hidePlayer(Bukkit.getPlayer(hidden)));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		vanished.remove(player.getUniqueId());
	}
}
