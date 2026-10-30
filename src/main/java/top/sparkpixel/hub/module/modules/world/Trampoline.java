package top.sparkpixel.hub.module.modules.world;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.Permissions;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.hook.hooks.LuckPermsHook;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.text.Sounds;
import top.sparkpixel.hub.text.Text;
import top.sparkpixel.hub.utility.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * VIP trampoline. A configured block pattern is built on top of the ground
 * (never inside it), then launches players that step on it. Access is checked
 * through LuckPerms groups, with a permission fallback when LuckPerms is absent.
 */
public class Trampoline extends Module {

	private final NamespacedKey itemKey = new NamespacedKey("sparkpixel_hub", "trampoline_item");
	private final List<String> allowedGroups = new ArrayList<>();
	private final Map<Location, BlockData> placedBlocks = new LinkedHashMap<>();
	private final Map<UUID, Long> launchCooldown = new HashMap<>();
	private final Set<BukkitTask> cleanupTasks = new HashSet<>();

	private boolean itemEnabled;
	private int itemSlot;
	private ItemStack item;
	private List<String> pattern = List.of("SSS", "SSS", "SSS");
	private Map<Character, Material> palette = new HashMap<>();
	private int duration;
	private double power;
	private double powerY;
	private Sound launchSound;
	private long cooldownMillis;
	private String fallbackPermission;

	public Trampoline(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.TRAMPOLINE);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		ConfigurationSection section = config.getConfigurationSection("trampoline");
		if (section == null) return;

		allowedGroups.clear();
		allowedGroups.addAll(config.getStringList("trampoline.allowed-groups"));
		fallbackPermission = config.getString("trampoline.fallback-permission", Permissions.TRAMPOLINE_USE.getPermission());
		itemEnabled = config.getBoolean("trampoline.item.enabled", true);
		itemSlot = Math.max(0, Math.min(35, config.getInt("trampoline.item.slot", 5)));
		duration = Math.max(5, config.getInt("trampoline.structure.duration", 30));
		power = config.getDouble("trampoline.launch.power", 2.5);
		powerY = config.getDouble("trampoline.launch.power-y", 1.4);
		cooldownMillis = Math.max(0, config.getLong("trampoline.launch.cooldown", 1)) * 1000L;
		launchSound = Sounds.resolve(config.getString("trampoline.launch.sound", "minecraft:entity.firework_rocket.launch"),
				Sound.ENTITY_FIREWORK_ROCKET_LAUNCH);

		pattern = config.getStringList("trampoline.structure.pattern");
		if (pattern.isEmpty()) pattern = List.of("SSS", "SSS", "SSS");
		palette.clear();
		ConfigurationSection blocks = config.getConfigurationSection("trampoline.structure.blocks");
		if (blocks != null) {
			for (String key : blocks.getKeys(false)) {
				if (key.isEmpty()) continue;
				try {
					palette.put(key.charAt(0), Material.valueOf(blocks.getString(key, "SLIME_BLOCK").toUpperCase()));
				} catch (IllegalArgumentException e) {
					getPlugin().getLogger().warning("Invalid trampoline block for '" + key + "'");
				}
			}
		}
		if (palette.isEmpty()) palette.put('S', Material.SLIME_BLOCK);

