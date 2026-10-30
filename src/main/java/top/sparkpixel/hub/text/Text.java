package top.sparkpixel.hub.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Central MiniMessage text service. New configuration uses MiniMessage tags.
 * Plain legacy '&' strings are still accepted at the boundary so existing
 * configurations keep working while servers migrate.
 */
public final class Text {

	private static final MiniMessage MINI = MiniMessage.miniMessage();
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

	private Text() {
	}

	public static Component parse(String input) {
		if (input == null || input.isEmpty()) return Component.empty();
		if (input.indexOf('<') >= 0) {
			try {
				return MINI.deserialize(input);
			} catch (Exception ignored) {
				// Fall through to legacy parsing for malformed MiniMessage input.
			}
		}
		return LEGACY.deserialize(input);
	}

	public static List<Component> parseList(List<String> input) {
		List<Component> result = new ArrayList<>();
		if (input == null) return result;
		for (String line : input) result.add(parse(line));
		return result;
	}

	public static Component joined(List<String> input) {
		List<Component> lines = parseList(input);
		if (lines.isEmpty()) return Component.empty();
		Component result = Component.empty();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0) result = result.append(Component.newline());
			result = result.append(lines.get(i));
		}
		return result;
	}

	public static String plain(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	public static String mini(Component component) {
		return MINI.serialize(component);
	}

	public static void send(CommandSender receiver, String message) {
		receiver.sendMessage(parse(message));
	}

	public static void send(CommandSender receiver, Component message) {
		receiver.sendMessage(message);
	}

	public static void actionBar(Player player, String message) {
		player.sendActionBar(parse(message));
	}

	public static void actionBar(Player player, Component message) {
		player.sendActionBar(message);
	}

	public static void title(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		player.showTitle(Title.title(
				parse(title == null ? "" : title),
				parse(subtitle == null ? "" : subtitle),
				Title.Times.times(
						Duration.ofMillis(Math.max(0, fadeInTicks) * 50L),
						Duration.ofMillis(Math.max(0, stayTicks) * 50L),
						Duration.ofMillis(Math.max(0, fadeOutTicks) * 50L))));
	}
}