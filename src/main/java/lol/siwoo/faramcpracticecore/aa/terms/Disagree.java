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
        p.kickPlayer(ChatColor.LIGHT_PURPLE + "{FaraMC Practice Beta}\n\n" +
                ChatColor.GRAY + "You have to agree to the" + ChatColor.RED + " Beta Terms and Conditions " + ChatColor.GRAY + "in order to play on the beta version of this server.\n" +
                ChatColor.GRAY + "Please agree to the terms to continue playing the beta version of the server.\n" +
                ChatColor.GRAY + "If you disagree, you will be kicked from the server.\n\n" +
                ChatColor.ITALIC + "Learn more at: " + ChatColor.ITALIC + ChatColor.DARK_AQUA + "https://sw.faramc.uk/faramc-beta-terms");
    }
}
