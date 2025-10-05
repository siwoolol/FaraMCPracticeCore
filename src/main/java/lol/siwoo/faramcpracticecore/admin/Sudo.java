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
            sender.sendMessage(ChatColor.GRAY + "Unknown command. Type" + ChatColor.RED + " /help " + ChatColor.GRAY + "for help.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Wrong Usage!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        StringBuilder command = new StringBuilder();

        if (args.length != 2) {
            for (int i = 1; i < args.length; i++) {
                if (i != 1)  {
                    command.append(" ");
                }
                command.append(args[i]);
            }
        } else {
            command = new StringBuilder(args[1]);
        }

        if (Objects.equals(target.getName(), "siwoolol")) {
            sender.sendMessage(ChatColor.RED + "You can't troll me my sneaky mate");
            return true;
        }

        if (String.valueOf(command).startsWith("c:")) {
            target.chat(String.valueOf(command).substring(2));
            Bukkit.getServer().getLogger().info(target.getName() + " was sudoed by " + sender.getName() + " to " + command);
            return true;
        }

        Bukkit.dispatchCommand(target, String.valueOf(command));
        Bukkit.getServer().getLogger().info(target.getName() + " was sudoed by " + sender.getName() + " to issue server command: /" + command);
        return true;
    }
}
