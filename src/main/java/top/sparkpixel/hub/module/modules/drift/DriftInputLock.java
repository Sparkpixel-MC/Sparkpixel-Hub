package top.sparkpixel.hub.module.modules.drift;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

/**
 * Client vehicle input is cancelled for drift riders. Because the server no
 * longer accepts the client position, the manager also sends an authoritative
 * entity teleport every tick; this is what prevents the "snap back when the
 * player releases WASD" problem.
 */
public final class DriftInputLock implements DriftInput {

	private final DriftManager manager;
	private final ProtocolManager protocol;
	private final PacketAdapter listener;

	public DriftInputLock(DriftManager manager) {
		this.manager = manager;
		this.protocol = ProtocolLibrary.getProtocolManager();
		this.listener = new PacketAdapter(manager.getPlugin(), ListenerPriority.HIGHEST,
				PacketType.Play.Client.VEHICLE_MOVE,
				PacketType.Play.Client.STEER_VEHICLE) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer();
				if (player != null && manager.isDriftRider(player)) {
					event.setCancelled(true);
				}
			}
		};
		this.protocol.addPacketListener(listener);
	}

	@Override
	public void sendTeleport(Player rider, Boat boat) {
		try {
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
			packet.getIntegers().write(0, boat.getEntityId());
			packet.getDoubles().write(0, boat.getLocation().getX());
			packet.getDoubles().write(1, boat.getLocation().getY());
			packet.getDoubles().write(2, boat.getLocation().getZ());
			try {
				packet.getBytes().write(0, angleToByte(boat.getLocation().getYaw()));
				packet.getBytes().write(1, angleToByte(boat.getLocation().getPitch()));
			} catch (Exception ignored) {
				// Some protocol versions do not expose angle fields on this packet.
			}
			try {
				packet.getBooleans().write(0, boat.isOnGround());
			} catch (Exception ignored) {
				// onGround is optional on newer protocol versions.
			}
			protocol.sendServerPacket(rider, packet);
		} catch (Exception ignored) {
			// A protocol update can change packet layouts; never let this break the boat task.
		}
	}

	private static byte angleToByte(float angle) {
		return (byte) ((int) (angle * 256.0F / 360.0F));
	}

	@Override
	public void close() {
		protocol.removePacketListener(listener);
	}
}