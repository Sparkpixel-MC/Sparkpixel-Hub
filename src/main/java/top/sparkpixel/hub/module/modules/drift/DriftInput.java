package top.sparkpixel.hub.module.modules.drift;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

public interface DriftInput {

	void sendTeleport(Player rider, Boat boat);

	void close();
}
