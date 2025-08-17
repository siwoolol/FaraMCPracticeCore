package lol.siwoo.faramcpracticecore.aa.terms;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Timer;

public class Agree implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cant agree to this legal agreement as a console.");
            return true;
        }

        agreeTerms((Player) sender);
        return true;
    }

    public void agreeTerms(Player p) {
        LocalTime time = LocalTime.now();

        p.sendMessage(ChatColor.GREEN + "You have agreed to the terms and conditions to play on this server.\n" +
                ChatColor.GRAY + "(Agreed time: " + time + ")");
    }
}
