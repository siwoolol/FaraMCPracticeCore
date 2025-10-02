package lol.siwoo.faramcpracticecore.train;

import org.bukkit.entity.Player;

import java.util.UUID;

public class TrainingSession {
    private final UUID playerId;
    private final TrainingMode mode;
    private final long startTime;
    private boolean active;
    private double score;
    private int attempts;

    public TrainingSession(Player player, TrainingMode mode) {
        this.playerId = player.getUniqueId();
        this.mode = mode;
        this.startTime = System.currentTimeMillis();
        this.active = true;
        this.score = 0.0;
        this.attempts = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public TrainingMode getMode() {
        return mode;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }
}