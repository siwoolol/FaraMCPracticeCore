package lol.siwoo.faramcpracticecore.admin;

import lol.siwoo.faramcpracticecore.design.MessageStyle;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GMS implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (!sender.hasPermission("faramcpracticecore.admin")) {
            sender.sendMessage(MessageStyle.error("Unknown command."));
            return true;
        }

        Bukkit.dispatchCommand(sender, "gamemode survival");
        return true;
    }
}
