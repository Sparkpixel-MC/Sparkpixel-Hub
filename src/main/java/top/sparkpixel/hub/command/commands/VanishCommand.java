package top.sparkpixel.hub.command.commands;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.Permissions;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.module.modules.player.PlayerVanish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand {

	private final SparkpixelHubPlugin plugin;

	public VanishCommand(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(
			aliases = {"vanish"},
			desc = "Disappear into thin air!"
	)
	public void vanish(final CommandContext args, final CommandSender sender) throws CommandException {

		if (!sender.hasPermission(Permissions.COMMAND_VANISH.getPermission())) {
			Messages.NO_PERMISSION.send(sender);
			return;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("Console cannot set the spawn location.");
			return;
		}

		Player player = (Player) sender;
		PlayerVanish vanishModule = ((PlayerVanish) plugin.getModuleManager().getModule(ModuleType.VANISH));
		vanishModule.toggleVanish(player);
	}

}
