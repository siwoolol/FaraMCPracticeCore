package lol.siwoo.faramcpracticecore.party;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.design.MessageStyle;
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
            p.sendMessage(MessageStyle.error("You're not in a party."));
            return true;
        }

        String ownerString = api.getParty(p).getOwner();
        Player owner = getPlayer(ownerString);

        if (owner == p) {
            p.sendMessage(MessageStyle.error("You're the party owner."));
            return true;
        }

        p.sendMessage(MessageStyle.success("Nudged the party owner."));
        owner.sendMessage(MessageStyle.infoFromPlayer(p.getName(), "wants you to hurry up!"));

        return true;
    }
}
