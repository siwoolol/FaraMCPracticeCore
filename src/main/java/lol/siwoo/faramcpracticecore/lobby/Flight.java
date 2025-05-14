package lol.siwoo.faramcpracticecore.lobby;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Flight implements CommandExecutor {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Player p = (Player) sender;
        if (!p.hasPermission("faramcpracticecore.fly")) {
            p.sendMessage(ChatColor.RED + "You don't have permissions to execute this command.");
            return true;
        }

        if (api.isInFight(p)) {
            p.sendMessage(ChatColor.RED + "You can't fly during a fight.");
            return true;
        }

        if (!p.getAllowFlight()) {
            p.sendMessage(ChatColor.GREEN + "Flight Enabled!");
            p.setAllowFlight(true);
        } else if (p.getAllowFlight()) {
            p.sendMessage(ChatColor.RED + "Flight Disabled!");
            p.setAllowFlight(false);
        }
        return true;
    }
}
