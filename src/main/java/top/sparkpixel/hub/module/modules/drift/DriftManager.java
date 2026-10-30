package top.sparkpixel.hub.module.modules.drift;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative river drifting.
 *
 * <p>The boat is always moved by the server along the vanilla water-level
 * gradient. Client steering packets are cancelled and the server sends an
 * entity teleport every tick, so the client cannot override the boat and the
 * boat cannot snap back after the player releases a key.</p>
 */
public class DriftManager extends Module {

	private final NamespacedKey boatKey = new NamespacedKey("sparkpixel_hub", "drift_boat");
	private final Map<UUID, DriftSession> sessions = new HashMap<>();

	private DriftInput inputLock;
	private Boat.Type boatType = Boat.Type.OAK;
	private double speed;
	private double maxSpeed;
	private int taskId = -1;
	private boolean actionBar = true;

	public DriftManager(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.DRIFT);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		ConfigurationSection section = config.getConfigurationSection("drift");
		if (section == null) return;

		speed = Math.max(0.1, config.getDouble("drift.speed", 0.55));
		actionBar = config.getBoolean("drift.messages.actionbar", true);
		maxSpeed = Math.max(speed, config.getDouble("drift.max-speed", 2.0));
		try {
			String type = config.getString("drift.boat-type", "OAK").toUpperCase()
					.replace("_CHEST_BOAT", "").replace("_BOAT", "").replace("_RAFT", "");
			boatType = Boat.Type.valueOf(type);
		} catch (IllegalArgumentException e) {
			boatType = Boat.Type.OAK;
		}

		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
			try {
				Class<?> lockClass = Class.forName("top.sparkpixel.hub.module.modules.drift.DriftInputLock");
				inputLock = (DriftInput) lockClass.getConstructor(DriftManager.class).newInstance(this);
				getPlugin().getLogger().info("Drift input lock hooked into ProtocolLib.");
			} catch (Throwable t) {
				inputLock = null;
				getPlugin().getLogger().warning("ProtocolLib input lock unavailable: " + t.getMessage());
			}
		} else {
			getPlugin().getLogger().warning("ProtocolLib is not installed; drift boats will use the fallback position sync.");
		}

		taskId = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::tick, 1L, 1L).getTaskId();
	}

	@Override
	public void onDisable() {
		if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
		for (DriftSession session : sessions.values()) {
			if (session.entity() != null && session.entity().isValid()) session.entity().remove();
		}
		sessions.clear();
		if (inputLock != null) inputLock.close();
	}

	public boolean spawn(Player player) {
		Block water = WaterFlow.findWater(player.getLocation());
		if (water == null) {
			Block target = player.getTargetBlockExact(8);
			if (target != null && target.getType() == Material.WATER) water = target;
		}
		if (water == null) {
			Messages.DRIFT_NO_WATER.send(player);
			return false;
		}

		Location location = water.getLocation().add(0.5, 1.0, 0.5);
		Entity entity;
		try {
			entity = player.getWorld().spawnEntity(location, entityTypeFor(boatType));
		} catch (Exception e) {
			getPlugin().getLogger().warning("Could not spawn drift boat: " + e.getMessage());
			Messages.DRIFT_FAILED.send(player);
			return false;
		}
		if (!(entity instanceof Boat boat)) {
			entity.remove();
			Messages.DRIFT_FAILED.send(player);
			return false;
		}
		try {
			boat.setMaxSpeed(maxSpeed);
			boat.setOccupiedDeceleration(0.0);
			boat.setUnoccupiedDeceleration(0.0);
		} catch (UnsupportedOperationException ignored) {
			// Some boat implementations expose read-only physics properties.
		}
		boat.setPersistent(false);
		boat.getPersistentDataContainer().set(boatKey, PersistentDataType.BYTE, (byte) 1);

		if (!boat.addPassenger(player)) {
			boat.remove();
			Messages.DRIFT_FAILED.send(player);
			return false;
		}

		start(player, boat);
		if (actionBar) Text.actionBar(player, Messages.DRIFT_START.get(player));
		else Messages.DRIFT_START.send(player);
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnter(VehicleEnterEvent event) {
		if (!(event.getVehicle() instanceof Boat boat)) return;
		if (!isDriftBoat(boat)) return;
		if (!(event.getEntered() instanceof Player player)) return;
		start(player, boat);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onExit(VehicleExitEvent event) {
		if (!(event.getVehicle() instanceof Boat boat)) return;
		if (!isDriftBoat(boat)) return;
		if (!(event.getExited() instanceof Player player)) return;
		stop(player, true);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		stop(event.getPlayer(), true);
	}

	private EntityType entityTypeFor(Boat.Type type) {
		return switch (type) {
			case SPRUCE -> EntityType.SPRUCE_BOAT;
			case BIRCH -> EntityType.BIRCH_BOAT;
			case JUNGLE -> EntityType.JUNGLE_BOAT;
			case ACACIA -> EntityType.ACACIA_BOAT;
			case CHERRY -> EntityType.CHERRY_BOAT;
			case DARK_OAK -> EntityType.DARK_OAK_BOAT;
			case MANGROVE -> EntityType.MANGROVE_BOAT;
			case BAMBOO -> EntityType.BAMBOO_RAFT;
			default -> EntityType.OAK_BOAT;
		};
	}

	private void start(Player player, Boat boat) {
		DriftSession previous = sessions.remove(player.getUniqueId());
		if (previous != null && previous.entity() != boat && previous.entity() != null && previous.entity().isValid()) {
			previous.entity().remove();
		}

		try {
			boat.setMaxSpeed(maxSpeed);
			boat.setOccupiedDeceleration(0.0);
		} catch (UnsupportedOperationException ignored) {
		}
		sessions.put(player.getUniqueId(), new DriftSession(player.getUniqueId(), boat.getUniqueId(), player, boat));
	}

	private void stop(Player player, boolean removeBoat) {
		DriftSession session = sessions.remove(player.getUniqueId());
		if (session == null) return;
		if (removeBoat && session.entity() != null && session.entity().isValid()) {
			session.entity().remove();
		}
	}

	public boolean isDriftBoat(Entity entity) {
		return entity != null && entity.getPersistentDataContainer().has(boatKey, PersistentDataType.BYTE);
	}

	public boolean isDriftRider(Player player) {
		return player != null && sessions.containsKey(player.getUniqueId());
	}

	private void tick() {
		if (sessions.isEmpty()) return;

		for (DriftSession session : Map.copyOf(sessions).values()) {
			Player player = session.player();
			Boat boat = session.entity();

			if (!player.isOnline() || boat == null || !boat.isValid() || boat.isDead()
					|| !boat.getPassengers().contains(player)) {
				stop(player, true);
				continue;
			}

			Vector flow = WaterFlow.flowAt(boat.getLocation());
			if (flow == null) {
				boat.setVelocity(new Vector(0, 0, 0));
				continue;
			}

			float yaw = (float) Math.toDegrees(Math.atan2(-flow.getX(), flow.getZ()));
			boat.setRotation(yaw, 0);
			boat.setVelocity(flow.clone().multiply(speed).setY(0));

			if (inputLock != null) {
				inputLock.sendTeleport(player, boat);
			} else if (Bukkit.getCurrentTick() % 2 == 0) {
				// Fallback for servers without ProtocolLib: periodically reassert the
				// server position so the client cannot keep a predicted position.
				boat.teleport(boat.getLocation(), io.papermc.paper.entity.TeleportFlag.EntityState.RETAIN_PASSENGERS);
			}
		}
	}
}