package top.sparkpixel.hub.config;

import top.sparkpixel.hub.locale.LanguageManager;
import top.sparkpixel.hub.text.Text;
import org.bukkit.command.CommandSender;


public enum Messages {

	PREFIX("GENERAL.PREFIX"),
	NO_PERMISSION("GENERAL.NO_PERMISSION"),
	CUSTOM_COMMAND_NO_PERMISSION("GENERAL.CUSTOM_COMMAND_NO_PERMISSION"),
	INVALID_PLAYER("GENERAL.INVALID_PLAYER"),
	CONFIG_RELOAD("GENERAL.CONFIG_RELOAD"),
	COOLDOWN_ACTIVE("GENERAL.COOLDOWN_ACTIVE"),

	GAMEMODE_CHANGE("GAMEMODE.GAMEMODE_CHANGE"),
	GAMEMODE_CHANGE_OTHER("GAMEMODE.GAMEMODE_CHANGE_OTHER"),
	GAMEMODE_INVALID("GAMEMODE.GAMEMODE_INVALID"),

	VANISH_ENABLE("VANISH.ENABLE"),
	VANISH_DISABLE("VANISH.DISABLE"),

	FLIGHT_ENABLE("FLIGHT.ENABLE"),
	FLIGHT_ENABLE_OTHER("FLIGHT.ENABLE_OTHER"),
	FLIGHT_DISABLE("FLIGHT.DISABLE"),
	FLIGHT_DISABLE_OTHER("FLIGHT.DISABLE_OTHER"),

	PLAYER_HIDER_HIDDEN("PLAYER_HIDER.HIDDEN"),
	PLAYER_HIDER_SHOWN("PLAYER_HIDER.SHOWN"),

	SET_LOBBY("LOBBY.SET_LOBBY"),

	DOUBLE_JUMP_COOLDOWN("DOUBLE_JUMP.COOLDOWN_ACTIVE"),

	EVENT_ITEM_DROP("WORLD_EVENT_MODIFICATIONS.ITEM_DROP"),
	EVENT_ITEM_PICKUP("WORLD_EVENT_MODIFICATIONS.ITEM_PICKUP"),
	EVENT_BLOCK_PLACE("WORLD_EVENT_MODIFICATIONS.BLOCK_PLACE"),
	EVENT_BLOCK_BREAK("WORLD_EVENT_MODIFICATIONS.BLOCK_BREAK"),
	EVENT_BLOCK_INTERACT("WORLD_EVENT_MODIFICATIONS.BLOCK_INTERACT"),
	EVENT_PLAYER_PVP("WORLD_EVENT_MODIFICATIONS.PLAYER_PVP"),

	HOLOGRAMS_EMPTY("HOLOGRAMS.EMPTY"),
	HOLOGRAMS_ALREADY_EXISTS("HOLOGRAMS.ALREADY_EXISTS"),
	HOLOGRAMS_INVALID_HOLOGRAM("HOLOGRAMS.INVALID_HOLOGRAM"),
	HOLOGRAMS_INVALID_LINE("HOLOGRAMS.INVALID_LINE"),
	HOLOGRAMS_SPAWNED("HOLOGRAMS.SPAWNED"),
	HOLOGRAMS_DESPAWNED("HOLOGRAMS.DESPAWNED"),
	HOLOGRAMS_MOVED("HOLOGRAMS.MOVED"),
	HOLOGRAMS_LINE_SET("HOLOGRAMS.LINE_SET"),
	HOLOGRAMS_ADDED_LINE("HOLOGRAMS.ADDED_LINE"),
	HOLOGRAMS_REMOVED_LINE("HOLOGRAMS.REMOVED_LINE"),

	WORLD_DOWNLOAD_NOTIFY("ANTI_WORLD_DOWNLOADER.ADMIN_NOTIFY"),

	BUILD_MODE_ENABLED_ACTIONBAR("BUILD_MODE.ENABLED_ACTION_BAR"),
	BUILD_MODE_ENABLED("BUILD_MODE.ENABLED"),
	BUILD_MODE_DISABLED("BUILD_MODE.DISABLED"),
	BUILD_MODE_COMMAND_TARGET_NOT_FOUND("BUILD_MODE.TARGET_NOT_FOUND"),
	BUILD_MODE_COMMAND_ENABLED_FOR_TARGET("BUILD_MODE.ENABLED_FOR_TARGET"),
	BUILD_MODE_COMMAND_IN_PVP_MODE("BUILD_MODE.IN_PVP_MODE"),
	BUILD_MODE_COMMAND_TARGET_IN_PVP_MODE("BUILD_MODE.TARGET_IN_PVP_MODE"),

	PARKOUR_NOT_CONFIGURED("PARKOUR.NOT_CONFIGURED"),

	PVP_ENTER("PVP.ENTER"),
	PVP_LEAVE("PVP.LEAVE"),

	TRAMPOLINE_PLACED("TRAMPOLINE.PLACED"),
	TRAMPOLINE_NO_PERMISSION("TRAMPOLINE.NO_PERMISSION"),
	TRAMPOLINE_NO_SPACE("TRAMPOLINE.NO_SPACE"),
	TRAMPOLINE_LAUNCH("TRAMPOLINE.LAUNCH"),
	TRAMPOLINE_PROTECTED("TRAMPOLINE.PROTECTED"),

	TELEPORTATION_BOW_IN_COOLDOWN("TELEPORTATION_BOW.IN_COOLDOWN"),
	TELEPORTATION_BOW_MAX_DISTANCE_REACHED("TELEPORTATION_BOW.MAX_DISTANCE_REACHED");

	private static LanguageManager languageManager;
	private final String path;

	Messages(String path) {
		this.path = path;
	}

	public static void setLanguageManager(LanguageManager manager) {
		languageManager = manager;
	}

	/**
	 * Resolves the message in the sender's client language.
	 */
	public String get(CommandSender sender) {
		if (languageManager == null) return missing();
		String message = languageManager.getMessage(sender, this.path);
		return message != null ? message : missing();
	}

	@Override
	public String toString() {
		if (languageManager == null) return missing();
		String message = languageManager.getDefaultMessage(this.path);
		return message != null ? message : missing();
	}

	private String missing() {
		return "Sparkpixel-Hub: message not found (" + this.path + ") - Check your lang files!";
	}

	public void send(CommandSender receiver, Object... replacements) {
		String message = get(receiver);
		if (!message.isEmpty()) {
			receiver.sendMessage(Text.parse(replace(receiver, message, replacements)));
		}
	}

	private String replace(CommandSender receiver, String message, Object... replacements) {
		for (int i = 0; i < replacements.length; i += 2) {
			if (i + 1 >= replacements.length) break;
			message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
		}

		String prefix = languageManager == null ? null : languageManager.getMessage(receiver, PREFIX.getPath());
		return message.replace("%prefix%", prefix != null && !prefix.isEmpty() ? prefix : "");
	}

	public String getPath() {
		return this.path;
	}
}