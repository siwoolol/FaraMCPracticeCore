package lol.siwoo.drizzyPracticeCore.lobby;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class Flight implements CommandExecutor {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Player p = (Player) sender;
        if (!p.hasPermission("drizzypracticecore.fly")) {
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("drizzypracticecore.fly")) {
            p.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        Player p = (Player) e.getFight().getPlayersInFight();
        p.setAllowFlight(false);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        Player p = (Player) e.getFight().getPlayersInFight();
        if (!p.hasPermission("drizzypracticecore.fly")) {
            p.setAllowFlight(true);
        }
    }
}
