package top.sparkpixel.hub.module.modules.player;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.config.Messages;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.utility.ItemStackBuilder;
import top.sparkpixel.hub.hook.SparkChessHook;
import top.sparkpixel.hub.text.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Region based PvP arena. Entering equips the configured kit, leaving restores
 * the previous inventory. There is no player-controlled PvP toggle.
 */
public class PvPMode extends Module {

	private final NamespacedKey pvpItemKey = new NamespacedKey("sparkpixel_hub", "pvp_kit_item");
	private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
	private final Set<UUID> inPvp = new HashSet<>();
	private final Map<Integer, ItemStack> kit = new HashMap<>();

	private PvpRegion region;
	private boolean restoreInventory = true;
	private boolean actionBar = true;

	public PvPMode(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.PVP_MODE);
	}

	@Override
	public void onEnable() {
		ConfigurationSection config = getConfig(ConfigType.SETTINGS).getConfigurationSection("pvp");
		if (config == null) {
			region = null;
			return;
		}

		ConfigurationSection regionSection = config.getConfigurationSection("region");
		if (regionSection != null) {
			region = PvpRegion.from(
					regionSection.getString("world", "world"),
					regionSection.getDouble("min.x"), regionSection.getDouble("min.y"), regionSection.getDouble("min.z"),
					regionSection.getDouble("max.x"), regionSection.getDouble("max.y"), regionSection.getDouble("max.z"));
		}

		restoreInventory = config.getBoolean("kit.restore-inventory", true);
		actionBar = config.getBoolean("messages.actionbar", true);
		kit.clear();

		ConfigurationSection items = config.getConfigurationSection("kit.items");
		if (items != null) {
			for (String key : items.getKeys(false)) {
				ConfigurationSection itemSection = items.getConfigurationSection(key);
				if (itemSection == null) continue;
				int slot = itemSection.getInt("slot", -1);
				if (slot < 0 || slot > 40) {
					getPlugin().getLogger().warning("PvP kit item '" + key + "' has an invalid slot: " + slot);
					continue;
				}
				try {
					ItemStack item = ItemStackBuilder.getItemStack(itemSection).build();
					ItemMeta meta = item.getItemMeta();
					if (meta != null) {
						meta.getPersistentDataContainer().set(pvpItemKey, PersistentDataType.BYTE, (byte) 1);
						item.setItemMeta(meta);
					}
					kit.put(slot, item);
				} catch (Exception e) {
					getPlugin().getLogger().warning("Could not load PvP kit item '" + key + "': " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void onDisable() {
		for (UUID uuid : Set.copyOf(inPvp)) {
			Player player = getPlugin().getServer().getPlayer(uuid);
			if (player != null) leave(player, true);
		}
		inPvp.clear();
		savedInventories.clear();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event) {
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> updateRegionState(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		if (event.getTo() == null) return;
		if (!inPvp.contains(event.getPlayer().getUniqueId()) && !inside(event.getTo())) return;
		updateRegionState(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> updateRegionState(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChange(PlayerChangedWorldEvent event) {
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> updateRegionState(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent event) {
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> updateRegionState(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		if (inPvp.contains(event.getPlayer().getUniqueId())) leave(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent event) {
		if (!inPvp.contains(event.getEntity().getUniqueId())) return;
		event.getDrops().clear();
		leave(event.getEntity(), true);
	}

	public void updateRegionState(Player player) {
		if (region == null || !region.isValid()) return;
		if (SparkChessHook.isInActiveGame(player)) return;
		boolean inside = region.contains(player.getLocation());
		boolean active = inPvp.contains(player.getUniqueId());

		if (inside && !active) enter(player);
		else if (!inside && active) leave(player, true);
	}

	private void enter(Player player) {
		if (inPvp.contains(player.getUniqueId())) return;

		PlayerInventory inventory = player.getInventory();
		savedInventories.put(player.getUniqueId(), inventory.getContents().clone());
		inPvp.add(player.getUniqueId());

		inventory.clear();
		inventory.setHelmet(null);
		inventory.setChestplate(null);
		inventory.setLeggings(null);
		inventory.setBoots(null);
		inventory.setItemInOffHand(null);

		for (Map.Entry<Integer, ItemStack> entry : kit.entrySet()) {
			int slot = entry.getKey();
			ItemStack item = entry.getValue().clone();
			switch (slot) {
				case 36 -> inventory.setHelmet(item);
				case 37 -> inventory.setChestplate(item);
				case 38 -> inventory.setLeggings(item);
				case 39 -> inventory.setBoots(item);
				case 40 -> inventory.setItemInOffHand(item);
				default -> inventory.setItem(slot, item);
			}
		}

		notify(player, true);
	}

	private void leave(Player player, boolean restore) {
		if (!inPvp.remove(player.getUniqueId())) return;

		PlayerInventory inventory = player.getInventory();
		inventory.clear();
		inventory.setHelmet(null);
		inventory.setChestplate(null);
		inventory.setLeggings(null);
		inventory.setBoots(null);
		inventory.setItemInOffHand(null);

		ItemStack[] saved = savedInventories.remove(player.getUniqueId());
		if (restore && restoreInventory && saved != null) {
			inventory.setContents(saved);
		}

		notify(player, false);
	}

	private void notify(Player player, boolean entering) {
		if (!player.isOnline()) return;
		String fallback = entering ? "<green>You entered the PvP arena." : "<yellow>You left the PvP arena.";
		String message = entering ? Messages.PVP_ENTER.get(player) : Messages.PVP_LEAVE.get(player);
		if (message == null || message.isBlank()) message = fallback;
		if (actionBar) Text.actionBar(player, message);
		else Text.send(player, message);
	}

	public boolean isInPvp(UUID uuid) {
		return inPvp.contains(uuid);
	}

	public PvpRegion getRegion() {
		return region;
	}

	public void setRegion(PvpRegion region) {
		this.region = region;
		for (Player player : getPlugin().getServer().getOnlinePlayers()) updateRegionState(player);
	}

	private boolean inside(Location location) {
		return region != null && region.isValid() && region.contains(location);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPvpDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player victim)) return;
		Player attacker = null;
		if (event.getDamager() instanceof Player player) {
			attacker = player;
		} else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile
				&& projectile.getShooter() instanceof Player shooter) {
			attacker = shooter;
		}
		if (attacker == null) return;
		if (SparkChessHook.isInActiveGame(attacker) || SparkChessHook.isInActiveGame(victim)) return;
		if (isInPvp(attacker.getUniqueId()) && isInPvp(victim.getUniqueId())) return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onKitItemMove(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		ItemStack item = event.getCurrentItem();
		if (!isKitItem(item)) return;
		event.setCancelled(true);
	}

	public boolean isKitItem(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(pvpItemKey, PersistentDataType.BYTE);
	}
}