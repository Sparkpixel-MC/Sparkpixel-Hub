package top.sparkpixel.hub.hook.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * LuckPerms group bridge. The primary group is checked through the API and
 * inherited groups are checked through the standard group.<name> permission,
 * which LuckPerms exposes natively.
 */
public class LuckPermsHook {

	private LuckPerms api;

	public LuckPermsHook() {
		try {
			this.api = LuckPermsProvider.get();
		} catch (IllegalStateException ignored) {
			this.api = null;
		}
	}

	public boolean isAvailable() {
		return api != null;
	}

	public boolean isInAnyGroup(Player player, List<String> groups) {
		if (groups == null || groups.isEmpty()) return false;
		for (String group : groups) {
			if (group == null || group.isBlank()) continue;
			if (api != null) {
				User user = api.getUserManager().getUser(player.getUniqueId());
				if (user != null && group.equalsIgnoreCase(user.getPrimaryGroup())) return true;
			}
			if (player.hasPermission("group." + group)) return true;
		}
		return false;
	}
}
