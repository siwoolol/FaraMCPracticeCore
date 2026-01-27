package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    public void onFightStart(FightStartEvent event) {
        Fight fight = event.getFight();
        List<Player> players = fight.getPlayersInFight();
        
        if (players.isEmpty()) {
            plugin.getLogger().warning("Fight started with no players!");
            return;
        }
        
        Player player = players.get(0);
        String kitName = fight.getKit() != null ? fight.getKit().getName() : "unknown";

        plugin.getLogger().info("Fight starting - Kit: " + kitName + ", Players: " + players.size());

        // Check if admin wants to manually select arena
        if (player.hasPermission("faramcpracticecore.admin.selectarena")) {
            plugin.getLogger().info("Opening arena selector for admin: " + player.getName());
            ArenaSelectorGUI.open(player, manager, fight);
            return;
        }

        // Auto-select appropriate arena based on kit
        ArenaConfig selectedArena = manager.getRandomArenaForKit(kitName);

        if (selectedArena == null) {
            plugin.getLogger().severe("No suitable arena found for kit: " + kitName);
            plugin.getLogger().info("Available arenas: " + manager.getArenas().keySet());
            
            // Try to get any arena as fallback
            selectedArena = manager.getRandomArena();
            
            if (selectedArena == null) {
                plugin.getLogger().severe("NO ARENAS AVAILABLE AT ALL!");
                player.sendMessage("§cNo arenas available! Please contact an administrator.");
                return;
            } else {
                plugin.getLogger().info("Using fallback arena: " + selectedArena.getName());
                player.sendMessage("§eUsing fallback arena: " + selectedArena.getName());
            }
        }

        startMatch(fight, selectedArena);
    }

    public void startMatch(Fight fight, ArenaConfig arenaConfig) {
        plugin.getLogger().info("Starting match with arena: " + arenaConfig.getName());

        FightSession session = manager.createSession(fight, arenaConfig);
        if (session == null) {
            plugin.getLogger().severe("Failed to create arena session!");
            
            // Notify players about the issue
            for (Player player : fight.getPlayersInFight()) {
                player.sendMessage("§cArena system error! Fight may not work properly.");
            }
            return;
        }

        // Calculate actual spawn positions
        Location centerLocation = session.getCenter().clone().add(arenaConfig.getCenter());
        Location spawn1 = centerLocation.clone().add(arenaConfig.getPos1());
        Location spawn2 = centerLocation.clone().add(arenaConfig.getPos2());

        // Set arena boundaries for StrikePractice
        fight.getArena().setLoc1(spawn1);
        fight.getArena().setLoc2(spawn2);

        // Teleport players to their spawn positions
        List<Player> players = fight.getPlayersInFight();
        if (players.size() >= 1) {
            players.get(0).teleport(spawn1);
            plugin.getLogger().info("Teleported " + players.get(0).getName() + " to spawn1: " + 
                                  spawn1.getBlockX() + "," + spawn1.getBlockY() + "," + spawn1.getBlockZ());
        }
        if (players.size() >= 2) {
            players.get(1).teleport(spawn2);
            plugin.getLogger().info("Teleported " + players.get(1).getName() + " to spawn2: " + 
                                  spawn2.getBlockX() + "," + spawn2.getBlockY() + "," + spawn2.getBlockZ());
        }
        
        // Notify players
        for (Player player : players) {
            player.sendMessage("§aFight starting in arena: §b" + arenaConfig.getName());
        }

        plugin.getLogger().info("Successfully started match in arena " + arenaConfig.getName());
    }

    @EventHandler
    public void onFightEnd(FightEndEvent event) {
        Fight fight = event.getFight();
        FightSession session = manager.getSession(fight);

        if (session != null) {
            plugin.getLogger().info("Fight ended, cleaning up arena: " + session.getConfig().getName());
        } else {
            plugin.getLogger().info("Fight ended but no arena session found");
        }

        manager.endSession(fight);
    }
}