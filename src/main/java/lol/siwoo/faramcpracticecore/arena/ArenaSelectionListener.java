package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.*;

public class ArenaSelectionListener implements Listener {
    private final FaraMCPracticeCore plugin;
    private final ArenaManager manager;

    public ArenaSelectionListener(FaraMCPracticeCore plugin, ArenaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onFightStart(FightStartEvent event) {
        Player p = event.getFight().getPlayersInFight().get(0);

        // Integration with UnrankedGUI: If admin, open selector
        if (p.hasPermission("faramcpracticecore.admin")) {
            ArenaSelectorGUI.open(p, manager, event.getFight());
        } else {
            List<ArenaConfig> configs = new ArrayList<>(manager.getArenas().values());
            if (configs.isEmpty()) return;
            startMatch(event.getFight(), configs.get(new Random().nextInt(configs.size())));
        }
    }

    public void startMatch(Fight fight, ArenaConfig selected) {
        FightSession session = manager.createSession(fight, selected);
        // Use the center offset from ArenaConfig
        Location matchCenter = session.getCenter().clone().add(selected.getCenter());

        fight.getArena().setLoc1(matchCenter.clone().add(selected.getPos1()));
        fight.getArena().setLoc2(matchCenter.clone().add(selected.getPos2()));

        List<Player> players = fight.getPlayersInFight();
        players.get(0).teleport(matchCenter.clone().add(selected.getPos1()));
        players.get(1).teleport(matchCenter.clone().add(selected.getPos2()));
    }

    @EventHandler
    public void onFightEnd(FightEndEvent event) {
        manager.endSession(event.getFight());
    }
}