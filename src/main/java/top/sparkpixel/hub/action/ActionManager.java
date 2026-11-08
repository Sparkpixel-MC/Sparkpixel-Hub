package top.sparkpixel.hub.action;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.actions.*;
import top.sparkpixel.hub.utility.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManager {

	private final SparkpixelHubPlugin plugin;
	private final Map<String, Action> actions;

	public ActionManager(SparkpixelHubPlugin plugin) {
		this.plugin = plugin;
		actions = new HashMap<>();
		load();
	}

	private void load() {
		registerAction(
				new MessageAction(),
				new BroadcastMessageAction(),
				new CommandAction(),
				new ConsoleCommandAction(),
				new SoundAction(),
				new PotionEffectAction(),
				new GamemodeAction(),
				new ProxyAction(),
				new CloseInventoryAction(),
				new ActionbarAction(),
				new TitleAction(),
				new MenuAction()
		);
	}

	public void registerAction(Action... actions) {
		Arrays.asList(actions).forEach(action -> this.actions.put(action.getIdentifier(), action));
	}

	public void executeActions(Player player, List<String> items) {
		items.forEach(item -> {
			int start = item.indexOf('[');
			int end = item.indexOf(']');
			String actionName = (start >= 0 && end > start) ? item.substring(start + 1, end) : null;
			Action action = actionName == null ? null : actions.get(actionName.trim().toUpperCase());

			if (action != null) {
				item = item.contains(" ") ? item.split(" ", 2)[1] : "";
				item = PlaceholderUtil.setPlaceholders(item, player);

				action.execute(plugin, player, item);
			} else {
				plugin.getLogger().warning("There was a problem attempting to process action: '" + item + "'");
			}
		});
	}
}