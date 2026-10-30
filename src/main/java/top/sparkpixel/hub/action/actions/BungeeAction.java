package top.sparkpixel.hub.action.actions;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.action.Action;
import org.bukkit.entity.Player;

@Deprecated
public class BungeeAction implements Action {

	@Override
	public String getIdentifier() {
		return "BUNGEE";
	}

	@Override
	public void execute(SparkpixelHubPlugin plugin, Player player, String data) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("ConnectOther");
		out.writeUTF(player.getName());
		out.writeUTF(data);
		player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
}