		ConfigurationSection itemSection = config.getConfigurationSection("trampoline.item");
		if (itemSection != null) {
			item = ItemStackBuilder.getItemStack(itemSection).build();
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
				item.setItemMeta(meta);
			}
		}
	}

	@Override
	public void onDisable() {
		for (BukkitTask task : cleanupTasks) task.cancel();
		cleanupTasks.clear();
		restoreAll();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			if (!player.isOnline()) return;
			if (itemEnabled && canUse(player) && item != null) {
				player.getInventory().setItem(itemSlot, item.clone());
			} else {
				removeItem(player);
			}
		}, 5L);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		launchCooldown.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player player = event.getPlayer();
		ItemStack held = player.getInventory().getItemInMainHand();
		if (!isTrampolineItem(held)) return;
		event.setCancelled(true);
		if (!canUse(player)) {
			Messages.TRAMPOLINE_NO_PERMISSION.send(player);
			return;
		}
		place(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (event.getTo() == null) return;
		Block below = event.getTo().clone().subtract(0, 0.2, 0).getBlock();
		if (!placedBlocks.containsKey(below.getLocation())) return;
		if (!canUse(player)) return;

		long now = System.currentTimeMillis();
		long last = launchCooldown.getOrDefault(player.getUniqueId(), 0L);
		if (now - last < cooldownMillis) return;
		launchCooldown.put(player.getUniqueId(), now);

		player.setFallDistance(0);
		player.setVelocity(new Vector(0, powerY, 0));
		player.getWorld().playSound(player.getLocation(), launchSound, 1.0f, 1.0f);
		Text.actionBar(player, Messages.TRAMPOLINE_LAUNCH.get(player));
	}

	public boolean place(Player player) {
		Location base = player.getLocation().clone();
		base.add(base.getDirection().setY(0).normalize().multiply(2));
		base.setY(Math.floor(base.getY()));

		World world = base.getWorld();
		Location ground = null;
		for (int y = base.getBlockY() + 2; y >= base.getBlockY() - 3; y--) {
			Block candidate = world.getBlockAt(base.getBlockX(), y, base.getBlockZ());
			if (candidate.getType().isSolid()) {
				ground = candidate.getLocation().add(0, 1, 0);
				break;
			}
		}
		if (ground == null) {
			Messages.TRAMPOLINE_NO_SPACE.send(player);
			return false;
		}

		Map<Location, Material> newBlocks = new LinkedHashMap<>();
		for (int row = 0; row < pattern.size(); row++) {
			String line = pattern.get(row);
			for (int column = 0; column < line.length(); column++) {
				Material material = palette.get(line.charAt(column));
				if (material == null) continue;
				double offsetX = column - (line.length() - 1) / 2.0;
				double offsetZ = row - (pattern.size() - 1) / 2.0;
				Location location = ground.clone().add(offsetX, 0, offsetZ).getBlock().getLocation();
				Block block = location.getBlock();
				if (!block.getType().isAir() && !block.isReplaceable()) {
					Messages.TRAMPOLINE_NO_SPACE.send(player);
					return false;
				}
				newBlocks.put(location.clone(), material);
			}
		}

		Map<Location, BlockData> previous = new LinkedHashMap<>();
		for (Location location : newBlocks.keySet()) {
			previous.put(location.clone(), location.getBlock().getBlockData().clone());
		}
		for (Map.Entry<Location, Material> entry : newBlocks.entrySet()) {
			entry.getKey().getBlock().setType(entry.getValue(), false);
		}
		placedBlocks.putAll(previous);

		BukkitTask task = Bukkit.getScheduler().runTaskLater(getPlugin(), () -> cleanup(previous.keySet()), duration * 20L);
		cleanupTasks.add(task);
		Messages.TRAMPOLINE_PLACED.send(player);
		return true;
	}

	private void cleanup(Set<Location> locations) {
		for (Location location : locations) {
			BlockData old = placedBlocks.remove(location);
			if (old != null) location.getBlock().setBlockData(old, false);
		}
	}

	private void restoreAll() {
		for (Map.Entry<Location, BlockData> entry : placedBlocks.entrySet()) {
			entry.getKey().getBlock().setBlockData(entry.getValue(), false);
		}
		placedBlocks.clear();
	}

	public boolean canUse(Player player) {
		if (player.hasPermission(Permissions.COMMAND_TRAMPOLINE.getPermission())
				|| player.hasPermission("sparkpixelhub.trampoline.admin")) return true;

		LuckPermsHook hook = getPlugin().getHookManager().getLuckPermsHook();
		if (hook != null && hook.isAvailable()) {
			if (hook.isInAnyGroup(player, allowedGroups)) return true;
		}
		for (String group : allowedGroups) {
			if (player.hasPermission("group." + group)) return true;
		}
		return fallbackPermission != null && !fallbackPermission.isBlank() && player.hasPermission(fallbackPermission);
	}

	private boolean isTrampolineItem(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
	}

	private void removeItem(Player player) {
		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			if (isTrampolineItem(player.getInventory().getItem(slot))) {
				player.getInventory().setItem(slot, null);
			}
		}
	}
}