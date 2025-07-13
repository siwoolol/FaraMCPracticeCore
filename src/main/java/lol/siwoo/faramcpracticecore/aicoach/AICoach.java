package lol.siwoo.faramcpracticecore.aicoach;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AICoach implements CommandExecutor {
    private final Set<UUID> aiCoachEnabled = new HashSet<>();

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
            player.sendMessage(ChatColor.RED + "AI Coach disabled!");
        } else {
            aiCoachEnabled.add(playerUUID);
            player.sendMessage(ChatColor.GREEN + "✨AI Coach is here! Type /ai to experience top-quality AI Experiences!");
        }

        return true;
    }

    public boolean hasAICoach(UUID playerUUID) {
        return aiCoachEnabled.contains(playerUUID);
    }
}
