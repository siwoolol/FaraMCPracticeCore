package lol.siwoo.faramcpracticecore.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GMA implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        Bukkit.dispatchCommand(sender, "gamemode adventure");
        return true;
    }
}
