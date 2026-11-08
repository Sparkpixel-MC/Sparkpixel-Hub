package top.sparkpixel.hub.profile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerProfile {

	private final UUID uuid;
	private String name;
	private long coins;
	private long xp;
	private int level = 1;
	private long firstJoin;
	private long lastSeen;
	private long playtimeSeconds;
	private int loginStreak;
	private String lastLoginDate = "";
	private boolean joinEffects = true;
	private boolean announcements = true;
	private boolean particles = true;
	private boolean playerVisibility = true;
	private String dailyDate = "";
	private String weeklyKey = "";
	private final Map<String, Integer> dailyProgress = new HashMap<>();
	private final Map<String, Integer> weeklyProgress = new HashMap<>();
	private final Set<String> dailyClaimed = new HashSet<>();
	private final Set<String> weeklyClaimed = new HashSet<>();
	private final Set<String> achievements = new HashSet<>();
	private final Set<String> collectibles = new HashSet<>();
	private final Set<String> ownedCosmetics = new HashSet<>();
	private String selectedTrail = "";
	private long parkourBest;
	private int parkourRuns;

	public PlayerProfile(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
		this.firstJoin = System.currentTimeMillis();
	}

	public UUID getUuid() { return uuid; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public long getCoins() { return coins; }
	public void setCoins(long coins) { this.coins = Math.max(0, coins); }
	public void addCoins(long amount) { this.coins = Math.max(0, this.coins + amount); }

	public long getXp() { return xp; }
	public void setXp(long xp) { this.xp = Math.max(0, xp); recalculateLevel(); }
	public void addXp(long amount) { this.xp = Math.max(0, this.xp + amount); recalculateLevel(); }
	private void recalculateLevel() { this.level = Math.max(1, (int) (this.xp / 100) + 1); }
	public int getLevel() { return level; }
	public void setLevel(int level) { this.level = Math.max(1, level); }

	public long getFirstJoin() { return firstJoin; }
	public void setFirstJoin(long firstJoin) { this.firstJoin = firstJoin; }
	public long getLastSeen() { return lastSeen; }
	public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
	public long getPlaytimeSeconds() { return playtimeSeconds; }
	public void setPlaytimeSeconds(long playtimeSeconds) { this.playtimeSeconds = playtimeSeconds; }

	public int getLoginStreak() { return loginStreak; }
	public void setLoginStreak(int loginStreak) { this.loginStreak = Math.max(0, loginStreak); }
	public String getLastLoginDate() { return lastLoginDate; }
	public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }

	public boolean isJoinEffects() { return joinEffects; }
	public void setJoinEffects(boolean joinEffects) { this.joinEffects = joinEffects; }
	public boolean isAnnouncements() { return announcements; }
	public void setAnnouncements(boolean announcements) { this.announcements = announcements; }
	public boolean isParticles() { return particles; }
	public void setParticles(boolean particles) { this.particles = particles; }
	public boolean isPlayerVisibility() { return playerVisibility; }
	public void setPlayerVisibility(boolean playerVisibility) { this.playerVisibility = playerVisibility; }

	public String getDailyDate() { return dailyDate; }
	public void setDailyDate(String dailyDate) { this.dailyDate = dailyDate; }
	public String getWeeklyKey() { return weeklyKey; }
	public void setWeeklyKey(String weeklyKey) { this.weeklyKey = weeklyKey; }
	public Map<String, Integer> getDailyProgress() { return dailyProgress; }
	public Map<String, Integer> getWeeklyProgress() { return weeklyProgress; }
	public Set<String> getDailyClaimed() { return dailyClaimed; }
	public Set<String> getWeeklyClaimed() { return weeklyClaimed; }

	public Set<String> getAchievements() { return achievements; }
	public Set<String> getCollectibles() { return collectibles; }
	public Set<String> getOwnedCosmetics() { return ownedCosmetics; }
	public String getSelectedTrail() { return selectedTrail; }
	public void setSelectedTrail(String selectedTrail) { this.selectedTrail = selectedTrail == null ? "" : selectedTrail; }

	public long getParkourBest() { return parkourBest; }
	public void setParkourBest(long parkourBest) { this.parkourBest = parkourBest; }
	public int getParkourRuns() { return parkourRuns; }
	public void setParkourRuns(int parkourRuns) { this.parkourRuns = parkourRuns; }
}