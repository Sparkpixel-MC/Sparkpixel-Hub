package top.sparkpixel.hub.command.commands;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.Permissions;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.module.modules.world.LobbySpawn;
import top.sparkpixel.hub.text.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand {

	private final SparkpixelHubPlugin plugin;

	public SetLobbyCommand(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(
			aliases = {"setlobby"},
			desc = "Set the lobby location"
	)
	public void setlobby(final CommandContext args, final CommandSender sender) throws CommandException {

		if (!sender.hasPermission(Permissions.COMMAND_SET_LOBBY.getPermission())) {
			Messages.NO_PERMISSION.send(sender);
			return;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("Console cannot set the spawn location.");
			return;
		}

		Player player = (Player) sender;
		if (plugin.getModuleManager().getDisabledWorlds().contains(player.getWorld().getName())) {
			sender.sendMessage(Text.parse("&cYou cannot set the lobby location in a disabled world."));
			return;
		}

		LobbySpawn lobbyModule = ((LobbySpawn) plugin.getModuleManager().getModule(ModuleType.LOBBY));
		lobbyModule.setLocation(player.getLocation());
		Messages.SET_LOBBY.send(sender);

	}

}
