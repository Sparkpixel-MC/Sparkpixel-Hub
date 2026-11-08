package top.sparkpixel.hub.module.modules.engagement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import top.sparkpixel.hub.SparkpixelHubPlugin;
import top.sparkpixel.hub.config.ConfigType;
import top.sparkpixel.hub.inventory.BuiltinMenu;
import top.sparkpixel.hub.inventory.InventoryBuilder;
import top.sparkpixel.hub.inventory.InventoryItem;
import top.sparkpixel.hub.module.Module;
import top.sparkpixel.hub.module.ModuleType;
import top.sparkpixel.hub.profile.PlayerProfile;
import top.sparkpixel.hub.text.Text;
import top.sparkpixel.hub.utility.Locations;
import top.sparkpixel.hub.hook.SparkChessHook;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Lobby retention layer: daily login, quests, achievements, collectibles,
 * shop cosmetics, leaderboards, news, guide, feedback and event rotation.
 */
public class EngagementManager extends Module {

	private final NamespacedKey menuKey = new NamespacedKey("sparkpixel_hub", "hub_menu");
	private final NamespacedKey statsKey = new NamespacedKey("sparkpixel_hub", "hub_stats");
	private final NamespacedKey hideKey = new NamespacedKey("sparkpixel_hub", "hub_hide");
	private final Map<String, Quest> dailyQuests = new LinkedHashMap<>();
	private final Map<String, Quest> weeklyQuests = new LinkedHashMap<>();
	private final Map<String, Achievement> achievements = new LinkedHashMap<>();
	private final Map<String, Collectible> collectibles = new LinkedHashMap<>();
	private final Map<String, Cosmetic> cosmetics = new LinkedHashMap<>();
	private final List<HubEvent> events = new ArrayList<>();
	private final List<NewsEntry> news = new ArrayList<>();
	private final List<GuideStep> guide = new ArrayList<>();
	private final Map<String, ServerEntry> servers = new LinkedHashMap<>();

	private Material menuMaterial = Material.COMPASS;
	private int menuSlot = 4;
	private String menuName = "<gradient:#9863E7:#545eb6><bold>Sparkpixel Hub</bold></gradient>";
	private List<String> menuLore = List.of("<gray>Right-click to open the hub menu");
	private String socialFriends = "暂无";
	private String socialGuild = "暂无";
	private String defaultTrail = "";

	private int coinsPerMinute = 1;
	private int xpPerMinute = 1;

	private int dailyBaseCoins = 50;
	private int dailyStreakBonus = 10;
	private int dailyMaxBonus = 100;

	private int returnDays = 7;
	private int returnCoins = 200;
	private int returnXp = 100;

	private String seasonName = "";
	private boolean seasonEnabled;
	private String seasonStart = "";
	private String seasonEnd = "";
	private int seasonBonus;

	private int taskId = -1;
	private int eventIntervalMinutes = 30;
	private long nextEventAt;
	private int eventIndex;
	private int minuteCounter;
	private int trailCounter;

	public EngagementManager(SparkpixelHubPlugin plugin) {
		super(plugin, ModuleType.ENGAGEMENT);
	}

