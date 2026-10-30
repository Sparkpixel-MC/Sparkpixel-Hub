package top.sparkpixel.hub.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import top.sparkpixel.hub.SparkpixelHubPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HubTabCompleter implements TabCompleter {

	private final SparkpixelHubPlugin plugin;

	public HubTabCompleter(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		String name = command.getName().toLowerCase(Locale.ROOT);
		if (name.equals("sparkpixelhub") || name.equals("sphub") || (alias != null && alias.equalsIgnoreCase("sphub"))) {
			return completeRoot(sender, args);
		}

		if (name.equals("gamemode")) {
			if (args.length == 1) return filter(args[0], List.of("survival", "creative", "adventure", "spectator", "0", "1", "2", "3"));
			if (args.length == 2) return playerNames(args[1]);
			return List.of();
		}

		if (name.equals("gms") || name.equals("gmc") || name.equals("gma") || name.equals("gmsp")
				|| name.equals("fly") || name.equals("vanish") || name.equals("buildmode")) {
			if (args.length == 1) return playerNames(args[0]);
			return List.of();
		}

		return List.of();
	}

	private List<String> completeRoot(CommandSender sender, String[] args) {
		if (args.length == 0) return List.of();
		if (args.length == 1) {
			List<String> options = new ArrayList<>(List.of("help", "info", "open", "pvp", "trampoline", "drift", "hologram"));
			if (sender.hasPermission("sparkpixelhub.command.reload")) options.add("reload");
			return filter(args[0], options);
		}

		String sub = args[0].toLowerCase(Locale.ROOT);
		if (sub.equals("open")) {
			if (args.length == 2) return filter(args[1], new ArrayList<>(plugin.getInventoryManager().getInventories().keySet()));
			return List.of();
		}
		if (sub.equals("pvp")) {
			if (args.length == 2) return filter(args[1], List.of("pos1", "pos2", "info"));
			return List.of();
		}
		if (sub.equals("hologram") || sub.equals("holo")) {
			if (args.length == 2) return filter(args[1], List.of("list", "create", "remove", "move", "setline", "addline", "removeline"));
			if (args.length == 3) {
				if (plugin.getHologramManager() == null) return List.of();
				List<String> names = new ArrayList<>();
				plugin.getHologramManager().getHolograms().forEach(h -> names.add(h.getName()));
				return filter(args[2], names);
			}
			return List.of();
		}
		return List.of();
	}

	private List<String> playerNames(String prefix) {
		List<String> names = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
		return filter(prefix, names);
	}

	private List<String> filter(String prefix, List<String> values) {
		String lower = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
		List<String> result = new ArrayList<>();
		for (String value : values) {
			if (value.toLowerCase(Locale.ROOT).startsWith(lower)) result.add(value);
		}
		return result;
	}
}