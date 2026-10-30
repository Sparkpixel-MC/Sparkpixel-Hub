package top.sparkpixel.hub.command.commands;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.module.modules.world.LobbySpawn;
import top.sparkpixel.hub.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand {

	private final SparkpixelHubPlugin plugin;

	public LobbyCommand(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(
			aliases = {"lobby"},
			desc = "Teleport to the lobby (if set)"
	)
	public void lobby(final CommandContext args, final CommandSender sender) throws CommandException {

		if (!(sender instanceof Player)) {
			sender.sendMessage("Console cannot teleport to spawn");
			return;
		}

		Location location = ((LobbySpawn) plugin.getModuleManager().getModule(ModuleType.LOBBY)).getLocation();
		if (location == null) {
			sender.sendMessage(Text.parse("&cThe spawn location has not been set &7(/setlobby)&c."));
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> ((Player) sender).teleport(location), 3L);

	}

}
