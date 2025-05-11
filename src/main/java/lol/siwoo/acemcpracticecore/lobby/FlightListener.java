package lol.siwoo.acemcpracticecore.lobby;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.BotDuelEndEvent;
import ga.strikepractice.events.DuelEndEvent;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FlightListener implements Listener {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("faramcpracticecore.fly")) {
            p.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        e.getFight().getPlayersInFight().forEach( player -> {
            player.setAllowFlight(false);
        });
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        e.getFight().getPlayersInFight().forEach( player -> {
            if (player.hasPermission("faramcpracticecore.fly")) {
                player.setAllowFlight(true);
            }
        });
    }

    @EventHandler
    public void onDuelEnd(DuelEndEvent e) {
        e.getFight().getPlayersInFight().forEach( player -> {
            if (player.hasPermission("faramcpracticecore.fly")) {
                player.setAllowFlight(true);
            }
        });
    }

    @EventHandler
    public void onBotDuelEnd(BotDuelEndEvent e) {
        e.getFight().getPlayersInFight().forEach( player -> {
            if (player.hasPermission("faramcpracticecore.fly")) {
                player.setAllowFlight(true);
            }
        });
    }
}
