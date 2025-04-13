package lol.siwoo.drizzyPracticeCore.party;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getPlayer;

public class HurryUpPartyOwner implements CommandExecutor {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Player p = (Player) sender;

        if (api.getParty(p) == null) {
            p.sendMessage(ChatColor.RED + "You are not in a Party!");
            return true;
        }

        String ownerString = api.getParty(p).getOwner();
        Player owner = getPlayer(ownerString);

        owner.sendMessage(ChatColor.AQUA + p.getName() + " has Asked You to Hurry up!");
        p.sendMessage(ChatColor.GREEN + "You have Asked The Owner of the Party to Hurry up!");

        return true;
    }
}
