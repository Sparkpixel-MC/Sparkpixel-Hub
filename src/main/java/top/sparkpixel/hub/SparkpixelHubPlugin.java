package top.sparkpixel.hub;

import cl.bgmp.minecraft.util.commands.exceptions.*;
import top.sparkpixel.hub.action.ActionManager;
import top.sparkpixel.hub.command.CommandManager;
import top.sparkpixel.hub.config.ConfigManager;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.config.Version;
import top.sparkpixel.hub.cooldown.CooldownManager;
import top.sparkpixel.hub.hook.HooksManager;
import top.sparkpixel.hub.inventory.InventoryManager;
import top.sparkpixel.hub.module.ModuleManager;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.profile.PlayerDataManager;
import top.sparkpixel.hub.module.modules.hologram.HologramManager;
import top.sparkpixel.hub.module.modules.world.BuildMode;
import top.sparkpixel.hub.text.Text;
import top.sparkpixel.hub.utility.NamespacedKeys;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class SparkpixelHubPlugin extends JavaPlugin {

	private ConfigManager configManager;
	private ActionManager actionManager;
	private HooksManager hooksManager;
	private CommandManager commandManager;
	private CooldownManager cooldownManager;
	private ModuleManager moduleManager;
	private InventoryManager inventoryManager;
	private Version currentVersion;
	private PlayerDataManager profileManager;

	public void onEnable() {
		long start = System.currentTimeMillis();
		getLogger().log(Level.INFO, "Sparkpixel-Hub - modern Paper lobby suite");

		NamespacedKeys.registerKeys();
		profileManager = new PlayerDataManager(this);

		hooksManager = new HooksManager(this);

		configManager = new ConfigManager();
		configManager.loadFiles(this);

		if (!getServer().getPluginManager().isPluginEnabled(this)) return;

		commandManager = new CommandManager(this);
		commandManager.reload();

		cooldownManager = new CooldownManager();

		inventoryManager = new InventoryManager();
		if (!hooksManager.isHookEnabled("HEAD_DATABASE")) inventoryManager.onEnable(this);

		moduleManager = new ModuleManager();
		moduleManager.loadModules(this);

		actionManager = new ActionManager(this);

		BuildMode.getInstance();

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		getLogger().log(Level.INFO, "Successfully loaded in " + (System.currentTimeMillis() - start) + "ms");
		currentVersion = Version.parse(getPluginMeta().getVersion());
	}

	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		if (moduleManager != null) moduleManager.unloadModules();
		if (profileManager != null) profileManager.saveAll();
		if (inventoryManager != null) inventoryManager.onDisable();
	}

	public void reload() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);

		configManager.reloadFiles();
		BuildMode.getInstance().runScheduler();

		inventoryManager.onDisable();
		inventoryManager.onEnable(this);

		getCommandManager().reload();
		moduleManager.loadModules(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
		try {
			getCommandManager().execute(cmd.getName(), args, sender);
		} catch (CommandPermissionsException e) {
			Messages.NO_PERMISSION.send(sender);
		} catch (MissingNestedCommandException e) {
			sender.sendMessage(Text.parse("<red>" + e.getUsage()));
		} catch (CommandUsageException e) {
			sender.sendMessage(Text.parse("<red>Usage: " + e.getUsage()));
		} catch (WrappedCommandException e) {
			if (e.getCause() instanceof NumberFormatException) {
				sender.sendMessage(Text.parse("<red>Number expected, string received instead."));
			} else {
				sender.sendMessage(Text.parse("<red>An internal error has occurred. See console."));
				e.printStackTrace();
			}
		} catch (CommandException e) {
			sender.sendMessage(Text.parse("<red>" + e.getMessage()));
		}
		return true;
	}

	public HologramManager getHologramManager() {
		return (HologramManager) moduleManager.getModule(ModuleType.HOLOGRAMS);
	}

	public HooksManager getHookManager() { return hooksManager; }
	public ModuleManager getModuleManager() { return moduleManager; }
	public CommandManager getCommandManager() { return commandManager; }
	public CooldownManager getCooldownManager() { return cooldownManager; }
	public InventoryManager getInventoryManager() { return inventoryManager; }
	public ConfigManager getConfigManager() { return configManager; }
	public PlayerDataManager getProfileManager() { return profileManager; }
	public top.sparkpixel.hub.locale.LanguageManager getLanguageManager() { return configManager.getLanguageManager(); }
	public Version getCurrentVersion() { return currentVersion; }
	public ActionManager getActionManager() { return actionManager; }
}