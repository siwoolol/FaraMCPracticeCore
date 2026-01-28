package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.events.*;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.List;

public class ArenaSelectionListener implements Listener {
    private final FaraMCPracticeCore plugin;
    private final ArenaManager manager;

    public ArenaSelectionListener(FaraMCPracticeCore plugin, ArenaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onQueueJoin(PlayerQueue event) {
        if (event.getPlayer().hasPermission("faramcpracticecore.admin.selectarena")) {
            ArenaSelectorGUI.open(event.getPlayer(), manager, event.getKit().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFightStart(FightStartEvent event) {
        Fight fight = event.getFight();
        // TRICK: Release entry immediately
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
        Location matchCenter = session.getCenter().clone().add(config.getCenter());
        Location s1 = matchCenter.clone().add(config.getPos1()), s2 = matchCenter.clone().add(config.getPos2());

        fight.getArena().setLoc1(s1);
        fight.getArena().setLoc2(s2);

        List<Player> players = fight.getPlayersInFight();
        if (players.size() >= 1) players.get(0).teleport(s1);
        if (players.size() >= 2) players.get(1).teleport(s2);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent event) { manager.endSession(event.getFight()); }
}