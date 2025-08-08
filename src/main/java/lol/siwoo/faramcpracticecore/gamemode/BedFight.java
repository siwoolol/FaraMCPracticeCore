package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.arena.DefaultCachedBlockChange;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Bed;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BedFight implements Listener {

    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api;
    private final Map<UUID, Long> cooldownMap;
    private static final long COOLDOWN_DURATION = 5000;
    private final Map<UUID, Boolean> isInBedfight;
    private final Map<UUID, Boolean> isDead;
    private final Map<UUID, Location> startPositions;
    private final Map<String, String> fightIds;
    private final Map<String, List<BedBreakData>> fightBedBreaks;
    private int fightCounter = 0;

    public BedFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
        this.cooldownMap = new HashMap<>();
        this.isInBedfight = new HashMap<>();
        this.isDead = new HashMap<>();
        this.startPositions = new HashMap<>();
        this.fightIds = new HashMap<>();
        this.fightBedBreaks = new HashMap<>();

        startCleanupTask();
    }

    private static class BedBreakData {
        final Location headLocation;
        final Location footLocation;
        final Material headMaterial;
        final Material footMaterial;
        final byte headData;
        final byte footData;

        BedBreakData(Location headLoc, Location footLoc, Material headMat, Material footMat, byte headData, byte footData) {
            this.headLocation = headLoc;
            this.footLocation = footLoc;
            this.headMaterial = headMat;
            this.footMaterial = footMat;
            this.headData = headData;
            this.footData = footData;
        }
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                cooldownMap.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue() > COOLDOWN_DURATION);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("bedfight")) {
            return;
        }

        String fightId = "bedfight_" + (++fightCounter)+ "_" + System.currentTimeMillis();
        fightBedBreaks.put(fightId, new ArrayList<>());

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Bedfight match started. Players: " + e.getFight().getPlayersInFight());

                e.getFight().getPlayersInFight().forEach(p -> {
                    UUID playerId = p.getUniqueId();
                    cooldownMap.put(playerId, System.currentTimeMillis());
                    isInBedfight.put(playerId, true);
                    fightIds.put(playerId.toString(), fightId);

                    startPositions.put(playerId, p.getLocation().clone());
                    plugin.getLogger().info("Cached starting position for " + p.getName() + ": " + p.getLocation());
                });
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("bedfight")) {
            return;
        }

        String fightId = null;
        for (Player p : e.getFight().getPlayersInFight()) {
            fightId = fightIds.get(p.getUniqueId().toString());
            if (fightId != null) {
                break;
            }
        }

        if (fightId != null) {
            final String finalFightId = fightId;
            plugin.getLogger().info("Fight ended with ID: " + fightId + ". Starting bed rollback in 1 second...");

            // Schedule rollback after 1 second (20 ticks)
            new BukkitRunnable() {
                @Override
                public void run() {
                    rollbackBeds(finalFightId);
                }
            }.runTaskLater(plugin, 20L);
        }

        e.getFight().getPlayersInFight().forEach(p -> {
            UUID playerId = p.getUniqueId();
            cooldownMap.remove(playerId);
            isInBedfight.remove(playerId);
            startPositions.remove(playerId);
            fightIds.remove(playerId.toString());
        });
    }

    private void rollbackBeds(String fightId) {
        List<BedBreakData> bedBreaks = fightBedBreaks.get(fightId);
        if (bedBreaks == null || bedBreaks.isEmpty()) {
            plugin.getLogger().info("No beds to rollback for fight ID: " + fightId);
            return;
        }

        plugin.getLogger().info("Rolling back " + bedBreaks.size() + " bed breaks for fight ID: " + fightId);

        for (BedBreakData bedData : bedBreaks) {
            // Restore head block
            bedData.headLocation.getBlock().setType(bedData.headMaterial);
            bedData.headLocation.getBlock().setData(bedData.headData);

            // Restore foot block
            bedData.footLocation.getBlock().setType(bedData.footMaterial);
            bedData.footLocation.getBlock().setData(bedData.footData);

            plugin.getLogger().info("Restored bed at head: " + bedData.headLocation + ", foot: " + bedData.footLocation);
        }

        // Clean up bed break data after rollback
        fightBedBreaks.remove(fightId);
        plugin.getLogger().info("Cleaned up bed break data for fight ID: " + fightId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        cooldownMap.remove(playerId);
        isInBedfight.remove(playerId);
        startPositions.remove(playerId);
        fightIds.remove(playerId.toString());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (isInCooldown(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }

        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (Boolean.TRUE.equals(isInBedfight.get(playerId))
                && p.getLocation().getY() < api.getFight(p).getArena().getLoc1().getY() - 12
                && !Boolean.TRUE.equals(isDead.get(playerId))) {

            Location oldlocation = new Location(p.getLocation().getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            Location location = new Location(p.getLocation().getWorld(), p.getLocation().getX(), -20, p.getLocation().getZ());

            isDead.put(playerId, true);
            p.teleport(location);

            new BukkitRunnable() {
                @Override
                public void run() {
                    p.teleport(oldlocation);
                }
            }.runTaskLater(plugin, 5L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    isDead.remove(playerId);
                }
            }.runTaskLater(plugin, 60L);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        
        if (Boolean.TRUE.equals(isInBedfight.get(playerId))) {
            if (e.getBlock().getY() > api.getFight(p).getArena().getLoc1().getY() + 10
                    || isInCooldown(playerId)) {
                e.setCancelled(true);
            }
        }
    }

@EventHandler
public void onPlayerBlockBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    UUID playerId = p.getUniqueId();

    // Always log basic info first
    plugin.getLogger().info("=== BLOCK BREAK EVENT TRIGGERED ===");
    plugin.getLogger().info("Player: " + p.getName() + " (" + playerId + ")");
    plugin.getLogger().info("Block type: " + e.getBlock().getType());
    plugin.getLogger().info("Block location: " + e.getBlock().getLocation());
    
    // Check if player is in bedfight map
    Boolean inBedfight = isInBedfight.get(playerId);
    plugin.getLogger().info("Player in bedfight map: " + inBedfight);
    plugin.getLogger().info("Boolean.TRUE.equals(inBedfight): " + Boolean.TRUE.equals(inBedfight));
    
    // Check if block is bed
    boolean isBed = e.getBlock().getType() == Material.BED_BLOCK;
    plugin.getLogger().info("Is block a bed: " + isBed);
    plugin.getLogger().info("Block type equals Material.BED: " + e.getBlock().getType().equals(Material.BED));
    
    // Check the condition that's failing
    boolean condition1 = !Boolean.TRUE.equals(isInBedfight.get(playerId));
    boolean condition2 = !(e.getBlock().getType() == Material.BED_BLOCK);
    plugin.getLogger().info("Condition 1 (not in bedfight): " + condition1);
    plugin.getLogger().info("Condition 2 (not a bed): " + condition2);
    plugin.getLogger().info("Overall condition (should return): " + (condition1 || condition2));

    if (!Boolean.TRUE.equals(isInBedfight.get(playerId))
        || !(e.getBlock().getType() == Material.BED_BLOCK)) {
        plugin.getLogger().info("EARLY RETURN - Condition failed, not processing bed break");
        plugin.getLogger().info("=== BLOCK BREAK EVENT END (EARLY RETURN) ===");
        return;
    }

    plugin.getLogger().info("PASSED INITIAL CHECKS - Continuing with bed break logic");

    String fightId = fightIds.get(playerId.toString());
    if (fightId == null) {
        plugin.getLogger().warning("No fight ID found for player: " + p.getName());
        plugin.getLogger().info("Available fight IDs: " + fightIds.keySet());
        return;
    }

    plugin.getLogger().info("Fight ID: " + fightId);

    // Continue with rest of the logic...
    int x1 = api.getFight(p).getArena().getLoc1().getBlockX();
    int y1 = api.getFight(p).getArena().getLoc1().getBlockY();
    int z1 = api.getFight(p).getArena().getLoc1().getBlockZ();

    int x2 = api.getFight(p).getArena().getLoc2().getBlockX();
    int y2 = api.getFight(p).getArena().getLoc2().getBlockY();
    int z2 = api.getFight(p).getArena().getLoc2().getBlockZ();

    plugin.getLogger().info("Arena loc1: " + x1 + ", " + y1 + ", " + z1);
    plugin.getLogger().info("Arena loc2: " + x2 + ", " + y2 + ", " + z2);

    int x = e.getBlock().getX();
    int y = e.getBlock().getY();
    int z = e.getBlock().getZ();

    plugin.getLogger().info("Bed location: " + x + ", " + y + ", " + z);

    Location startPos = startPositions.get(playerId);
    if (startPos == null) {
        plugin.getLogger().warning("No start position found for player: " + p.getName());
        plugin.getLogger().info("Available start positions: " + startPositions.keySet());
        return;
    }

    int sx = startPos.getBlockX();
    int sy = startPos.getBlockY(); // Fixed: was using getBlockX() for Y
    int sz = startPos.getBlockZ(); // Fixed: was using getBlockX() for Z

    plugin.getLogger().info("Player start position: " + sx + ", " + sy + ", " + sz);

    int playerTeam = 0;

    String playerTeamResult = compareCoords(sx, sy, sz, x1, y1, z1, x2, y2, z2);
    plugin.getLogger().info("Player team coordinate comparison result: " + playerTeamResult);

    if (playerTeamResult.equals("1")) {
        playerTeam = 1; // team 1
    } else if (playerTeamResult.equals("2")) {
        playerTeam = 2; // team 2
    }

    plugin.getLogger().info("Player team: " + playerTeam);

    String bedTeamResult = compareCoords(x, y, z, x1, y1, z1, x2, y2, z2);
    plugin.getLogger().info("Bed team coordinate comparison result: " + bedTeamResult);

    if (Boolean.TRUE.equals(isInBedfight.get(playerId)) && !isInCooldown(playerId)) {
        plugin.getLogger().info("Player is in bedfight and not in cooldown");
        
        if (bedTeamResult.equals("1")) {
            plugin.getLogger().info("Bed is on team 1 side");
            if (playerTeam == 2) {
                plugin.getLogger().info("Player is team 2, can break team 1 bed - calling handleBedBreak");
                handleBedBreak(e, fightId, p);
            } else {
                plugin.getLogger().info("Player is team 1, cannot break own bed - cancelling event");
                e.setCancelled(true);
            }
        } else if (bedTeamResult.equals("2")) {
            plugin.getLogger().info("Bed is on team 2 side");
            if (playerTeam == 1) {
                plugin.getLogger().info("Player is team 1, can break team 2 bed - calling handleBedBreak");
                handleBedBreak(e, fightId, p);
            } else {
                plugin.getLogger().info("Player is team 2, cannot break own bed - cancelling event");
                e.setCancelled(true);
            }
        } else {
            plugin.getLogger().info("Bed is not in either team area - cancelling event");
            e.setCancelled(true);
        }
    } else {
        plugin.getLogger().info("Player not in bedfight or in cooldown - cancelling event");
        plugin.getLogger().info("In cooldown: " + isInCooldown(playerId));
        e.setCancelled(true);
    }
    
    plugin.getLogger().info("Final event cancelled status: " + e.isCancelled());
    plugin.getLogger().info("=== BLOCK BREAK EVENT DEBUG END ===");
}

    private void handleBedBreak(BlockBreakEvent e, String fightId, Player p) {
        plugin.getLogger().info("=== BED BREAK DEBUG START ===");
        plugin.getLogger().info("Player: " + p.getName() + " (" + p.getUniqueId() + ")");
        plugin.getLogger().info("Fight ID: " + fightId);
        plugin.getLogger().info("Block type: " + e.getBlock().getType());
        plugin.getLogger().info("Block location: " + e.getBlock().getLocation());

        if (e.getBlock().getType() == Material.BED_BLOCK) {
            Bed bedData = (Bed) e.getBlock().getState().getData();
            Block headBlock;
            Block footBlock;

            plugin.getLogger().info("Bed data - IsHeadOfBed: " + bedData.isHeadOfBed());
            plugin.getLogger().info("Bed data - Facing: " + bedData.getFacing());

            if (bedData.isHeadOfBed()) {
                headBlock = e.getBlock();
                footBlock = headBlock.getRelative(bedData.getFacing().getOppositeFace());
            } else {
                footBlock = e.getBlock();
                headBlock = footBlock.getRelative(bedData.getFacing());
            }

            plugin.getLogger().info("Head block location: " + headBlock.getLocation());
            plugin.getLogger().info("Foot block location: " + footBlock.getLocation());
            plugin.getLogger().info("Head block type: " + headBlock.getType());
            plugin.getLogger().info("Foot block type: " + footBlock.getType());

            // Log the bed break data before destroying
            BedBreakData breakData = new BedBreakData(
                    headBlock.getLocation().clone(),
                    footBlock.getLocation().clone(),
                    headBlock.getType(),
                    footBlock.getType(),
                    headBlock.getData(),
                    footBlock.getData()
            );

            fightBedBreaks.get(fightId).add(breakData);

            plugin.getLogger().info("Bed break logged for fight ID: " + fightId +
                    " at head: " + headBlock.getLocation() + ", foot: " + footBlock.getLocation());

            e.setCancelled(false);
            plugin.getLogger().info("Break event cancelled: false");

            if (footBlock.getType() == Material.BED) {
                plugin.getLogger().info("Breaking foot block at: " + footBlock.getLocation());
                footBlock.setType(Material.AIR);
                api.getFight(p).addBlockChange(new DefaultCachedBlockChange(footBlock.getLocation(), footBlock));
            }
            if (headBlock.getType() == Material.BED && !headBlock.equals(e.getBlock())) {
                plugin.getLogger().info("Breaking head block at: " + headBlock.getLocation());
                headBlock.setType(Material.AIR);
                api.getFight(p).addBlockChange(new DefaultCachedBlockChange(headBlock.getLocation(), headBlock));
            }

            api.getFight(p).addBlockChange(new DefaultCachedBlockChange(e.getBlock().getLocation(), e.getBlock()));
            plugin.getLogger().info("Added block change to StrikePractice tracking");

            // Get all players in fight for debugging
            plugin.getLogger().info("Players in fight: " + api.getFight(p).getPlayersInFight().size());

            api.getFight(p).getPlayersInFight().forEach(player -> {
                plugin.getLogger().info("Processing player: " + player.getName());

                // Get the list of teammates for the current player
                java.util.List<String> teammates = api.getFight(player).getTeammates(player);
                plugin.getLogger().info("Player " + player.getName() + " teammates: " + teammates);

                // Check if the bed breaker is on the same team as the current player
                boolean sameTeam = teammates.contains(p.getName()) || player.equals(p);
                plugin.getLogger().info("Player " + player.getName() + " same team as breaker " + p.getName() + ": " + sameTeam);

                if (sameTeam) {
                    // Same team - bed destroyer and their teammates get success message
                    plugin.getLogger().info("Sending success title to " + player.getName());
                    player.sendTitle(ChatColor.GREEN.toString() + ChatColor.BOLD + "Bed Destroyed!",
                            ChatColor.WHITE + "Enemy bed has been destroyed!");
                    player.sendMessage(ChatColor.GREEN + "[DEBUG] You/your teammate destroyed the enemy bed!");
                } else {
                    // Different team - victims get the warning message
                    plugin.getLogger().info("Sending warning title to " + player.getName());
                    player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + "Bed Destroyed",
                            ChatColor.WHITE + "You can no longer respawn!");
                    player.sendMessage(ChatColor.RED + "[DEBUG] Your bed was destroyed by the enemy!");
                }
            });

            plugin.getLogger().info("Title messages sent to all players");
        } else {
            plugin.getLogger().warning("Block is not a bed! Type: " + e.getBlock().getType());
        }

        plugin.getLogger().info("=== BED BREAK DEBUG END ===");
    }

        private boolean isInCooldown(UUID playerId) {
        Long cooldownStart = cooldownMap.get(playerId);
        if (cooldownStart == null) {
            return false;
        }
        return System.currentTimeMillis() - cooldownStart < COOLDOWN_DURATION;
    }

    public String compareCoords(int x, int y, int z, int x1, int y1, int z1, int x2, int y2, int z2) {
        String selected = "";

        int diffx1 = x1 - x;
        int diffy1 = y1 - y;
        int diffz1 = z1 - z;
        int diff1 = makenonMinus(diffx1) + makenonMinus(diffy1) + makenonMinus(diffz1);

        int diffx2 = x2 - x;
        int diffy2 = y2 - y;
        int diffz2 = z2 - z;
        int diff2 = makenonMinus(diffx2) + makenonMinus(diffy2) + makenonMinus(diffz2);

        if (diff1 < diff2) {
            selected = "1";
        } else if (diff1 > diff2) {
            selected = "2";
        } else {
            selected = "0";
        }

        return selected;
    }

    public int makenonMinus(int i) {
        if (i < 0) {
            i = -i;
        } else {
            i = i;
        }
        return i;
    }
}