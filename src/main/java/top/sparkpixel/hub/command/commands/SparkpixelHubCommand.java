package top.sparkpixel.hub.command.commands;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.Permissions;
import top.sparkpixel.hub.command.CommandManager;
import top.sparkpixel.hub.config.ConfigHandler;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.modules.player.PvPMode;
import top.sparkpixel.hub.module.modules.player.PvpRegion;
import top.sparkpixel.hub.module.modules.world.Trampoline;
import top.sparkpixel.hub.module.modules.drift.DriftManager;
import top.sparkpixel.hub.inventory.AbstractInventory;
import top.sparkpixel.hub.inventory.InventoryManager;
import top.sparkpixel.hub.module.ModuleManager;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.module.modules.hologram.Hologram;
import top.sparkpixel.hub.module.modules.hotbar.HotbarItem;
import top.sparkpixel.hub.module.modules.hotbar.HotbarManager;
import top.sparkpixel.hub.module.modules.world.LobbySpawn;
import top.sparkpixel.hub.text.Text;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SparkpixelHubCommand {

	private static final java.util.Map<java.util.UUID, org.bukkit.Location> PVP_POS1 = new java.util.HashMap<>();
	private static final java.util.Map<java.util.UUID, org.bukkit.Location> PVP_POS2 = new java.util.HashMap<>();

	private final SparkpixelHubPlugin plugin;

	private static String joinArgs(String[] args, int start) {
		if (args == null || start >= args.length) return "";
		return String.join(" ", java.util.Arrays.copyOfRange(args, start, args.length)).trim();
	}

	public SparkpixelHubCommand(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(
			aliases = {"sparkpixelhub", "sphub"},
			desc = "View plugin information"
	)
	public void main(final CommandContext args, final CommandSender sender) throws CommandException {

		String pluginVersion = plugin.getPluginMeta().getVersion();

		/*
		Command: help
		Description: displays help message
		*/
		if (args.argsLength() == 0 || args.getString(0).equalsIgnoreCase("help")) {

			if (!sender.hasPermission(Permissions.COMMAND_HUB_HELP.getPermission())) {
				sender.sendMessage(Text.parse("&8&l> &7Server is running &dSparkpixel-Hub &ev" + pluginVersion + " &7By &6Sparkpixel"));
				return;
			}

			sender.sendMessage("");
			sender.sendMessage(Text.parse("&d&lSparkpixel-Hub " + "&fv" + pluginVersion));
			sender.sendMessage(Text.parse("&7Author: &fSparkpixel"));
			sender.sendMessage(Text.parse("&7Contributor: &fdeathbot159"));
			sender.sendMessage("");
			sender.sendMessage(Text.parse(" &d/sparkpixelhub info &8- &7&oDisplays information about the current config"));
			sender.sendMessage(Text.parse(" &d/sparkpixelhub open <menu> &8- &7&oOpen a custom menu"));
			sender.sendMessage(Text.parse(" &d/sparkpixelhub hologram &8- &7&oView the hologram help"));
			sender.sendMessage(Text.parse(" &d/sparkpixelhub pvp &8- &7&oConfigure the PvP region"));
			sender.sendMessage(Text.parse(" &d/sparkpixelhub trampoline &8- &7&oPlace a trampoline"));
			sender.sendMessage(Text.parse(" &d/sparkpixelhub drift &8- &7&oSpawn a river drift boat"));
			sender.sendMessage("");
			sender.sendMessage(Text.parse("  &d/vanish &8- &7&oToggle vanish mode"));
			sender.sendMessage(Text.parse("  &d/fly &8- &7&oToggle flight mode"));
			sender.sendMessage(Text.parse("  &d/setlobby &8- &7&oSet the spawn location"));
			sender.sendMessage(Text.parse("  &d/lobby &8- &7&oTeleport to the spawn location"));
			sender.sendMessage(Text.parse("  &d/gamemode <gamemode> &8- &7&oSet your gamemode"));
			sender.sendMessage(Text.parse("  &d/gmc &8- &7&oGo into creative mode"));
			sender.sendMessage(Text.parse("  &d/gms &8- &7&oGo into survival mode"));
			sender.sendMessage(Text.parse("  &d/gma &8- &7&oGo into adventure mode"));
			sender.sendMessage(Text.parse("  &d/gmsp &8- &7&oGo into spectator mode"));
			sender.sendMessage(Text.parse("  &d/build &8- &7&oToggle build mode"));
			sender.sendMessage("");
			return;
		}

		/*
		Command: reload
		Description: reloads the entire plugin
		*/
		else if (args.getString(0).equalsIgnoreCase("reload")) {

			if (!sender.hasPermission(Permissions.COMMAND_HUB_RELOAD.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}

			long start = System.currentTimeMillis();
			plugin.reload();
			Messages.CONFIG_RELOAD.send(sender, "%time%", String.valueOf(System.currentTimeMillis() - start));
			int inventories = plugin.getInventoryManager().getInventories().size();
			if (inventories > 0) {
				sender.sendMessage(Text.parse("&8- &7Loaded &a" + inventories + "&7 custom menus."));
			}
		}

		/*
		Command: info
		Description: displays useful information about the configuration
		*/
		else if (args.getString(0).equalsIgnoreCase("info")) {

			if (!sender.hasPermission(Permissions.COMMAND_HUB_HELP.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}

			sender.sendMessage("");
			sender.sendMessage(Text.parse("&d&lPlugin Information"));
			sender.sendMessage("");

			Location location = ((LobbySpawn) plugin.getModuleManager().getModule(ModuleType.LOBBY)).getLocation();
			sender.sendMessage(Text.parse("&7Spawn set &8- " + (location != null ? "&ayes" : "&cno &7&o(/setlobby)")));

			sender.sendMessage("");

			ModuleManager moduleManager = plugin.getModuleManager();
			sender.sendMessage(Text.parse("&7Disabled Worlds (" + moduleManager.getDisabledWorlds().size() + ") &8- &a" + (String.join(", ", moduleManager.getDisabledWorlds()))));

			InventoryManager inventoryManager = plugin.getInventoryManager();
			sender.sendMessage(Text.parse("&7Custom menus (" + inventoryManager.getInventories().size() + ")" + " &8- &a" + (String.join(", ", inventoryManager.getInventories().keySet()))));

			HotbarManager hotbarManager = ((HotbarManager) plugin.getModuleManager().getModule(ModuleType.HOTBAR_ITEMS));
			sender.sendMessage(Text.parse("&7Hotbar items (" + hotbarManager.getHotbarItems().size() + ")" + " &8- &a" + (hotbarManager.getHotbarItems().stream().map(HotbarItem::getKey).collect(Collectors.joining(", ")))));

			CommandManager commandManager = plugin.getCommandManager();
			sender.sendMessage(Text.parse("&7Custom commands (" + commandManager.getCustomCommands().size() + ")" + " &8- &a" + (commandManager.getCustomCommands().stream().map(command -> command.getAliases().get(0)).collect(Collectors.joining(", ")))));

			sender.sendMessage("");

			sender.sendMessage(Text.parse("&7PlaceholderAPI Hook: " + (plugin.getHookManager().isHookEnabled("PLACEHOLDER_API") ? "&ayes" : "&cno")));
			sender.sendMessage(Text.parse("&7HeadDatabase Hook: " + (plugin.getHookManager().isHookEnabled("HEAD_DATABASE") ? "&ayes" : "&cno")));

			sender.sendMessage("");
		}

		/*
		Command: open
		Description: opens a custom menu
		*/
		else if (args.getString(0).equalsIgnoreCase("open")) {
			if (!(sender instanceof Player)) throw new CommandException("Console cannot open menus");

			if (!sender.hasPermission(Permissions.COMMAND_OPEN_MENUS.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}

			if (args.argsLength() == 1) {
				sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub open <menu>"));
				return;
			}

			AbstractInventory inventory = plugin.getInventoryManager().getInventory(args.getString(1));
			if (inventory == null) {
				sender.sendMessage(Text.parse("&c" + args.getString(1) + " is not a valid menu ID."));
				return;
			}
			inventory.openInventory((Player) sender);
		}

		/*
		 * PvP arena setup
		 */
		if (args.getString(0).equalsIgnoreCase("pvp")) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage(Text.parse("<red>Only players can configure the PvP region."));
				return;
			}
			if (!sender.hasPermission(Permissions.COMMAND_PVP.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}
			if (args.argsLength() == 1 || args.getString(1).equalsIgnoreCase("info")) {
				PvPMode pvp = (PvPMode) plugin.getModuleManager().getModule(ModuleType.PVP_MODE);
				PvpRegion region = pvp == null ? null : pvp.getRegion();
				sender.sendMessage(Text.parse(region == null
						? "<yellow>PvP region: <red>not set"
						: "<yellow>PvP region: <white>" + region.world() + " <gray>" + region.minX() + "," + region.minY() + "," + region.minZ() + " -> " + region.maxX() + "," + region.maxY() + "," + region.maxZ()));
				return;
			}
			if (args.getString(1).equalsIgnoreCase("pos1") || args.getString(1).equalsIgnoreCase("pos2")) {
				boolean first = args.getString(1).equalsIgnoreCase("pos1");
				if (first) PVP_POS1.put(player.getUniqueId(), player.getLocation());
				else PVP_POS2.put(player.getUniqueId(), player.getLocation());
				Text.send(player, "<green>PvP " + (first ? "pos1" : "pos2") + " set to <white>" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
				if (PVP_POS1.containsKey(player.getUniqueId()) && PVP_POS2.containsKey(player.getUniqueId())) {
					Location a = PVP_POS1.get(player.getUniqueId());
					Location b = PVP_POS2.get(player.getUniqueId());
					if (a.getWorld() == null || b.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
						Text.send(player, "<red>Both positions must be in the same world.");
						return;
					}
					PvpRegion region = PvpRegion.from(a.getWorld().getName(), a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
					ConfigHandler handler = plugin.getConfigManager().getModuleFile("pvp");
					if (handler != null) {
						FileConfiguration config = handler.getRawConfig();
						config.set("pvp.region.world", region.world());
						config.set("pvp.region.min.x", region.minX());
						config.set("pvp.region.min.y", region.minY());
						config.set("pvp.region.min.z", region.minZ());
						config.set("pvp.region.max.x", region.maxX());
						config.set("pvp.region.max.y", region.maxY());
						config.set("pvp.region.max.z", region.maxZ());
						handler.save();
					}
					PvPMode pvp = (PvPMode) plugin.getModuleManager().getModule(ModuleType.PVP_MODE);
					if (pvp != null) pvp.setRegion(region);
					Text.send(player, "<green>PvP region saved.");
				}
				return;
			}
			Text.send(player, "<yellow>Usage: /sparkpixelhub pvp <pos1|pos2|info>");
			return;
		}

		/*
		 * Trampoline
		 */
		if (args.getString(0).equalsIgnoreCase("trampoline")) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage(Text.parse("<red>Only players can place a trampoline."));
				return;
			}
			if (!sender.hasPermission(Permissions.COMMAND_TRAMPOLINE.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}
			Trampoline trampoline = (Trampoline) plugin.getModuleManager().getModule(ModuleType.TRAMPOLINE);
			if (trampoline == null) {
				Text.send(player, "<red>The trampoline module is disabled.");
				return;
			}
			trampoline.place(player);
			return;
		}

		/*
		 * Drift boat
		 */
		if (args.getString(0).equalsIgnoreCase("drift")) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage(Text.parse("<red>Only players can spawn a drift boat."));
				return;
			}
			if (!sender.hasPermission(Permissions.DRIFT_USE.getPermission())
					&& !sender.hasPermission(Permissions.COMMAND_DRIFT.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}
			DriftManager drift = (DriftManager) plugin.getModuleManager().getModule(ModuleType.DRIFT);
			if (drift == null) {
				Text.send(player, "<red>The drift module is disabled.");
				return;
			}
			drift.spawn(player);
			return;
		}

		/*
		 * Holograms
		 */
		if (args.getString(0).equalsIgnoreCase("hologram") || args.getString(0).equalsIgnoreCase("holo")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage("You cannot do this command.");
				return;
			}

			if (!sender.hasPermission(Permissions.COMMAND_HOLOGRAMS.getPermission())) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}

			if (args.argsLength() == 1) {
				sender.sendMessage("");
				sender.sendMessage(Text.parse("&d&lSparkpixel-Hub Holograms"));
				sender.sendMessage("");
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram list"));
				sender.sendMessage(Text.parse("   &7&oList all created holograms"));
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram create <id>"));
				sender.sendMessage(Text.parse("   &7&oCreate a new hologram"));
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram remove <id>"));
				sender.sendMessage(Text.parse("   &7&oDelete an existing hologram"));
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram move <id>"));
				sender.sendMessage(Text.parse("   &7&oMove the location of a hologram"));
				sender.sendMessage(Text.parse(""));
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram setline <id> <line> <text>"));
				sender.sendMessage(Text.parse("   &7&oSet the line of a specific hologram"));
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram addline <id> <text>"));
				sender.sendMessage(Text.parse("   &7&oAdd a new line to a hologram"));
				sender.sendMessage(Text.parse(" &d/" + args.getCommand() + " hologram removeline <id> <line>"));
				sender.sendMessage(Text.parse("   &7&oRemove a line from a hologram"));
				sender.sendMessage("");
				return;
			}

			Player player = (Player) sender;

			if (args.argsLength() >= 2) {
				if (args.getString(1).equalsIgnoreCase("list")) {

					if (plugin.getHologramManager().getHolograms().isEmpty()) {
						Messages.HOLOGRAMS_EMPTY.send(player);
						return;
					}

					sender.sendMessage("");
					sender.sendMessage(Text.parse("&d&lHologram List"));
					for (Hologram entry : plugin.getHologramManager().getHolograms()) {
						sender.sendMessage(Text.parse("&8- &7" + entry.getName()));
					}
					sender.sendMessage("");
				}


				if (args.getString(1).equalsIgnoreCase("create")) {
					if (args.argsLength() == 2) {
						sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub hologram create <id>"));
						return;
					}

					if (plugin.getHologramManager().hasHologram(args.getString(2))) {
						Messages.HOLOGRAMS_ALREADY_EXISTS.send(player, "%name%", args.getString(2));
						return;
					}

					Hologram holo = plugin.getHologramManager().createHologram(args.getString(2), player.getLocation());
					List<String> defaultMsg = new ArrayList<String>();
					defaultMsg.add("&7Created new Hologram called &b" + args.getString(2));
					defaultMsg.add("&7Use &b/sparkpixelhub holo &7to customise");
					holo.setLines(defaultMsg);
					Messages.HOLOGRAMS_SPAWNED.send(player, "%name%", args.getString(2));
					return;
				}


				if (args.getString(1).equalsIgnoreCase("remove") || args.getString(1).equalsIgnoreCase("delete")) {
					if (args.argsLength() == 2) {
						sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub hologram remove <id>"));
						return;
					}

					if (!plugin.getHologramManager().hasHologram(args.getString(2))) {
						Messages.HOLOGRAMS_INVALID_HOLOGRAM.send(player, "%name%", args.getString(2));
						return;
					}

					plugin.getHologramManager().deleteHologram(args.getString(2));
					Messages.HOLOGRAMS_DESPAWNED.send(player, "%name%", args.getString(2));
					return;
				}

				if (args.getString(1).equalsIgnoreCase("setline")) {
					if (args.argsLength() < 5) {
						sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub hologram setline <id> <line> <text>"));
						return;
					}

					if (!plugin.getHologramManager().hasHologram(args.getString(2))) {
						Messages.HOLOGRAMS_INVALID_HOLOGRAM.send(player, "%name%", args.getString(2));
						return;
					}

					Hologram holo = plugin.getHologramManager().getHologram(args.getString(2));
					int line = Integer.parseInt(args.getString(3));
					String text = joinArgs(args.getOriginalArgs(), 4);

					if (!holo.hasLine(line)) {
						Messages.HOLOGRAMS_INVALID_LINE.send(player, "%line%", String.valueOf(line));
						return;
					}
					holo.setLine(line, text);
					Messages.HOLOGRAMS_LINE_SET.send(player, "%line%", String.valueOf(line));
					return;
				}

				if (args.getString(1).equalsIgnoreCase("addline")) {
					if (args.argsLength() <= 3) {
						sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub hologram addline <id> <text>"));
						return;
					}

					if (!plugin.getHologramManager().hasHologram(args.getString(2))) {
						Messages.HOLOGRAMS_INVALID_HOLOGRAM.send(player, "%name%", args.getString(2));
						return;
					}

					Hologram holo = plugin.getHologramManager().getHologram(args.getString(2));
					String text = joinArgs(args.getOriginalArgs(), 3);

					holo.addLine(text);
					Messages.HOLOGRAMS_ADDED_LINE.send(player, "%name%", args.getString(2));
				}

				if (args.getString(1).equalsIgnoreCase("removeline")) {
					if (args.argsLength() != 4) {
						sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub hologram removeline <id> <line>"));
						return;
					}

					if (!plugin.getHologramManager().hasHologram(args.getString(2))) {
						Messages.HOLOGRAMS_INVALID_HOLOGRAM.send(player, "%name%", args.getString(2));
						return;
					}

					Hologram holo = plugin.getHologramManager().getHologram(args.getString(2));
					int line = Integer.parseInt(args.getString(3));

					if (!holo.hasLine(line)) {
						Messages.HOLOGRAMS_INVALID_LINE.send(player, "%line%", String.valueOf(line));
						return;
					}

					if (holo.removeLine(line) == null) {
						plugin.getHologramManager().deleteHologram(args.getString(2));
						Messages.HOLOGRAMS_REMOVED_LINE.send(player, "%name%", args.getString(2));
					}

					return;
				}

				if (args.getString(1).equalsIgnoreCase("move")) {
					if (args.argsLength() == 2) {
						sender.sendMessage(Text.parse("&cUsage: /sparkpixelhub hologram move <id>"));
						return;
					}

					if (!plugin.getHologramManager().hasHologram(args.getString(2))) {
						Messages.HOLOGRAMS_INVALID_HOLOGRAM.send(player, "%name%", args.getString(2));
						return;
					}

					Hologram holo = plugin.getHologramManager().getHologram(args.getString(2));

					holo.setLocation(player.getLocation());
					Messages.HOLOGRAMS_MOVED.send(player, "%name%", args.getString(2));
				}

			}

		}

	}
}