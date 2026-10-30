package top.sparkpixel.hub.hook;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.hook.hooks.LuckPermsHook;
import top.sparkpixel.hub.hook.hooks.head.BaseHead;
import top.sparkpixel.hub.hook.hooks.head.DatabaseHead;
import top.sparkpixel.hub.utility.PlaceholderUtil;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HooksManager {

	private final Map<String, PluginHook> hooks;
	private LuckPermsHook luckPermsHook;

	public HooksManager(SparkpixelHubPlugin plugin) {
		hooks = new HashMap<>();

		// Base64 head
		hooks.put("BASE64", new BaseHead());

		// PlaceholderAPI
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			hooks.put("PLACEHOLDER_API", null);
			PlaceholderUtil.PAPI = true;
			plugin.getLogger().info(" Hooked into PlaceholderAPI");
		}

		if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
			hooks.put("HEAD_DATABASE", new DatabaseHead());
			plugin.getLogger().info(" Hooked into HeadDatabase");
		}

		if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
			luckPermsHook = new LuckPermsHook();
			plugin.getLogger().info(" Hooked into LuckPerms");
		}

		hooks.values().stream().filter(Objects::nonNull).forEach(pluginHook -> pluginHook.onEnable(plugin));

	}

	public boolean isHookEnabled(String id) {
		return hooks.containsKey(id);
	}

	public PluginHook getPluginHook(String id) {
		return hooks.get(id);
	}

	public LuckPermsHook getLuckPermsHook() {
		return luckPermsHook;
	}

}