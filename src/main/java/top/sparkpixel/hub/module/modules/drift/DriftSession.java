package top.sparkpixel.hub.module.modules.drift;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

import java.util.UUID;

public record DriftSession(UUID rider, UUID boat, Player player, Boat entity) {
}
