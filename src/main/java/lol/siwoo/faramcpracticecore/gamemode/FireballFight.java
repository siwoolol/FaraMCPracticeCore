package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireballFight implements Listener {

    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api;
    private final Map<UUID, Long> cooldownMap;
    private static final long COOLDOWN_DURATION = 5000;
    private final Map<UUID, Boolean> isInFireballfight;
    private final Map<UUID, Boolean> isDead;

    public FireballFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
        this.cooldownMap = new HashMap<>();
        this.isInFireballfight = new HashMap<>();
        this.isDead = new HashMap<>();

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
        if (!e.getFight().getKit().getName().equalsIgnoreCase("fireballfight")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("FireballFight match started. Players: " + e.getFight().getPlayersInFight());

                e.getFight().getPlayersInFight().forEach(p -> {
                    UUID playerId = p.getUniqueId();
                    cooldownMap.put(playerId, System.currentTimeMillis());
                    isInFireballfight.put(playerId, true);
                });
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("fireballfight")) {
            return;
        }

        e.getFight().getPlayersInFight().forEach(p -> {
            UUID playerId = p.getUniqueId();
            cooldownMap.remove(playerId);
            isInFireballfight.remove(playerId);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        cooldownMap.remove(playerId);
        isInFireballfight.remove(playerId);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (isInCooldown(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }

        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (Boolean.TRUE.equals(isInFireballfight.get(playerId))
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
        
        if (Boolean.TRUE.equals(isInFireballfight.get(playerId))) {
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
        
        if (Boolean.TRUE.equals(isInFireballfight.get(playerId))) {
            if (e.getBlock().getType() == Material.BED || e.getBlock().getType() == Material.BED_BLOCK) {
                e.getBlock().setType(Material.AIR);

                // Fixed the logic for bed destruction notification
                api.getFight(p).getPlayersInFight().forEach(player -> {
                    if (!api.getFight(p).getTeammates(p).contains(player.getName())) {
                        // This is an opponent, send them the bed destroyed message
                        player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + "Bed Destroyed", 
                                        ChatColor.WHITE + "You can no longer respawn");
                    }
                });
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
}