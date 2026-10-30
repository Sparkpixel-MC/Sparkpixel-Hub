package top.sparkpixel.hub.action.actions;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import top.sparkpixel.hub.text.Text;
import org.bukkit.entity.Player;

public class TitleAction implements Action {

	@Override
	public String getIdentifier() {
		return "TITLE";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		String[] args = data.split(";");
		String mainTitle = args.length > 0 ? args[0] : "";
		String subTitle = args.length > 1 ? args[1] : "";

		int fadeIn = 1, stay = 3, fadeOut = 1;
		try {
			fadeIn = Integer.parseInt(args[2]);
			stay = Integer.parseInt(args[3]);
			fadeOut = Integer.parseInt(args[4]);
		} catch (Exception ignored) {
		}

		Text.title(player, mainTitle, subTitle, fadeIn * 20, stay * 20, fadeOut * 20);
	}
}
