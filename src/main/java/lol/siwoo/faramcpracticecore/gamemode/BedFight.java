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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BedFight implements Listener {

    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api;
    private final Map<UUID, Long> cooldownMap;
    private static final long COOLDOWN_DURATION = 5000;
    private final Map<UUID, Boolean> isInBedfight;
    private final Map<UUID, Boolean> isDead;
    private final Map<UUID, Location> startPositions;


    public BedFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
        this.cooldownMap = new HashMap<>();
        this.isInBedfight = new HashMap<>();
        this.isDead = new HashMap<>();
        this.startPositions = new HashMap<>();

        startCleanupTask();
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

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Bedfight match started. Players: " + e.getFight().getPlayersInFight());

                e.getFight().getPlayersInFight().forEach(p -> {
                    UUID playerId = p.getUniqueId();
                    cooldownMap.put(playerId, System.currentTimeMillis());
                    isInBedfight.put(playerId, true);

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

        e.getFight().getPlayersInFight().forEach(p -> {
            UUID playerId = p.getUniqueId();
            cooldownMap.remove(playerId);
            isInBedfight.remove(playerId);
            startPositions.remove(playerId);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        cooldownMap.remove(playerId);
        isInBedfight.remove(playerId);
        startPositions.remove(playerId);
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
    public void onPlayerBlockDestroy(BlockDamageEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (!Boolean.TRUE.equals(isInBedfight.get(playerId))
            || !(e.getBlock().getType() == Material.BED)) {
            return;
        }

        int x1 = api.getFight(p).getArena().getLoc1().getBlockX();
        int y1 = api.getFight(p).getArena().getLoc1().getBlockY();
        int z1 = api.getFight(p).getArena().getLoc1().getBlockZ();

        int x2 = api.getFight(p).getArena().getLoc2().getBlockX();
        int y2 = api.getFight(p).getArena().getLoc2().getBlockY();
        int z2 = api.getFight(p).getArena().getLoc2().getBlockZ();

        int x = e.getBlock().getX();
        int y = e.getBlock().getY();
        int z = e.getBlock().getZ();

        int sx = startPositions.get(playerId).getBlockX();
        int sy = startPositions.get(playerId).getBlockX();
        int sz = startPositions.get(playerId).getBlockX();

        int playerTeam = 0;

        if (x1 == sx && y1 == sy && z1 == sz) {
            playerTeam = 1; // team 1
        } else if (x2 == sx && y2 == sy && z2 == sz) {
            playerTeam = 2; // team 2
        }

        if (Boolean.TRUE.equals(isInBedfight.get(playerId))
                && !isInCooldown(playerId)) {
            if (compareCoords(x, y, z, x1, y1, z1, x2, y2, z2).equals("1")) {
                if (playerTeam == 1) {
                    e.setCancelled(true);
                } else {
                    e.setCancelled(false);
                    e.getBlock().setType(Material.AIR);
                    api.getFight(p).addBlockChange(new DefaultCachedBlockChange(e.getBlock().getLocation(), Material.BED, (byte) 0));

                    api.getFight(p).getPlayersInFight().forEach(player -> {
                        if (!api.getFight(p).getTeammates(p).contains(player.getName())) {
                            // This is an opponent, send them the bed destroyed message
                            player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + "Bed Destroyed",
                                    ChatColor.WHITE + "You can no longer respawn");
                        }
                    });
                }
            } else if (compareCoords(x, y, z, x1, y1, z1, x2, y2, z2).equals("2")) {
                if (playerTeam == 2) {
                    e.setCancelled(true);
                } else {
                    e.setCancelled(false);
                    e.getBlock().setType(Material.AIR);
                    api.getFight(p).addBlockChange(new DefaultCachedBlockChange(e.getBlock().getLocation(), Material.BED, (byte) 0));

                    api.getFight(p).getPlayersInFight().forEach(player -> {
                        if (!api.getFight(p).getTeammates(p).contains(player.getName())) {
                            // This is an opponent, send them the bed destroyed message
                            player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + "Bed Destroyed",
                                    ChatColor.WHITE + "You can no longer respawn");
                        }
                    });
                }
            } else {
                e.setCancelled(true);
            }
        }
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