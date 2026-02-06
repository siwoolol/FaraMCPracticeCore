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
import org.bukkit.util.Vector; // Added
import java.util.List;

public class ArenaSelectionListener implements Listener {
    private final FaraMCPracticeCore plugin;
    private final ArenaManager manager;

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
        if (selected == null) selected = manager.getRandomArenaForKit(fight.getKit().getName());

        if (selected != null) startMatch(fight, selected);
    }

    public void startMatch(Fight fight, ArenaConfig config) {
        FightSession session = manager.createSession(fight, config);
        if (session == null) return;

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

        // Use a 1-tick delay to override StrikePractice's initial spawn logic
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (players.size() >= 1) players.get(0).teleport(s1);
            if (players.size() >= 2) players.get(1).teleport(s2);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFightEnd(FightEndEvent event) {
        Fight fight = event.getFight();
        List<Player> players = fight.getPlayersInFight();
        Location spawn = StrikePractice.getAPI().getSpawnLocation();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : players) {
                if (p != null && p.isOnline()) {
                    p.teleport(p.getLocation());
                }
            }
        }, 1L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : players) {
                if (p != null && p.isOnline()) {
                    p.teleport(spawn);
                }
            }
            manager.endSession(fight);
        }, 60L);
    }
}