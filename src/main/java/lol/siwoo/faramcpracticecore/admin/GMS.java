package lol.siwoo.faramcpracticecore.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GMS implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (!sender.hasPermission("faramcpracticecore.admin")) {
            sender.sendMessage(ChatColor.GRAY + "Unknown command. Type " + ChatColor.RED + " /help " + ChatColor.GRAY + "for help.");
            return true;
        }

        Bukkit.dispatchCommand(sender, "gamemode survival");
        return true;
    }
}
