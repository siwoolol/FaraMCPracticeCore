package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.events.*;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ArenaSelectionListener implements Listener {
    private final FaraMCPracticeCore plugin;
    private final ArenaManager manager;
    // Track players who are in the post-fight delay to prevent duplicate handling
    private final Set<UUID> delayedPlayers = new HashSet<>();

    public ArenaSelectionListener(FaraMCPracticeCore plugin, ArenaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onKitSelect(KitSelectEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faramcpracticecore.selectarena") && StrikePractice.getAPI().isInQueue(player)) {
            ArenaSelectorGUI.open(player, manager, event.getKit().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFightStart(FightStartEvent event) {
        Fight fight = event.getFight();
        if (fight.getArena().getName().toLowerCase().contains("dynamic")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> fight.getArena().setUsing(false), 2L);
        }

        Player p = fight.getPlayersInFight().get(0);
        ArenaConfig selected = ArenaSelectorGUI.queuedSelections.remove(p.getUniqueId());
        if (selected == null)
            selected = manager.getRandomArenaForKit(fight.getKit().getName());

        if (selected != null)
            startMatch(fight, selected);
    }

    public void startMatch(Fight fight, ArenaConfig config) {
        manager.createSession(fight, config).thenAccept(session -> {
            if (session == null)
                return;

            // Teleport on the main thread after paste completes
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location origin = session.getCenter().clone().add(config.getCenter());

                Location s1 = origin.clone().add(config.getPos1());
                Location s2 = origin.clone().add(config.getPos2());

                Vector dir1 = s2.toVector().subtract(s1.toVector()).setY(0);
                s1.setDirection(dir1);

                Vector dir2 = s1.toVector().subtract(s2.toVector()).setY(0);
                s2.setDirection(dir2);

                // Update StrikePractice internal locations
                fight.getArena().setLoc1(s1);
                fight.getArena().setLoc2(s2);

                List<Player> players = fight.getPlayersInFight();
                if (players.size() >= 1)
                    players.get(0).teleport(s1);
                if (players.size() >= 2)
                    players.get(1).teleport(s2);
            });
        });
    }

    /**
     * Handles the delayed teleport-to-lobby for all fight types.
     * Keeps players in the arena for 3 seconds so they can see the victory/defeat
     * title.
     */
    private void handleDelayedTeleport(Fight fight, List<Player> players) {
        Location spawn = StrikePractice.getAPI().getSpawnLocation();

        // Mark players as in post-fight delay
        for (Player p : players) {
            if (p != null)
                delayedPlayers.add(p.getUniqueId());
        }

        // 1-tick delay: override StrikePractice's instant teleport back to spawn
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : players) {
                if (p != null && p.isOnline() && delayedPlayers.contains(p.getUniqueId())) {
                    p.teleport(p.getLocation()); // Keep them where they are
                }
            }
        }, 1L);

        // After 3 seconds, teleport to lobby and clean up arena
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : players) {
                if (p != null && p.isOnline()) {
                    p.teleport(spawn);
                    delayedPlayers.remove(p.getUniqueId());
                }
            }
            manager.endSession(fight);
        }, 60L); // 60 ticks = 3 seconds
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFightEnd(FightEndEvent event) {
        Fight fight = event.getFight();
        List<Player> players = fight.getPlayersInFight();
        handleDelayedTeleport(fight, players);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelEnd(DuelEndEvent event) {
        Fight fight = event.getFight();
        List<Player> players = fight.getPlayersInFight();

        // Override StrikePractice's instant teleport for duels too
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : players) {
                if (p != null && p.isOnline() && delayedPlayers.contains(p.getUniqueId())) {
                    p.teleport(p.getLocation());
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBotDuelEnd(BotDuelEndEvent event) {
        Fight fight = event.getFight();
        Player player = event.getPlayer();

        // Override StrikePractice's instant teleport for bot duels too
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player != null && player.isOnline() && delayedPlayers.contains(player.getUniqueId())) {
                player.teleport(player.getLocation());
            }
        }, 1L);
    }
}