package lol.siwoo.faramcpracticecore.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Sudo implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (!sender.hasPermission("faramcpracticecore.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (!(args.length == 2)) {
            sender.sendMessage(ChatColor.RED + "Wrong Usage!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        String command = args[1];

        if (Objects.equals(target.getName(), "siwoolol")) {
            sender.sendMessage(ChatColor.RED + "You can't troll me my sneaky mate");
            return true;
        }

        Bukkit.dispatchCommand(target, command);
        return true;
    }
}
