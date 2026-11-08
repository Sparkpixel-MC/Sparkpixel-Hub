package top.sparkpixel.hub;

public enum Permissions {

	// Command permissions
	COMMAND_HUB_HELP("command.help"),
	COMMAND_HUB_RELOAD("command.reload"),
	COMMAND_OPEN_MENUS("command.openmenu"),
	COMMAND_HOLOGRAMS("command.holograms"),

	COMMAND_GAMEMODE("command.gamemode"),
	COMMAND_GAMEMODE_OTHERS("command.gamemode.others"),
	COMMAND_FLIGHT("command.fly"),
	COMMAND_FLIGHT_OTHERS("command.fly.others"),
	COMMAND_SET_LOBBY("command.setlobby"),
	COMMAND_VANISH("command.vanish"),
	COMMAND_BUILD_MODE("command.buildmode"),
	COMMAND_BUILD_MODE_OTHERS("command.buildmode.others"),
	COMMAND_PVP("command.pvp"),
	COMMAND_TRAMPOLINE("command.trampoline"),

	// Module stuff
	ANTI_WDL_BYPASS("bypass.antiwdl"),
	DOUBLE_JUMP_BYPASS("bypass.doublejump"),

	ANTI_WDL_NOTIFY("alert.antiwdl"),

	// Trampoline fallback when LuckPerms is not installed
	TRAMPOLINE_USE("trampoline.use"),
	PARKOUR_USE("parkour.use"),
	COMMAND_STATS("command.stats"),
	COMMAND_FEEDBACK("command.feedback");

	private final String perm;

	Permissions(String perm) {
		this.perm = perm;
	}

	public final String getPermission() {
		return "sparkpixelhub." + this.perm;
	}

}