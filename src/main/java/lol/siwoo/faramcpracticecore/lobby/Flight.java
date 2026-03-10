package lol.siwoo.faramcpracticecore.lobby;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.design.MessageStyle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Flight implements CommandExecutor {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(MessageStyle.error("This command can only be used by a player."));
            return true;
        }
        if (!p.hasPermission("faramcpracticecore.fly")) {
            p.sendMessage(Component.empty()
                    .append(MessageStyle.error("Rank required. "))
                    .append(Component.text("store.faramc.uk", NamedTextColor.AQUA)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl("https://store.faramc.uk/"))));
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
