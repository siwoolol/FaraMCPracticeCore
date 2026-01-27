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
        Player p = event.getFight().getPlayersInFight().get(0);
        List<ArenaConfig> configs = new ArrayList<>(manager.getArenas().values());
        if (configs.isEmpty()) return;

        ArenaConfig selected = configs.get(new Random().nextInt(configs.size()));

        // Example: Paste at a high Y-level or specific match world
        Location pasteLoc = new Location(p.getWorld(), 2000, 100, 2000);

        manager.paste(selected, pasteLoc);

        // Update StrikePractice boundaries for this specific match
        event.getFight().getArena().setLoc1(pasteLoc.clone().add(selected.getPos1()));
        event.getFight().getArena().setLoc2(pasteLoc.clone().add(selected.getPos2()));

        // Teleport players to their respective dynamic spawns
        event.getFight().getPlayersInFight().get(0).teleport(pasteLoc.clone().add(selected.getPos1()));
        event.getFight().getPlayersInFight().get(1).teleport(pasteLoc.clone().add(selected.getPos2()));
    }

    @EventHandler
    public void onFightEnd(FightEndEvent event) {
        // Logic to clear the physical area goes here
        // You would typically store the 'selected' config and 'pasteLoc' in a Map keyed by the Fight ID
    }
}