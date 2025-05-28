package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    public BedFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
        this.cooldownMap = new HashMap<>();
        this.isInBedfight = new HashMap<>();
        this.isDead = new HashMap<>();
        
        // Start cleanup task
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
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
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

                // Apply cooldown to all players in the fight
                e.getFight().getPlayersInFight().forEach(p -> {
                    UUID playerId = p.getUniqueId();
                    cooldownMap.put(playerId, System.currentTimeMillis());
                    isInBedfight.put(playerId, true);
                });
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("bedfight")) {
            return;
        }

        // Remove all players from the cooldown map when fight ends
        e.getFight().getPlayersInFight().forEach(p -> {
            cooldownMap.remove(UUID.fromString(p.getUniqueId().toString()));
            isInBedfight.remove(UUID.fromString(p.getUniqueId().toString()));
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        cooldownMap.remove(e.getPlayer().getUniqueId());
        isInBedfight.remove(UUID.fromString(e.getPlayer().getUniqueId().toString()));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (isInCooldown(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }

        Player p = e.getPlayer();

        if (isInBedfight.get(p.getUniqueId()) != null
                && isInBedfight.get(p.getUniqueId()).equals(true)
                && p.getLocation().getY() < api.getFight(p).getArena().getLoc1().getY() - 20
                && isDead.get(p.getUniqueId()) == null) {

            Location oldlocation = new Location(p.getLocation().getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            Location location = new Location(p.getLocation().getWorld(), p.getLocation().getX(), -20, p.getLocation().getZ());

            isDead.put(e.getPlayer().getUniqueId(), true);
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
                    isDead.remove(e.getPlayer().getUniqueId());
                }
            }.runTaskLater(plugin, 60L);

//            isDead.put(p.getUniqueId(), true);
//            p.damage(0.1);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (isInBedfight.get(p.getUniqueId()) != null
                && isInBedfight.get(p.getUniqueId()).equals(true)
                && p.getLocation().getY() < 50) {

            if (e.getBlock().getY() > api.getFight(p).getArena().getLoc1().getY() + 15 || isInCooldown(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

//    @EventHandler
//    public void onPlayerDamage(EntityDamageEvent e) {
//        if (!e.getEntityType().equals(EntityType.PLAYER)) {
//            return;
//        }
//
//        Player deadPlayer = (Player) e.getEntity();
//
//        if (isInBedfight.get(deadPlayer.getUniqueId()) != null
//                && isInBedfight.get(deadPlayer.getUniqueId())
//                && isDead.get(deadPlayer.getUniqueId()) != null
//                && isDead.get(deadPlayer.getUniqueId()).equals(true))
//        {
//            deadPlayer.setGameMode(GameMode.SPECTATOR);
//            deadPlayer.teleport(api.getFight(deadPlayer).getArena().getCenter());
//
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    deadPlayer.teleport(api.getSpawnLocation());
//
//                    deadPlayer.setHealth(20.0);
//                    deadPlayer.getActivePotionEffects().forEach(effect ->
//                        deadPlayer.removePotionEffect(effect.getType()));
//                }
//            }.runTaskLater(plugin, 60L);
//        }
//    }

    private boolean isInCooldown(UUID playerId) {
        Long cooldownStart = cooldownMap.get(playerId);
        if (cooldownStart == null) {
            return false;
        }
        return System.currentTimeMillis() - cooldownStart < COOLDOWN_DURATION;
    }
}
