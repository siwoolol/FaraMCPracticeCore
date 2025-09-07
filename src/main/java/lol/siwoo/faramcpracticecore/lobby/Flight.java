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
            p.sendMessage(ChatColor.RED + "Purchase Ranks to Access This Command - https://store.faramc.uk/");
            return true;
        }

        if (api.isInFight(p)) {
            return true;
        }

        if (!p.getAllowFlight()) {
            p.setAllowFlight(true);
        } else if (p.getAllowFlight()) {
            p.setAllowFlight(false);
        }
        return true;
    }
}
