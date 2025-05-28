package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

    public FireballFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
        this.cooldownMap = new HashMap<>();
        this.isInFireballfight = new HashMap<>();
        
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
        if (!e.getFight().getKit().getName().equalsIgnoreCase("fireballfight")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Fireballfight match started. Players: " + e.getFight().getPlayersInFight());

                // Apply cooldown to all players in the fight
                e.getFight().getPlayersInFight().forEach(p -> {
                    UUID playerId = UUID.fromString(p.getUniqueId().toString());
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

        // Remove all players from the cooldown map when fight ends
        e.getFight().getPlayersInFight().forEach(p -> {
            cooldownMap.remove(UUID.fromString(p.getUniqueId().toString()));
            isInFireballfight.remove(UUID.fromString(p.getUniqueId().toString()));
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        cooldownMap.remove(e.getPlayer().getUniqueId());
        isInFireballfight.remove(UUID.fromString(e.getPlayer().getUniqueId().toString()));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (isInCooldown(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }

        if (isInFireballfight.get(e.getPlayer().getUniqueId()) != null
                && isInFireballfight.get(e.getPlayer().getUniqueId()).equals(true)
                && e.getPlayer().getLocation().getY() < 70) {
            Player p = e.getPlayer();

            p.damage(69420.0);
            EntityDamageEvent voidDamageEvent = new EntityDamageEvent(p, EntityDamageEvent.DamageCause.VOID, 69420.0);
            p.setLastDamageCause(voidDamageEvent);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (isInFireballfight.get(e.getPlayer().getUniqueId()) != null
                && isInFireballfight.get(e.getPlayer().getUniqueId()).equals(true)
                && e.getPlayer().getLocation().getY() < 70) {

            if (e.getBlock().getY() > 95 || isInCooldown(e.getPlayer().getUniqueId())) {
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
}
