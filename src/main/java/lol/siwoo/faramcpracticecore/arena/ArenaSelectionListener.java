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
        Player player = fight.getPlayersInFight().get(0);
        String kitName = fight.getKit().getName();

        // Check if admin wants to manually select arena
        if (player.hasPermission("faramcpracticecore.admin.selectarena")) {
            ArenaSelectorGUI.open(player, manager, fight);
            return;
        }

        // Auto-select appropriate arena based on kit
        ArenaConfig selectedArena = manager.getRandomArenaForKit(kitName);

        if (selectedArena == null) {
            plugin.getLogger().warning("No suitable arena found for kit: " + kitName);
            player.sendMessage("§cNo arena available for this gamemode!");
            return;
        }

        startMatch(fight, selectedArena);
    }

    public void startMatch(Fight fight, ArenaConfig arenaConfig) {
        plugin.getLogger().info("Starting match with arena: " + arenaConfig.getName());

        FightSession session = manager.createSession(fight, arenaConfig);
        if (session == null) {
            plugin.getLogger().severe("Failed to create arena session!");
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
        if (players.size() >= 2) {
            players.get(0).teleport(spawn1);
            players.get(1).teleport(spawn2);

            plugin.getLogger().info("Teleported players to arena " + arenaConfig.getName() +
                    " - Player 1: " + spawn1.getBlockX() + "," + spawn1.getBlockY() + "," + spawn1.getBlockZ() +
                    " - Player 2: " + spawn2.getBlockX() + "," + spawn2.getBlockY() + "," + spawn2.getBlockZ());
        }
    }

    @EventHandler
    public void onFightEnd(FightEndEvent event) {
        Fight fight = event.getFight();
        FightSession session = manager.getSession(fight);

        if (session != null) {
            plugin.getLogger().info("Fight ended, cleaning up arena: " + session.getConfig().getName());
        }

        manager.endSession(fight);
    }
}