package lol.siwoo.faramcpracticecore.train;

import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrainingManager {
    private final FaraMCPracticeCore plugin;
    private final Map<UUID, TrainingSession> activeSessions;

    public TrainingManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
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
        // Add strafe-specific initialization logic here
    }

    private void initializeAimTrackerTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lAim Tracker Started!");
        player.sendMessage("§7Track and click the moving targets to improve your aim.");
        // Add aim tracker initialization logic here
    }

    private void initializeCPSTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lCPS Tester Started!");
        player.sendMessage("§7Click as fast as you can for 10 seconds!");
        // Add CPS tester initialization logic here
    }

    private void initializeWTapTraining(Player player, TrainingSession session) {
        player.sendMessage("§a§lW-Tap Trainer Started!");
        player.sendMessage("§7Practice releasing and pressing W to maintain combos.");
        // Add W-tap trainer initialization logic here
    }

    private void displayResults(Player player, TrainingSession session) {
        long duration = session.getDuration() / 1000; // Convert to seconds
        player.sendMessage("§6§l=== Training Results ===");
        player.sendMessage("§eMode: §f" + session.getMode().getDisplayName());
        player.sendMessage("§eDuration: §f" + duration + " seconds");
        player.sendMessage("§eScore: §f" + String.format("%.2f", session.getScore()));
        player.sendMessage("§eAttempts: §f" + session.getAttempts());
        player.sendMessage("§6§l=====================");
    }
}