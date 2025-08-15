
package lol.siwoo.faramcpracticecore.aa.siwwa_pvp_1;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.DuelEndEvent;
import ga.strikepractice.events.DuelStartEvent;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import ga.strikepractice.fights.Fight;
import ga.strikepractice.fights.duel.Duel;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataLogger implements Listener {

    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api;
    private final Gson gson;
    private final Map<String, MatchSession> activeSessions;
    private final Map<UUID, PlayerTracker> playerTrackers;

    public DataLogger(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaredClass() == java.io.File.class;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        this.activeSessions = new ConcurrentHashMap<>();
        this.playerTrackers = new ConcurrentHashMap<>();

        // Start periodic data collection task
        DataLogger.setPlugin(plugin);
        startPeriodicDataCollection();
    }

    private void startPeriodicDataCollection() {
        new BukkitRunnable() {
            @Override
            public void run() {
                collectPeriodicData();
            }
        }.runTaskTimer(plugin, 0L, 10L); // Collect data every 0.5 seconds
    }

    private void collectPeriodicData() {
        for (MatchSession session : activeSessions.values()) {
            if (session.isActive()) {
                session.captureGameState();
            }
        }
    }

    @EventHandler
    public void onFightStart(DuelStartEvent e) {
        String matchId = generateMatchId();
        MatchSession session = new MatchSession(matchId, e.getFight());
        activeSessions.put(matchId, session);

        // Initialize player trackers
        for (Player player : e.getFight().getPlayersInFight()) {
            playerTrackers.put(player.getUniqueId(), new PlayerTracker(player, matchId));
        }

        plugin.getLogger().info("[siwwa-pvp-1] Started data logging for match: " + matchId);
    }

    @EventHandler
    public void onFightEnd(DuelEndEvent event) {
        String matchId = findMatchId(event.getFight().getPlayersInFight());
        if (matchId != null) {
            MatchSession session = activeSessions.get(matchId);
            if (session != null) {
                session.endMatch(event);
                saveMatchData(session);
                activeSessions.remove(matchId);
            }

            // Clean up player trackers
            for (Player player : event.getFight().getPlayersInFight()) {
                playerTrackers.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        PlayerTracker tracker = playerTrackers.get(event.getPlayer().getUniqueId());
        if (tracker != null && tracker.isTracking()) {
            tracker.recordMovement(event.getFrom(), event.getTo());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerTracker tracker = playerTrackers.get(player.getUniqueId());
            if (tracker != null && tracker.isTracking()) {
                tracker.recordDamage(event);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            PlayerTracker attackerTracker = playerTrackers.get(attacker.getUniqueId());
            PlayerTracker victimTracker = playerTrackers.get(victim.getUniqueId());

            if (attackerTracker != null && attackerTracker.isTracking()) {
                attackerTracker.recordAttack(event);
            }

            if (victimTracker != null && victimTracker.isTracking()) {
                victimTracker.recordBeingAttacked(event);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerTracker tracker = playerTrackers.get(event.getEntity().getUniqueId());
        if (tracker != null && tracker.isTracking()) {
            tracker.recordDeath(event);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        PlayerTracker tracker = playerTrackers.get(event.getPlayer().getUniqueId());
        if (tracker != null && tracker.isTracking()) {
            tracker.recordBlockPlace(event);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerTracker tracker = playerTrackers.get(event.getPlayer().getUniqueId());
        if (tracker != null && tracker.isTracking()) {
            tracker.recordBlockBreak(event);
        }
    }

    private String generateMatchId() {
        return "match_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String findMatchId(List<Player> players) {
        for (MatchSession session : activeSessions.values()) {
            if (session.containsPlayers(players)) {
                return session.getMatchId();
            }
        }
        return null;
    }

    private void saveMatchData(MatchSession session) {
        try {
            File dataDir = new File(plugin.getDataFolder(), "ai_training_data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            File matchFile = new File(dataDir, session.getMatchId() + ".json");
            try (FileWriter writer = new FileWriter(matchFile)) {
                gson.toJson(session.getMatchData(), writer);
            }

            plugin.getLogger().info("[siwwa-pvp-1] Saved match data: " + matchFile.getName());

        } catch (IOException e) {
            plugin.getLogger().severe("[siwwa-pvp-1] Failed to save match data: " + e.getMessage());
        }
    }

    // Inner classes for data structures
    public static class MatchSession {
        private final String matchId;
        private final String gameMode;
        private final List<PlayerData> players;
        private final List<GameState> gameStates;
        private final long startTime;
        private long endTime;
        private boolean active;
        private String winner;
        private final Map<String, Object> matchMetadata;

        public MatchSession(String matchId, Duel fight) {
            this.matchId = matchId;
            this.gameMode = fight.getKit().getName();
            this.players = new ArrayList<>();
            this.gameStates = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
            this.active = true;
            this.matchMetadata = new HashMap<>();

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : fight.getPlayersInFight()) {
                        players.add(new PlayerData(player, fight.getTeammates(player)));
                    }

                    // Store match metadata
                    matchMetadata.put("arena", fight.getArena().getName());
                    matchMetadata.put("playerCount", fight.getPlayersInFight().

                            size());
                }
            }.runTaskLater(pluginn, 5L);
        }

        public void captureGameState() {
            GameState state = new GameState(System.currentTimeMillis() - startTime);

            for (PlayerData playerData : players) {
                Player player = playerData.getPlayer();
                if (player != null && player.isOnline()) {
                    PlayerState playerState = new PlayerState(
                            player.getUniqueId(),
                            player.getLocation(),
                            player.getHealth(),
                            player.getFoodLevel(),
                            Arrays.asList(player.getInventory().getContents()),
                            player.getVelocity(),
                            player.isOnGround(),
                            player.isSneaking(),
                            player.isSprinting(),
                            player.isBlocking()
                    );
                    state.addPlayerState(playerState);
                }
            }

            gameStates.add(state);
        }

        public void endMatch(DuelEndEvent e) {
            this.active = false;
            this.endTime = System.currentTimeMillis();

            // Determine winner
            if (e.getWinner() != null) {
                this.winner = e.getWinner().getName();
            }
        }

        public MatchData getMatchData() {
            return new MatchData(
                    matchId,
                    gameMode,
                    startTime,
                    endTime,
                    winner,
                    players,
                    gameStates,
                    matchMetadata
            );
        }

        public boolean containsPlayers(List<Player> checkPlayers) {
            Set<UUID> sessionPlayerIds = new HashSet<>();
            for (PlayerData pd : players) {
                if (pd.getPlayer() != null) {
                    sessionPlayerIds.add(pd.getPlayer().getUniqueId());
                }
            }

            for (Player player : checkPlayers) {
                if (sessionPlayerIds.contains(player.getUniqueId())) {
                    return true;
                }
            }
            return false;
        }

        // Getters
        public String getMatchId() { return matchId; }
        public boolean isActive() { return active; }
    }

    public static class PlayerTracker {
        private final UUID playerId;
        private final String playerName;
        private final String matchId;
        private final List<PlayerAction> actions;
        private final List<LocationPoint> movementPath;
        private final PlayerStats stats;
        private boolean tracking;

        public PlayerTracker(Player player, String matchId) {
            this.playerId = player.getUniqueId();
            this.playerName = player.getName();
            this.matchId = matchId;
            this.actions = new ArrayList<>();
            this.movementPath = new ArrayList<>();
            this.stats = new PlayerStats();
            this.tracking = true;
        }

        public void recordMovement(Location from, Location to) {
            if (!tracking) return;

            if (from.distanceSquared(to) > 0.01) { // Only record significant movement
                movementPath.add(new LocationPoint(to, System.currentTimeMillis()));

                // Calculate movement metrics
                double distance = from.distance(to);
                stats.totalDistanceMoved += distance;

                if (distance > 0.1) {
                    double speed = distance * 20; // Convert to blocks per second
                    stats.averageSpeed = (stats.averageSpeed * stats.movementCount + speed) / (stats.movementCount + 1);
                    stats.movementCount++;
                }
            }
        }

        public void recordAttack(EntityDamageByEntityEvent event) {
            if (!tracking) return;

            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            PlayerAction action = new PlayerAction(
                    "ATTACK",
                    System.currentTimeMillis(),
                    attacker.getLocation(),
                    createMap(
                            "victim", victim.getName(),
                            "damage", event.getFinalDamage(),
                            "weapon", attacker.getItemInHand().getType().toString(),
                            "distance", attacker.getLocation().distance(victim.getLocation())
                    )
            );

            actions.add(action);
            stats.attacksMade++;
            stats.totalDamageDealt += event.getFinalDamage();
        }

        public void recordBeingAttacked(EntityDamageByEntityEvent event) {
            if (!tracking) return;

            stats.damageReceived += event.getFinalDamage();
        }

        public void recordDamage(EntityDamageEvent event) {
            if (!tracking) return;

            PlayerAction action = new PlayerAction(
                    "DAMAGE_RECEIVED",
                    System.currentTimeMillis(),
                    ((Player) event.getEntity()).getLocation(),
                    createMap(
                            "cause", event.getCause().toString(),
                            "damage", event.getFinalDamage()
                    )
            );

            actions.add(action);
        }

        public void recordDeath(PlayerDeathEvent event) {
            if (!tracking) return;

            PlayerAction action = new PlayerAction(
                    "DEATH",
                    System.currentTimeMillis(),
                    event.getEntity().getLocation(),
                    createMap(
                            "killer", event.getEntity().getKiller() != null ?
                                    event.getEntity().getKiller().getName() : "UNKNOWN",
                            "deathMessage", event.getDeathMessage()
                    )
            );

            actions.add(action);
            stats.deaths++;
        }

        public void recordBlockPlace(BlockPlaceEvent event) {
            if (!tracking) return;

            PlayerAction action = new PlayerAction(
                    "BLOCK_PLACE",
                    System.currentTimeMillis(),
                    event.getBlock().getLocation(),
                    createMap(
                            "material", event.getBlock().getType().toString(),
                            "playerLocation", event.getPlayer().getLocation()
                    )
            );

            actions.add(action);
            stats.blocksPlaced++;
        }

        public void recordBlockBreak(BlockBreakEvent event) {
            if (!tracking) return;

            PlayerAction action = new PlayerAction(
                    "BLOCK_BREAK",
                    System.currentTimeMillis(),
                    event.getBlock().getLocation(),
                    createMap(
                            "material", event.getBlock().getType().toString(),
                            "playerLocation", event.getPlayer().getLocation()
                    )
            );

            actions.add(action);
            stats.blocksBroken++;
        }

        public boolean isTracking() { return tracking; }
        public void stopTracking() { this.tracking = false; }
    }

    // Data structure classes
    public static class MatchData {
        private final String matchId;
        private final String gameMode;
        private final long startTime;
        private final long endTime;
        private final String winner;
        private final List<PlayerData> players;
        private final List<GameState> gameStates;
        private final Map<String, Object> metadata;

        public MatchData(String matchId, String gameMode, long startTime, long endTime,
                         String winner, List<PlayerData> players, List<GameState> gameStates,
                         Map<String, Object> metadata) {
            this.matchId = matchId;
            this.gameMode = gameMode;
            this.startTime = startTime;
            this.endTime = endTime;
            this.winner = winner;
            this.players = players;
            this.gameStates = gameStates;
            this.metadata = metadata;
        }
    }

    public static class PlayerData {
        private final String name;
        private final UUID uuid;
        private final List<String> teammates;
        private final long joinTime;
        private final FaraMCPracticeCore pluginInstance;

        public PlayerData(Player player, List<String> teammates) {
            this.name = player.getName();
            this.uuid = player.getUniqueId();
            this.teammates = teammates != null ? new ArrayList<>(teammates) : new ArrayList<>();
            this.joinTime = System.currentTimeMillis();
            this.pluginInstance = (FaraMCPracticeCore) Bukkit.getPluginManager().getPlugin("FaraMCPracticeCore");
        }

        public Player getPlayer() {
            return pluginInstance.getServer().getPlayer(uuid);
        }
    }

    public static class GameState {
        private final long timestamp;
        private final List<PlayerState> playerStates;

        public GameState(long timestamp) {
            this.timestamp = timestamp;
            this.playerStates = new ArrayList<>();
        }

        public void addPlayerState(PlayerState state) {
            playerStates.add(state);
        }
    }

    public static class PlayerState {
        private final UUID playerId;
        private final LocationData location;
        private final double health;
        private final int hunger;
        private final List<ItemStack> inventory;
        private final VelocityData velocity;
        private final boolean onGround;
        private final boolean sneaking;
        private final boolean sprinting;
        private final boolean blocking;

        public PlayerState(UUID playerId, Location location, double health, int hunger,
                           List<ItemStack> inventory, org.bukkit.util.Vector velocity,
                           boolean onGround, boolean sneaking, boolean sprinting, boolean blocking) {
            this.playerId = playerId;
            this.location = new LocationData(location);
            this.health = health;
            this.hunger = hunger;
            this.inventory = inventory;
            this.velocity = new VelocityData(velocity);
            this.onGround = onGround;
            this.sneaking = sneaking;
            this.sprinting = sprinting;
            this.blocking = blocking;
        }
    }

    public static class PlayerAction {
        private final String type;
        private final long timestamp;
        private final LocationData location;
        private final Map<String, Object> data;

        public PlayerAction(String type, long timestamp, Location location, Map<String, Object> data) {
            this.type = type;
            this.timestamp = timestamp;
            this.location = new LocationData(location);
            this.data = data;
        }
    }

    public static class PlayerStats {
        public int attacksMade = 0;
        public int deaths = 0;
        public int blocksPlaced = 0;
        public int blocksBroken = 0;
        public double totalDamageDealt = 0.0;
        public double damageReceived = 0.0;
        public double totalDistanceMoved = 0.0;
        public double averageSpeed = 0.0;
        public int movementCount = 0;
    }

    public static class LocationData {
        private final double x, y, z;
        private final float yaw, pitch;
        private final String world;

        public LocationData(Location location) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.yaw = location.getYaw();
            this.pitch = location.getPitch();
            this.world = location.getWorld().getName();
        }
    }

    public static class VelocityData {
        private final double x, y, z;

        public VelocityData(org.bukkit.util.Vector velocity) {
            this.x = velocity.getX();
            this.y = velocity.getY();
            this.z = velocity.getZ();
        }
    }

    public static class LocationPoint {
        private final LocationData location;
        private final long timestamp;

        public LocationPoint(Location location, long timestamp) {
            this.location = new LocationData(location);
            this.timestamp = timestamp;
        }
    }

    private static FaraMCPracticeCore pluginn;

    public static void setPlugin(FaraMCPracticeCore pluginInstance) {
        pluginn = pluginInstance;
    }

    // Add this helper method to create maps in a Java 8 compatible way
    private static Map<String, Object> createMap(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private static Map<String, Object> createMap(String key1, Object value1, String key2, Object value2,
                                                 String key3, Object value3, String key4, Object value4) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }
}