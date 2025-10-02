package lol.siwoo.faramcpracticecore.train;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import lol.siwoo.faramcpracticecore.train.npcs.TrainingNPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TrainingManager {
    private final FaraMCPracticeCore plugin;
    private final ProtocolManager protocolManager;
    private final Map<UUID, TrainingSession> activeSessions;
    private final Map<UUID, List<TrainingNPC>> playerNPCs;

    public TrainingManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.activeSessions = new HashMap<>();
        this.playerNPCs = new HashMap<>();
    }

    public void startTraining(Player player, TrainingMode mode) {
        if (isInTraining(player)) {
            endTraining(player);
        }

        TrainingSession session = new TrainingSession(player, mode);
        activeSessions.put(player.getUniqueId(), session);

        // Initialize the specific training mode
        switch (mode) {
            case STRAFE:
                initializeStrafeTraining(player, session);
                break;
            case AIM_TRACKER:
                initializeAimTrackerTraining(player, session);
                break;
            case CPS_TESTER:
                initializeCPSTraining(player, session);
                break;
            case W_TAP_TRAINER:
                initializeWTapTraining(player, session);
                break;
        }
    }

    public void endTraining(Player player) {
        TrainingSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.setActive(false);
            // Remove all NPCs for this player
            removeAllNPCs(player);
            displayResults(player, session);
        }
    }

    public boolean isInTraining(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public TrainingSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    private void initializeStrafeTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lStrafe Training Started!");
        player.sendMessage("§7Move using only A and D keys while maintaining forward momentum.");

        // Spawn moving target NPCs for strafe training
        spawnStrafeTargets(player);
    }

    private void initializeAimTrackerTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lAim Tracker Started!");
        player.sendMessage("§7Track and click the moving targets to improve your aim.");

        // Spawn moving targets for aim training
        spawnAimTargets(player);
    }

    private void initializeCPSTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lCPS Tester Started!");
        player.sendMessage("§7Click as fast as you can for 10 seconds!");

        // Spawn a stationary target for CPS testing
        spawnCPSTarget(player);

        // Start 10-second timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isInTraining(player)) {
                    endTraining(player);
                }
            }
        }.runTaskLater(plugin, 200L); // 10 seconds
    }

    private void initializeWTapTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lW-Tap Trainer Started!");
        player.sendMessage("§7Practice releasing and pressing W to maintain combos.");

        // Spawn combat dummy for W-tap training
        spawnWTapTarget(player);
    }

    private void spawnStrafeTargets(Player player) {
        List<TrainingNPC> npcs = new ArrayList<>();
        Location playerLoc = player.getLocation();

        // Spawn 3 moving targets around the player
        for (int i = 0; i < 3; i++) {
            Location npcLoc = playerLoc.clone().add(
                    Math.cos(i * 2 * Math.PI / 3) * 10,
                    0,
                    Math.sin(i * 2 * Math.PI / 3) * 10
            );

            TrainingNPC npc = new TrainingNPC(plugin, npcLoc, "§c§lStrafe Target " + (i + 1));
            npc.spawnForPlayer(player);
            npcs.add(npc);

            // Make NPC move in circles
            startNPCMovement(npc, player, true);
        }

        playerNPCs.put(player.getUniqueId(), npcs);
    }

    private void spawnAimTargets(Player player) {
        List<TrainingNPC> npcs = new ArrayList<>();
        Location playerLoc = player.getLocation();

        // Spawn 5 randomly moving targets
        for (int i = 0; i < 5; i++) {
            Location npcLoc = playerLoc.clone().add(
                    (Math.random() - 0.5) * 20,
                    Math.random() * 5 + 1,
                    (Math.random() - 0.5) * 20
            );

            TrainingNPC npc = new TrainingNPC(plugin, npcLoc, "§e§lAim Target " + (i + 1));
            npc.spawnForPlayer(player);
            npcs.add(npc);

            // Make NPC move randomly
            startNPCMovement(npc, player, false);
        }

        playerNPCs.put(player.getUniqueId(), npcs);
    }

    private void spawnCPSTarget(Player player) {
        List<TrainingNPC> npcs = new ArrayList<>();
        Location npcLoc = player.getLocation().add(0, 0, 5);

        TrainingNPC npc = new TrainingNPC(plugin, npcLoc, "§b§lCPS Target");
        npc.spawnForPlayer(player);
        npcs.add(npc);

        playerNPCs.put(player.getUniqueId(), npcs);
    }

    private void spawnWTapTarget(Player player) {
        List<TrainingNPC> npcs = new ArrayList<>();
        Location npcLoc = player.getLocation().add(0, 0, 3);

        TrainingNPC npc = new TrainingNPC(plugin, npcLoc, "§d§lCombat Dummy");
        npc.spawnForPlayer(player);
        npcs.add(npc);

        playerNPCs.put(player.getUniqueId(), npcs);
    }

    private void startNPCMovement(TrainingNPC npc, Player player, boolean circular) {
        new BukkitRunnable() {
            double angle = 0;
            Location center = npc.getLocation().clone();

            @Override
            public void run() {
                if (!isInTraining(player) || npc.isRemoved()) {
                    cancel();
                    return;
                }

                Location newLoc;
                if (circular) {
                    // Circular movement
                    newLoc = center.clone().add(
                            Math.cos(angle) * 5,
                            0,
                            Math.sin(angle) * 5
                    );
                    angle += 0.1;
                } else {
                    // Random movement
                    newLoc = npc.getLocation().clone().add(
                            (Math.random() - 0.5) * 2,
                            0,
                            (Math.random() - 0.5) * 2
                    );
                }

                npc.teleportForPlayer(player, newLoc);
            }
        }.runTaskTimer(plugin, 0L, 2L); // Update every 2 ticks
    }

    private void removeAllNPCs(Player player) {
        List<TrainingNPC> npcs = playerNPCs.remove(player.getUniqueId());
        if (npcs != null) {
            for (TrainingNPC npc : npcs) {
                npc.removeForPlayer(player);
            }
        }
    }

    public void handleNPCClick(Player player, int entityId) {
        if (!isInTraining(player)) return;

        TrainingSession session = getSession(player);
        List<TrainingNPC> npcs = playerNPCs.get(player.getUniqueId());

        if (npcs == null) return;

        for (TrainingNPC npc : npcs) {
            if (npc.getEntityId() == entityId) {
                session.incrementAttempts();
                session.setScore(session.getScore() + 10); // Add points for hitting target

                player.sendMessage("§a§l+10 points! §7(" + session.getScore() + " total)");

                // Remove hit target and spawn new one for aim training
                if (session.getMode() == TrainingMode.AIM_TRACKER) {
                    npc.removeForPlayer(player);
                    npcs.remove(npc);

                    // Spawn new target
                    Location playerLoc = player.getLocation();
                    Location newNpcLoc = playerLoc.clone().add(
                            (Math.random() - 0.5) * 20,
                            Math.random() * 5 + 1,
                            (Math.random() - 0.5) * 20
                    );

                    TrainingNPC newNpc = new TrainingNPC(plugin, newNpcLoc, "§e§lAim Target");
                    newNpc.spawnForPlayer(player);
                    npcs.add(newNpc);
                    startNPCMovement(newNpc, player, false);
                }
                break;
            }
        }
    }

    private void displayResults(Player player, TrainingSession session) {
        long duration = session.getDuration() / 1000; // Convert to seconds
        player.sendMessage("§6§l=== Training Results ===");
        player.sendMessage("§eMode: §f" + session.getMode().getDisplayName());
        player.sendMessage("§eDuration: §f" + duration + " seconds");
        player.sendMessage("§eScore: §f" + String.format("%.2f", session.getScore()));
        player.sendMessage("§eHits: §f" + session.getAttempts());

        if (session.getMode() == TrainingMode.CPS_TESTER && duration > 0) {
            double cps = (double) session.getAttempts() / duration;
            player.sendMessage("§eAverage CPS: §f" + String.format("%.2f", cps));
        }

        player.sendMessage("§6§l=====================");
    }
}