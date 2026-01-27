package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArenaSelectionListener implements Listener {
    private final FaraMCPracticeCore plugin;
    private final ArenaManager manager;

    public ArenaSelectionListener(FaraMCPracticeCore plugin, ArenaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onFightStart(FightStartEvent event) {
        List<ArenaConfig> configs = new ArrayList<>(manager.getArenas().values());
        if (configs.isEmpty()) return;

        ArenaConfig selected = configs.get(new Random().nextInt(configs.size()));

        // Create a dedicated session in one of the void worlds
        FightSession session = manager.createSession(event.getFight(), selected);
        Location pasteLoc = session.getCenter();

        // Update StrikePractice boundaries for this specific isolated match
        event.getFight().getArena().setLoc1(pasteLoc.clone().add(selected.getPos1()));
        event.getFight().getArena().setLoc2(pasteLoc.clone().add(selected.getPos2()));

        // Teleport players to their respective dynamic spawns in the void world
        for (int i = 0; i < event.getFight().getPlayersInFight().size(); i++) {
            Player p = event.getFight().getPlayersInFight().get(i);
            // Cycle spawns based on config
            if (i % 2 == 0) {
                p.teleport(pasteLoc.clone().add(selected.getPos1()));
            } else {
                p.teleport(pasteLoc.clone().add(selected.getPos2()));
            }
        }
    }

    @EventHandler
    public void onFightEnd(FightEndEvent event) {
        // Clear the physical arena and release the session slot
        manager.endSession(event.getFight());
    }
}