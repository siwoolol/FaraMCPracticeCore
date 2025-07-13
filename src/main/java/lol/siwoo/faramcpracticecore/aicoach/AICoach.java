package lol.siwoo.faramcpracticecore.aicoach;

import ga.strikepractice.api.StrikePracticeAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AICoach implements CommandExecutor {
    private final Set<UUID> aiCoachEnabled = new HashSet<>();
    private final Map<UUID, BukkitRunnable> activeMonitors = new HashMap<>();
    private final Plugin plugin;
    private final StrikePracticeAPI api;

    public AICoach(Plugin plugin, StrikePracticeAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (aiCoachEnabled.contains(playerUUID)) {
            aiCoachEnabled.remove(playerUUID);
            stopMonitoring(playerUUID);
            player.sendMessage(ChatColor.RED + "✨AI Coach Disabled!");
        } else {
            aiCoachEnabled.add(playerUUID);
            player.sendMessage(ChatColor.GREEN + "✨AI Coach Enabled!");
        }

        return true;
    }

    public void startMonitoring(final Player player, final Player opponent) {
        if (!aiCoachEnabled.contains(player.getUniqueId())) {
            return;
        }

        final StrikePracticeAPI apiRef = this.api; // Create a final reference for the anonymous class

        BukkitRunnable monitor = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !opponent.isOnline() || apiRef == null || !apiRef.isInFight(player)) {
                    stopMonitoring(player.getUniqueId());
                    return;
                }

                Location playerLoc = player.getLocation();
                Location oppLoc = opponent.getLocation();
                Vector aimDirection = player.getLocation().getDirection();
                boolean isHitting = player.isBlocking();

                // Format the data
                String data = String.format(
                    "§6=== Combat Data ===\n" +
                    "§fYour Position: §a%.2f, %.2f, %.2f\n" +
                    "§fOpponent Position: §c%.2f, %.2f, %.2f\n" +
                    "§fAim Direction: §e%.2f, %.2f, %.2f\n" +
                    "§fHitting: §b%s",
                    playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(),
                    oppLoc.getX(), oppLoc.getY(), oppLoc.getZ(),
                    aimDirection.getX(), aimDirection.getY(), aimDirection.getZ(),
                    isHitting ? "Yes" : "No"
                );

                player.sendMessage(data);
            }
        };

        monitor.runTaskTimer(plugin, 0L, 10L); // 10 ticks = 0.5 seconds
        activeMonitors.put(player.getUniqueId(), monitor);
    }

    public void stopMonitoring(UUID playerUUID) {
        BukkitRunnable monitor = activeMonitors.remove(playerUUID);
        if (monitor != null) {
            monitor.cancel();
        }
    }

    public boolean hasAICoach(UUID playerUUID) {
        return aiCoachEnabled.contains(playerUUID);
    }
}
