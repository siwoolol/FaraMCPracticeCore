package lol.siwoo.faramcpracticecore.admin;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceWin implements CommandExecutor {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player p = (Player) sender;

        if (p.hasPermission("faramcpracticecore.admin")) {
            if (args.length == 0) {
                api.forceWinFight(p);
                p.sendMessage(ChatColor.GREEN + "Force Won The Fight!");
            } else if(args.length == 1) {
                // detect args type
                if(args[0].equalsIgnoreCase("fight")) {
                    api.getFight(p).getPlayersInFight().forEach(player -> {
                        player.teleport(api.getSpawnLocation());
                    });
                    api.forceWinFight(p);
                    p.sendMessage(ChatColor.GREEN + "Force Won The Fight!");
                } else if(args[0].equalsIgnoreCase("round")) {
                    api.forceWinRound(p);
                    p.sendMessage(ChatColor.GREEN + "Force Won The Round!");
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid Option! Only Use (fight/round)");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Usage: /forcewin (fight/round)");
            }
            return true;
        } else {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
    }
}
