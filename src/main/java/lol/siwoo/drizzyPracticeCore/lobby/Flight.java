package lol.siwoo.drizzyPracticeCore.lobby;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class Flight implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Player p = (Player) sender;
        if (!p.hasPermission("drizzypracticecore.fly")) {
            p.sendMessage("You don't have permissions to execute this command.");
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
