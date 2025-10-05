package lol.siwoo.faramcpracticecore.aa.aegis;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Aegis implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("faramcpracticecore.admin")) {
            p.sendMessage(ChatColor.GRAY + "Unknown command. Type " + ChatColor.RED + " /help " + ChatColor.GRAY + "for help.");
            return true;
        }

        return true;
    }
}