	@Override
	public void onEnable() {
		loadConfig();
		registerMenus();
		nextEventAt = System.currentTimeMillis() + Math.max(1, eventIntervalMinutes) * 60_000L;
		taskId = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::tick, 20L, 20L).getTaskId();
	}

	private void loadConfig() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);

		ConfigurationSection menu = config.getConfigurationSection("progression.menu-item");
		if (menu != null) {
			try { menuMaterial = Material.valueOf(menu.getString("material", "COMPASS").toUpperCase()); } catch (Exception ignored) {}
			menuSlot = menu.getInt("slot", 4);
			menuName = menu.getString("name", menuName);
			menuLore = menu.getStringList("lore");
		socialFriends = config.getString("progression.social.friends", "暂无");
		socialGuild = config.getString("progression.social.guild", "暂无");
		defaultTrail = config.getString("progression.default-trail", "");
		}
		coinsPerMinute = config.getInt("progression.playtime.coins-per-minute", 1);
		xpPerMinute = config.getInt("progression.playtime.xp-per-minute", 1);
		dailyBaseCoins = config.getInt("progression.daily-login.base-coins", 50);
		dailyStreakBonus = config.getInt("progression.daily-login.streak-bonus", 10);
		dailyMaxBonus = config.getInt("progression.daily-login.max-streak-bonus", 100);
		returnDays = config.getInt("progression.return-reward.days", 7);
		returnCoins = config.getInt("progression.return-reward.coins", 200);
		returnXp = config.getInt("progression.return-reward.xp", 100);

		seasonEnabled = config.getBoolean("seasonal.enabled", false);
		seasonName = config.getString("seasonal.name", "");
		seasonStart = config.getString("seasonal.start", "");
		seasonEnd = config.getString("seasonal.end", "");
		seasonBonus = config.getInt("seasonal.daily-bonus", 0);

		loadQuests(config, "progression.quests.daily", dailyQuests, true);
		loadQuests(config, "progression.quests.weekly", weeklyQuests, false);
		loadAchievements(config);
		loadCollectibles(config);
		loadShop(config);
		eventIntervalMinutes = Math.max(1, config.getInt("events.interval-minutes", 30));
		loadEvents(config);
		loadServers(config);
		loadNews(config);
		loadGuide(config);
	}

	private void loadQuests(FileConfiguration config, String path, Map<String, Quest> target, boolean daily) {
		target.clear();
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null) return;
		for (String key : section.getKeys(false)) {
			ConfigurationSection q = section.getConfigurationSection(key);
			if (q == null) continue;
			target.put(key, new Quest(key, q.getString("name", key), q.getString("type", "PARKOUR_FINISH"),
					q.getInt("target", 1), q.getInt("coins", 0), q.getInt("xp", 0), daily));
		}
	}

	private void loadAchievements(FileConfiguration config) {
		achievements.clear();
		ConfigurationSection section = config.getConfigurationSection("progression.achievements");
		if (section == null) return;
		for (String key : section.getKeys(false)) {
			ConfigurationSection a = section.getConfigurationSection(key);
			if (a == null) continue;
			achievements.put(key, new Achievement(key, a.getString("name", key),
					a.getStringList("description"), a.getString("type", "PARKOUR_FINISH"),
					a.getInt("target", 1), a.getInt("coins", 0), a.getInt("xp", 0)));
		}
	}

	private void loadCollectibles(FileConfiguration config) {
		collectibles.clear();
		ConfigurationSection section = config.getConfigurationSection("collectibles");
		if (section == null) return;
		for (String key : section.getKeys(false)) {
			ConfigurationSection c = section.getConfigurationSection(key);
			if (c == null) continue;
			Location location = Locations.parse(c.getString("location"));
			if (location == null) continue;
			collectibles.put(key, new Collectible(key, c.getString("name", key), location,
					c.getDouble("radius", 2.0), c.getInt("coins", 20), c.getInt("xp", 10)));
		}
	}

	private void loadShop(FileConfiguration config) {
		cosmetics.clear();
		ConfigurationSection section = config.getConfigurationSection("shop.cosmetics");
		if (section == null) return;
		for (String key : section.getKeys(false)) {
			ConfigurationSection c = section.getConfigurationSection(key);
			if (c == null) continue;
			cosmetics.put(key, new Cosmetic(key, c.getString("name", key), c.getStringList("lore"),
					Material.valueOf(c.getString("material", "BLAZE_POWDER").toUpperCase()),
					c.getInt("price", 100), c.getString("particle", "FLAME")));
		}
	}

	private void loadServers(FileConfiguration config) {
		servers.clear();
		ConfigurationSection section = config.getConfigurationSection("servers");
		if (section == null) return;
		for (String key : section.getKeys(false)) {
			ConfigurationSection s = section.getConfigurationSection(key);
			if (s == null) continue;
			Material material;
			try { material = Material.valueOf(s.getString("material", "GRASS_BLOCK").toUpperCase()); }
			catch (IllegalArgumentException e) { material = Material.GRASS_BLOCK; }
			servers.put(key, new ServerEntry(key, s.getString("name", key), material, s.getInt("slot", 20),
					s.getString("status", "在线"), s.getString("players", "?"), s.getStringList("lore"),
					s.getStringList("actions")));
		}
	}

	private void loadEvents(FileConfiguration config) {
		events.clear();
		ConfigurationSection section = config.getConfigurationSection("events.list");
		if (section == null) return;
		for (String key : section.getKeys(false)) {
			ConfigurationSection e = section.getConfigurationSection(key);
			if (e == null) continue;
			events.add(new HubEvent(key, e.getString("name", key), e.getString("announcement", ""),
					e.getStringList("commands")));
		}
	}

	private void loadNews(FileConfiguration config) {
		news.clear();
		for (Map<?, ?> raw : config.getMapList("news.entries")) {
			Object title = raw.get("title");
			Object lines = raw.get("lines");
			List<String> lore = new ArrayList<>();
			if (lines instanceof List<?> list) for (Object line : list) lore.add(String.valueOf(line));
			news.add(new NewsEntry(String.valueOf(title == null ? "News" : title), lore));
		}
	}

	private void loadGuide(FileConfiguration config) {
		guide.clear();
		for (Map<?, ?> raw : config.getMapList("guide.steps")) {
			Object title = raw.get("title");
			Object lines = raw.get("lines");
			List<String> lore = new ArrayList<>();
			if (lines instanceof List<?> list) for (Object line : list) lore.add(String.valueOf(line));
			guide.add(new GuideStep(String.valueOf(title == null ? "Step" : title), lore));
		}
	}

	private void registerMenus() {
		register("main", this::buildMain);
		register("quests", this::buildQuests);
		register("daily", this::buildDaily);
		register("profile", this::buildProfile);
		register("settings", this::buildSettings);
		register("shop", this::buildShop);
		register("achievements", this::buildAchievements);
		register("leaderboard", this::buildLeaderboard);
		register("collectibles", this::buildCollectibles);
		register("events", this::buildEvents);
		register("servers", this::buildServers);
		register("news", this::buildNews);
		register("guide", this::buildGuide);
		register("parkour", this::buildParkour);
		register("feedback", this::buildFeedback);
	}

	private void register(String id, java.util.function.Function<Player, org.bukkit.inventory.Inventory> builder) {
		getPlugin().getInventoryManager().addInventory(id, new BuiltinMenu(getPlugin(), builder));
	}

	@Override
	public void onDisable() {
		if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		if (profile.getFirstJoin() == 0) profile.setFirstJoin(System.currentTimeMillis());
		if (!defaultTrail.isBlank() && profile.getSelectedTrail().isBlank() && cosmetics.containsKey(defaultTrail)) {
			profile.setSelectedTrail(defaultTrail);
			profile.getOwnedCosmetics().add(defaultTrail);
		}
		handleReturnReward(player, profile);
		checkDailyReset(profile);
		claimDaily(player, profile, true);
		checkAchievements(player, profile);
		profile.setLastSeen(System.currentTimeMillis());
		applyVisibility(player, profile);
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			if (player.isOnline()) {
				giveLobbyItems(player);
				checkAchievements(player, profile);
			}
		}, 5L);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		PlayerProfile profile = getPlugin().getProfileManager().get(event.getPlayer());
		profile.setLastSeen(System.currentTimeMillis());
		getPlugin().getProfileManager().save(profile);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) return;
		Player player = event.getPlayer();
		if (SparkChessHook.isInActiveGame(player)) return;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (!isLobbyItem(item)) return;
		event.setCancelled(true);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		var data = meta.getPersistentDataContainer();
		if (data.has(statsKey, PersistentDataType.BYTE)) {
			open(player, "profile");
		} else if (data.has(hideKey, PersistentDataType.BYTE)) {
			PlayerProfile profile = getPlugin().getProfileManager().get(player);
			profile.setPlayerVisibility(!profile.isPlayerVisibility());
			applyVisibility(player, profile);
			getPlugin().getProfileManager().save(profile);
			player.getInventory().setItem(8, hideItem(player));
			Text.actionBar(player, profile.isPlayerVisibility() ? "<green>已显示其他玩家" : "<red>已隐藏其他玩家");
		} else {
			open(player, "main");
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.getTo() == null || collectibles.isEmpty()) return;
		Player player = event.getPlayer();
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		for (Collectible collectible : collectibles.values()) {
			if (profile.getCollectibles().contains(collectible.id)) continue;
			if (collectible.location.getWorld() == null || !collectible.location.getWorld().equals(player.getWorld())) continue;
			if (collectible.location.distanceSquared(player.getLocation()) > collectible.radius * collectible.radius) continue;
			profile.getCollectibles().add(collectible.id);
			profile.addCoins(collectible.coins);
			profile.addXp(collectible.xp);
			getPlugin().getProfileManager().save(profile);
			Text.send(player, "<gradient:#57F287:#00AAFF><bold>发现收集品!</bold></gradient> <white>" + collectible.name
					+ "</white> <gray>+<gold>" + collectible.coins + " 金币");
			progress(player, "COLLECTIBLE", 1);
			checkAchievements(player, profile);
		}
	}

	private void tick() {
		minuteCounter++;
		trailCounter++;
		if (minuteCounter >= 60) {
			minuteCounter = 0;
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerProfile profile = getPlugin().getProfileManager().get(player);
				profile.setPlaytimeSeconds(profile.getPlaytimeSeconds() + 60);
				profile.addCoins(coinsPerMinute);
				profile.addXp(xpPerMinute);
				progress(player, "PLAYTIME", 1);
				if (minuteCounter == 0 && profile.getPlaytimeSeconds() % 300 == 0) {
					getPlugin().getProfileManager().save(profile);
				}
			}
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (SparkChessHook.isInActiveGame(player) || isInPvp(player)) {
				removeLobbyItems(player);
			} else if (!hasLobbyItems(player)) {
				giveLobbyItems(player);
			}
		}
		if (trailCounter >= 1) {
			trailCounter = 0;
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerProfile profile = getPlugin().getProfileManager().get(player);
				if (!profile.isParticles() || profile.getSelectedTrail().isBlank()) continue;
				Cosmetic cosmetic = cosmetics.get(profile.getSelectedTrail());
				if (cosmetic == null) continue;
				try {
					Particle particle = Particle.valueOf(cosmetic.particle.toUpperCase(Locale.ROOT));
					player.getWorld().spawnParticle(particle, player.getLocation().add(0, 0.1, 0), 2, 0.15, 0.05, 0.15, 0.01);
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
		if (System.currentTimeMillis() >= nextEventAt) {
			runNextEvent();
			nextEventAt = System.currentTimeMillis() + Math.max(1, eventIntervalMinutes) * 60_000L;
		}
	}

	private void runNextEvent() {
		if (events.isEmpty()) return;
		HubEvent event = events.get(eventIndex % events.size());
		eventIndex++;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!event.announcement.isBlank()) Text.send(player, event.announcement);
		}
		for (String command : event.commands) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%event%", event.name));
		}
	}

	private void claimDaily(Player player, PlayerProfile profile, boolean onJoin) {
		String today = LocalDate.now().toString();
		String last = profile.getLastLoginDate();
		if (today.equals(last)) return;

		LocalDate yesterday = LocalDate.now().minusDays(1);
		int streak = yesterday.toString().equals(last) ? profile.getLoginStreak() + 1 : 1;
		profile.setLoginStreak(streak);
		profile.setLastLoginDate(today);
		int bonus = Math.min(dailyMaxBonus, Math.max(0, (streak - 1) * dailyStreakBonus));
		int coins = dailyBaseCoins + bonus + (isSeasonActive() ? seasonBonus : 0);
		profile.addCoins(coins);
		getPlugin().getProfileManager().save(profile);
		Text.send(player, "<gradient:#57F287:#00AAFF><bold>每日签到</bold></gradient> <gray>连续 <white>"
				+ streak + "</white> 天，获得 <gold>" + coins + " 金币</gold>");
	}

	private void handleReturnReward(Player player, PlayerProfile profile) {
		if (profile.getLastSeen() <= 0) return;
		long days = (System.currentTimeMillis() - profile.getLastSeen()) / 86_400_000L;
		if (days >= returnDays) {
			profile.addCoins(returnCoins);
			profile.addXp(returnXp);
			Text.send(player, "<gradient:#FFD166:#FF8C42><bold>欢迎回来!</bold></gradient> <gray>获得回归奖励 <gold>"
					+ returnCoins + " 金币</gold>、<aqua>" + returnXp + " 经验</aqua>");
		}
	}

	private boolean isSeasonActive() {
		if (!seasonEnabled) return false;
		try {
			String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
			return today.compareTo(seasonStart) >= 0 && today.compareTo(seasonEnd) <= 0;
		} catch (Exception ignored) {
			return false;
		}
	}

	private void checkDailyReset(PlayerProfile profile) {
		String today = LocalDate.now().toString();
		if (!today.equals(profile.getDailyDate())) {
			profile.setDailyDate(today);
			profile.getDailyProgress().clear();
			profile.getDailyClaimed().clear();
		}
		String week = LocalDate.now().getYear() + "-" + LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
		if (!week.equals(profile.getWeeklyKey())) {
			profile.setWeeklyKey(week);
			profile.getWeeklyProgress().clear();
			profile.getWeeklyClaimed().clear();
		}
	}

	public void progress(Player player, String type, int amount) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		checkDailyReset(profile);
		progressMap(player, profile, dailyQuests, profile.getDailyProgress(), profile.getDailyClaimed(), type, amount);
		progressMap(player, profile, weeklyQuests, profile.getWeeklyProgress(), profile.getWeeklyClaimed(), type, amount);
		checkAchievements(player, profile);
		getPlugin().getProfileManager().save(profile);
	}

	private void progressMap(Player player, PlayerProfile profile, Map<String, Quest> quests,
							 Map<String, Integer> progress, java.util.Set<String> claimed, String type, int amount) {
		for (Quest quest : quests.values()) {
			if (!quest.type.equalsIgnoreCase(type)) continue;
			int value = progress.getOrDefault(quest.id, 0) + amount;
			progress.put(quest.id, value);
			if (value >= quest.target && claimed.add(quest.id)) {
				profile.addCoins(quest.coins);
				profile.addXp(quest.xp);
				Text.send(player, "<green>任务完成: <white>" + quest.name + "</white> <gray>+<gold>"
						+ quest.coins + " 金币</gold>");
			}
		}
	}

	private void checkAchievements(Player player, PlayerProfile profile) {
		for (Achievement achievement : achievements.values()) {
			if (profile.getAchievements().contains(achievement.id)) continue;
			if (value(profile, achievement.type) < achievement.target) continue;
			profile.getAchievements().add(achievement.id);
			profile.addCoins(achievement.coins);
			profile.addXp(achievement.xp);
			Text.send(player, "<gradient:#FFD166:#FF8C42><bold>成就解锁!</bold></gradient> <white>"
					+ achievement.name + "</white> <gray>+<gold>" + achievement.coins + " 金币");
		}
	}

	private int value(PlayerProfile profile, String type) {
		return switch (type.toUpperCase(Locale.ROOT)) {
			case "PARKOUR_FINISH" -> profile.getParkourRuns();
			case "COLLECTIBLE" -> profile.getCollectibles().size();
			case "LEVEL" -> profile.getLevel();
			case "PLAYTIME" -> (int) (profile.getPlaytimeSeconds() / 60);
			default -> 0;
		};
	}

	private void applyVisibility(Player player, PlayerProfile profile) {
		for (Player other : Bukkit.getOnlinePlayers()) {
			if (other.equals(player)) continue;
			if (profile.isPlayerVisibility()) player.showPlayer(getPlugin(), other);
			else player.hidePlayer(getPlugin(), other);
		}
	}

	private void giveLobbyItems(Player player) {
		if (SparkChessHook.isInActiveGame(player) || isInPvp(player)) {
			removeLobbyItems(player);
			return;
		}
		player.getInventory().setItem(0, statsItem(player));
		player.getInventory().setItem(Math.max(0, Math.min(8, menuSlot)), menuItem());
		player.getInventory().setItem(8, hideItem(player));
	}

	private ItemStack menuItem() {
		ItemStack item = new ItemStack(menuMaterial);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;
		meta.displayName(Text.parse(menuName));
		meta.lore(Text.parseList(menuLore));
		meta.getPersistentDataContainer().set(menuKey, PersistentDataType.BYTE, (byte) 1);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack statsItem(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		if (meta == null) return item;
		meta.setOwningPlayer(player);
		meta.displayName(Text.parse("<light_purple><bold>玩家统计</bold>"));
		meta.lore(Text.parseList(List.of(
				"<gray>好友: <white>" + socialFriends,
				"<gray>公会: <white>" + socialGuild,
				"<gray>等级: <white>" + profile.getLevel(),
				"<gray>金币: <gold>" + profile.getCoins(),
				"<dark_gray>点击查看完整资料")));
		meta.getPersistentDataContainer().set(statsKey, PersistentDataType.BYTE, (byte) 1);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack hideItem(Player player) {
		boolean visible = getPlugin().getProfileManager().get(player).isPlayerVisibility();
		ItemStack item = new ItemStack(visible ? Material.LIME_DYE : Material.GRAY_DYE);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;
		meta.displayName(Text.parse(visible
				? "<green><bold>玩家可见: 开</bold>"
				: "<red><bold>玩家可见: 关</bold>"));
		meta.lore(Text.parseList(List.of("<gray>点击切换其他玩家是否可见")));
		meta.getPersistentDataContainer().set(hideKey, PersistentDataType.BYTE, (byte) 1);
		item.setItemMeta(meta);
		return item;
	}

	private boolean isInPvp(Player player) {
		var pvp = (top.sparkpixel.hub.module.modules.player.PvPMode) getPlugin().getModuleManager().getModule(ModuleType.PVP_MODE);
		return pvp != null && pvp.isInPvp(player.getUniqueId());
	}

	private void removeLobbyItems(Player player) {
		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			ItemStack item = player.getInventory().getItem(slot);
			if (isLobbyItem(item)) player.getInventory().setItem(slot, null);
		}
	}

	private boolean hasLobbyItems(Player player) {
		boolean menu = false, stats = false, hide = false;
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) continue;
			ItemMeta meta = item.getItemMeta();
			if (meta == null) continue;
			var data = meta.getPersistentDataContainer();
			if (data.has(menuKey, PersistentDataType.BYTE)) menu = true;
			if (data.has(statsKey, PersistentDataType.BYTE)) stats = true;
			if (data.has(hideKey, PersistentDataType.BYTE)) hide = true;
		}
		return menu && stats && hide;
	}

	public boolean isLobbyItem(ItemStack item) {
		if (item == null) return false;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return false;
		var data = meta.getPersistentDataContainer();
		return data.has(menuKey, PersistentDataType.BYTE)
				|| data.has(statsKey, PersistentDataType.BYTE)
				|| data.has(hideKey, PersistentDataType.BYTE);
	}

	public boolean isMenuItem(ItemStack item) {
		if (item == null) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(menuKey, PersistentDataType.BYTE);
	}

	public void open(Player player, String id) {
		if (getPlugin().getInventoryManager().getInventory(id) == null) {
			Text.send(player, "<red>菜单不存在: " + id);
			return;
		}
		getPlugin().getInventoryManager().getInventory(id).openInventory(player);
	}

	private InventoryBuilder base(String title) {
		return base(title, 6);
	}

	private InventoryBuilder base(String title, int rows) {
		InventoryBuilder builder = new InventoryBuilder(rows, Text.parse(title));
		InventoryItem filler = button(Material.GRAY_STAINED_GLASS_PANE, " ", List.of(), null);
		int size = rows * 9;
		for (int slot = 0; slot < size; slot++) {
			if (slot < 9 || slot >= size - 9 || slot % 9 == 0 || slot % 9 == 8) builder.setItem(slot, filler);
		}
		return builder;
	}

	private InventoryItem button(Material material, String name, List<String> lore, Consumer<Player> action) {
		ItemStack stack = new ItemStack(material);
		ItemMeta meta = stack.getItemMeta();
		if (meta != null) {
			meta.displayName(Text.parse(name));
			meta.lore(Text.parseList(lore));
			stack.setItemMeta(meta);
		}
		InventoryItem item = new InventoryItem(stack);
		if (action != null) item.addClickAction(action::accept);
		return item;
	}

	private org.bukkit.inventory.Inventory buildMain(Player player) {
		InventoryBuilder menu = base("<gradient:#9863E7:#545eb6><bold>Sparkpixel Hub</bold></gradient>");
		menu.setItem(4, button(Material.PLAYER_HEAD, "<gradient:#9863E7:#545eb6><bold>SP 菜单</bold></gradient>",
				List.of("<gray>Sparkpixel Hub", "<dark_gray>大厅总菜单"), null));
		menu.setItem(20, button(Material.COMPASS, "<aqua><bold>服务器选择</bold>", List.of("<gray>查看服务器状态并快速加入"), p -> open(p, servers.isEmpty() ? "serverselector" : "servers")));
		menu.setItem(21, button(Material.WRITABLE_BOOK, "<yellow><bold>任务</bold>", List.of("<gray>每日与每周目标"), p -> open(p, "quests")));
		menu.setItem(22, button(Material.CLOCK, "<gold><bold>每日签到</bold>", List.of("<gray>连续签到领取金币"), p -> open(p, "daily")));
		menu.setItem(23, button(Material.FEATHER, "<green><bold>跑酷</bold>", List.of("<gray>挑战个人最佳成绩"), p -> open(p, "parkour")));
		menu.setItem(24, button(Material.PLAYER_HEAD, "<light_purple><bold>个人资料</bold>", List.of("<gray>等级、金币与统计"), p -> open(p, "profile")));
		menu.setItem(29, button(Material.COMPARATOR, "<gray><bold>设置</bold>", List.of("<gray>特效、公告与可见性"), p -> open(p, "settings")));
		menu.setItem(30, button(Material.EMERALD, "<green><bold>商店</bold>", List.of("<gray>购买外观与粒子"), p -> open(p, "shop")));
		menu.setItem(31, button(Material.NETHER_STAR, "<gold><bold>成就</bold>", List.of("<gray>查看已解锁成就"), p -> open(p, "achievements")));
		menu.setItem(32, button(Material.GOLD_INGOT, "<yellow><bold>排行榜</bold>", List.of("<gray>金币与跑酷排名"), p -> open(p, "leaderboard")));
		menu.setItem(33, button(Material.CHEST, "<gold><bold>收集品</bold>", List.of("<gray>寻找大厅隐藏收集品"), p -> open(p, "collectibles")));
		menu.setItem(38, button(Material.BELL, "<red><bold>活动</bold>", List.of("<gray>查看当前与下一场活动"), p -> open(p, "events")));
		menu.setItem(39, button(Material.BOOK, "<blue><bold>新闻</bold>", List.of("<gray>服务器更新与公告"), p -> open(p, "news")));
		menu.setItem(25, button(Material.BOW, "<light_purple><bold>传送弓</bold>", List.of("<gray>获得一把大厅传送弓"), p -> {
			var bow = (top.sparkpixel.hub.module.modules.player.TeleportationBow) getPlugin().getModuleManager().getModule(ModuleType.TELEPORTATION_BOW);
			if (bow != null) bow.giveBow(p);
		}));
		menu.setItem(34, button(Material.SLIME_BALL, "<green><bold>蹦床</bold>", List.of("<gray>VIP 可放置大厅蹦床"), p -> {
			var trampoline = (top.sparkpixel.hub.module.modules.world.Trampoline) getPlugin().getModuleManager().getModule(ModuleType.TRAMPOLINE);
			if (trampoline == null) { Text.send(p, "<red>蹦床未启用。"); return; }
			if (!trampoline.canUse(p)) { Text.send(p, "<red>只有 VIP 可以使用蹦床。"); return; }
			trampoline.place(p);
			p.closeInventory();
		}));
		menu.setItem(40, button(Material.MAP, "<green><bold>新手指南</bold>", List.of("<gray>第一次来？从这里开始"), p -> open(p, "guide")));
		menu.setItem(41, button(Material.PAPER, "<gray><bold>反馈</bold>", List.of("<gray>使用 /sphub feedback <text>"), p -> open(p, "feedback")));
		menu.setItem(49, button(Material.BARRIER, "<red>关闭", List.of(), Player::closeInventory));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildQuests(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<yellow><bold>每日 / 每周任务</bold>");
		int slot = 10;
		for (Quest quest : dailyQuests.values()) {
			int value = profile.getDailyProgress().getOrDefault(quest.id, 0);
			menu.setItem(slot++, button(Material.WRITABLE_BOOK, "<yellow>" + quest.name,
					List.of("<gray>进度: <white>" + value + "/" + quest.target,
							"<gray>奖励: <gold>" + quest.coins + " 金币", "<aqua>" + quest.xp + " 经验"), p -> open(p, "quests")));
		}
		slot = 28;
		for (Quest quest : weeklyQuests.values()) {
			int value = profile.getWeeklyProgress().getOrDefault(quest.id, 0);
			menu.setItem(slot++, button(Material.BOOK, "<gold>" + quest.name,
					List.of("<gray>进度: <white>" + value + "/" + quest.target,
							"<gray>奖励: <gold>" + quest.coins + " 金币", "<aqua>" + quest.xp + " 经验"), p -> open(p, "quests")));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildDaily(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<gold><bold>每日签到</bold>");
		menu.setItem(22, button(Material.CLOCK, "<gold><bold>连续 " + profile.getLoginStreak() + " 天</bold>",
				List.of("<gray>上次签到: <white>" + (profile.getLastLoginDate().isBlank() ? "从未" : profile.getLastLoginDate()),
						"<gray>基础奖励: <gold>" + dailyBaseCoins + " 金币",
						"<gray>连续奖励: <gold>+" + Math.min(dailyMaxBonus, Math.max(0, (profile.getLoginStreak() - 1) * dailyStreakBonus)) + " 金币"),
				p -> {
					claimDaily(p, getPlugin().getProfileManager().get(p), false);
					open(p, "daily");
				}));
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildProfile(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<light_purple><bold>个人资料</bold>");
		menu.setItem(22, button(Material.PLAYER_HEAD, "<light_purple><bold>" + player.getName() + "</bold>",
				List.of("<gray>等级: <white>" + profile.getLevel(), "<gray>经验: <white>" + profile.getXp(),
						"<gray>金币: <gold>" + profile.getCoins(), "<gray>游玩时长: <white>" + (profile.getPlaytimeSeconds() / 3600) + " 小时",
						"<gray>跑酷最佳: <white>" + (profile.getParkourBest() == 0 ? "无" : top.sparkpixel.hub.module.modules.parkour.ParkourManager.format(profile.getParkourBest())),
						"<gray>成就: <white>" + profile.getAchievements().size(), "<gray>收集品: <white>" + profile.getCollectibles().size()), null));
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildSettings(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<gray><bold>设置</bold>");
		menu.setItem(20, toggle("加入特效", profile.isJoinEffects(), p -> { profile.setJoinEffects(!profile.isJoinEffects()); saveAndOpen(p, "settings"); }));
		menu.setItem(21, toggle("公告", profile.isAnnouncements(), p -> { profile.setAnnouncements(!profile.isAnnouncements()); saveAndOpen(p, "settings"); }));
		menu.setItem(22, toggle("粒子效果", profile.isParticles(), p -> { profile.setParticles(!profile.isParticles()); saveAndOpen(p, "settings"); }));
		menu.setItem(23, toggle("玩家可见", profile.isPlayerVisibility(), p -> { profile.setPlayerVisibility(!profile.isPlayerVisibility()); applyVisibility(p, profile); saveAndOpen(p, "settings"); }));
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private InventoryItem toggle(String name, boolean enabled, Consumer<Player> action) {
		return button(enabled ? Material.LIME_DYE : Material.GRAY_DYE,
				(enabled ? "<green>" : "<red>") + name + " <gray>(" + (enabled ? "开" : "关") + ")",
				List.of("<gray>点击切换"), action);
	}

	private void saveAndOpen(Player player, String menu) {
		getPlugin().getProfileManager().save(getPlugin().getProfileManager().get(player));
		open(player, menu);
	}

	private org.bukkit.inventory.Inventory buildShop(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<green><bold>外观商店</bold>");
		int slot = 10;
		for (Cosmetic cosmetic : cosmetics.values()) {
			boolean owned = profile.getOwnedCosmetics().contains(cosmetic.id);
			boolean selected = cosmetic.id.equals(profile.getSelectedTrail());
			List<String> lore = new ArrayList<>(cosmetic.lore);
			lore.add("<gray>价格: <gold>" + cosmetic.price + " 金币");
			lore.add(selected ? "<green>已装备" : owned ? "<yellow>点击装备" : "<yellow>点击购买");
			menu.setItem(slot++, button(cosmetic.material, "<light_purple>" + cosmetic.name, lore, p -> {
				PlayerProfile profile2 = getPlugin().getProfileManager().get(p);
				if (profile2.getOwnedCosmetics().contains(cosmetic.id)) {
					profile2.setSelectedTrail(selected ? "" : cosmetic.id);
				} else if (profile2.getCoins() >= cosmetic.price) {
					profile2.addCoins(-cosmetic.price);
					profile2.getOwnedCosmetics().add(cosmetic.id);
					profile2.setSelectedTrail(cosmetic.id);
					Text.send(p, "<green>购买成功: <white>" + cosmetic.name);
				} else {
					Text.send(p, "<red>金币不足。");
				}
				saveAndOpen(p, "shop");
			}));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildAchievements(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<gold><bold>成就</bold>");
		int slot = 10;
		for (Achievement achievement : achievements.values()) {
			boolean unlocked = profile.getAchievements().contains(achievement.id);
			List<String> lore = new ArrayList<>(achievement.description);
			lore.add(unlocked ? "<green>已解锁" : "<gray>进度: " + value(profile, achievement.type) + "/" + achievement.target);
			menu.setItem(slot++, button(unlocked ? Material.NETHER_STAR : Material.COAL,
					(unlocked ? "<gold>" : "<dark_gray>") + achievement.name, lore, null));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildLeaderboard(Player player) {
		InventoryBuilder menu = base("<yellow><bold>排行榜</bold>");
		List<PlayerProfile> profiles = getPlugin().getProfileManager().all();
		profiles.sort(Comparator.comparingLong(PlayerProfile::getCoins).reversed());
		int slot = 10;
		for (int i = 0; i < Math.min(10, profiles.size()); i++) {
			PlayerProfile profile = profiles.get(i);
			menu.setItem(slot++, button(Material.GOLD_INGOT, "<gold>#" + (i + 1) + " " + profile.getName(),
					List.of("<gray>金币: <white>" + profile.getCoins(), "<gray>等级: <white>" + profile.getLevel()), null));
		}
		List<PlayerProfile> parkour = new ArrayList<>(profiles);
		parkour.removeIf(p -> p.getParkourBest() <= 0);
		parkour.sort(Comparator.comparingLong(PlayerProfile::getParkourBest));
		int slot2 = 28;
		for (int i = 0; i < Math.min(10, parkour.size()); i++) {
			PlayerProfile profile = parkour.get(i);
			menu.setItem(slot2++, button(Material.FEATHER, "<green>#" + (i + 1) + " " + profile.getName(),
					List.of("<gray>最佳: <white>" + top.sparkpixel.hub.module.modules.parkour.ParkourManager.format(profile.getParkourBest())), null));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildCollectibles(Player player) {
		PlayerProfile profile = getPlugin().getProfileManager().get(player);
		InventoryBuilder menu = base("<gold><bold>收集品</bold>");
		int slot = 10;
		for (Collectible collectible : collectibles.values()) {
			boolean found = profile.getCollectibles().contains(collectible.id);
			menu.setItem(slot++, button(found ? Material.CHEST : Material.BARREL,
					(found ? "<green>" : "<gray>") + collectible.name,
					List.of(found ? "<green>已收集" : "<gray>尚未发现", "<gray>位置: <white>" + collectible.location.getWorld().getName()), null));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildServers(Player player) {
		InventoryBuilder menu = base("<aqua><bold>服务器状态</bold>");
		for (ServerEntry server : servers.values()) {
			List<String> lore = new ArrayList<>(server.lore);
			lore.add("<gray>状态: <white>" + server.status);
			lore.add("<gray>在线: <white>" + server.players);
			menu.setItem(server.slot, button(server.material, "<aqua>" + server.name, lore,
					p -> getPlugin().getActionManager().executeActions(p, server.actions)));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildEvents(Player player) {
		InventoryBuilder menu = base("<red><bold>活动</bold>");
		long seconds = Math.max(0, (nextEventAt - System.currentTimeMillis()) / 1000);
		menu.setItem(22, button(Material.BELL, "<red><bold>活动轮换</bold>",
				List.of("<gray>下一场: <white>" + formatSeconds(seconds), "<gray>当前赛季: <white>"
						+ (isSeasonActive() ? seasonName : "无")), null));
		int slot = 28;
		for (HubEvent event : events) {
			menu.setItem(slot++, button(Material.FIREWORK_ROCKET, "<yellow>" + event.name,
					List.of("<gray>由活动轮换自动触发"), null));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildNews(Player player) {
		InventoryBuilder menu = base("<blue><bold>新闻与更新</bold>");
		int slot = 10;
		for (NewsEntry entry : news) {
			menu.setItem(slot++, button(Material.BOOK, "<aqua>" + entry.title, entry.lines, null));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildGuide(Player player) {
		InventoryBuilder menu = base("<green><bold>新手指南</bold>");
		int slot = 10;
		for (GuideStep step : guide) {
			menu.setItem(slot++, button(Material.MAP, "<green>" + step.title, step.lines,
					p -> open(p, "quests")));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildParkour(Player player) {
		var parkour = (top.sparkpixel.hub.module.modules.parkour.ParkourManager)
				getPlugin().getModuleManager().getModule(ModuleType.PARKOUR);
		InventoryBuilder menu = base("<green><bold>跑酷</bold>");
		if (parkour == null) {
			menu.setItem(22, button(Material.BARRIER, "<red>跑酷未启用", List.of(), null));
		} else {
			menu.setItem(20, button(Material.FEATHER, "<green><bold>开始跑酷</bold>",
					List.of("<gray>个人最佳: <white>" + parkour.best(player)), p -> {
						parkour.start(p);
						p.closeInventory();
					}));
			menu.setItem(21, button(Material.RED_DYE, "<red><bold>退出跑酷</bold>", List.of(), p -> {
				parkour.stop(p);
				p.closeInventory();
			}));
		}
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private org.bukkit.inventory.Inventory buildFeedback(Player player) {
		InventoryBuilder menu = base("<gray><bold>反馈</bold>");
		menu.setItem(22, button(Material.PAPER, "<gray><bold>提交反馈</bold>",
				List.of("<gray>在聊天栏输入:", "<white>/sphub feedback <你的建议>"), null));
		menu.setItem(49, button(Material.BARRIER, "<red>返回", List.of(), p -> open(p, "main")));
		return menu.getInventory();
	}

	private String formatSeconds(long seconds) {
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}

	private record Quest(String id, String name, String type, int target, int coins, int xp, boolean daily) {}
	private record Achievement(String id, String name, List<String> description, String type, int target, int coins, int xp) {}
	private record Collectible(String id, String name, Location location, double radius, int coins, int xp) {}
	private record Cosmetic(String id, String name, List<String> lore, Material material, int price, String particle) {}
	private record HubEvent(String id, String name, String announcement, List<String> commands) {}
	private record NewsEntry(String title, List<String> lines) {}
	private record GuideStep(String title, List<String> lines) {}
	private record ServerEntry(String id, String name, Material material, int slot, String status, String players,
							   List<String> lore, List<String> actions) {}
}