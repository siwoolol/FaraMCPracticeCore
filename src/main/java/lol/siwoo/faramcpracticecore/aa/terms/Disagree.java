package lol.siwoo.faramcpracticecore.aa.terms;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Disagree implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cant disagree to this legal agreement as a console.");
            return true;
        }

        disagreeTerms((Player) sender);
        return true;
    }

    public void disagreeTerms(Player p) {
        p.kickPlayer(ChatColor.RED + "You have to agree to the terms and conditions to play on this server.\n" +
                ChatColor.YELLOW + "Please read the agreement carefully before proceeding.");
    }
}
