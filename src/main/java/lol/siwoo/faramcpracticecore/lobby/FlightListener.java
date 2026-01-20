package lol.siwoo.faramcpracticecore.lobby;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FlightListener implements Listener {

    StrikePracticeAPI api = StrikePractice.getAPI();

    public static void enableFlight(Player p) {
        if (p.hasPermission("faramcpracticecore.fly")) {
            p.setAllowFlight(true);
        }
    }

    // Disable Flight
    @EventHandler
    public void onFightStart(FightStartEvent e) {
        e.getFight().getPlayersInFight().forEach( player -> {
            player.setAllowFlight(false);
        });
    }

    // Enable Flight
    // Single Player
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        enableFlight(e.getPlayer());
    }

    @EventHandler
    public void onPlayerStopSpectating(PlayerStopSpectatingEvent e) {
        enableFlight(e.getPlayer());
    }

    // Multiple Players
    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }

    @EventHandler
    public void onDuelEnd(DuelEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }

    @EventHandler
    public void onBotDuelEnd(BotDuelEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }

    @EventHandler
    public void onPartySplitEnd(PartySplitEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }

    @EventHandler
    public void onPartyVSPartyEnd(PartyVsPartyEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }

    @EventHandler
    public void onPartyVSBotsEnd(PartyVsBotsEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }

    @EventHandler
    public void onPartyFFAEnd(PartyFFAEndEvent e) {
        e.getFight().getPlayersInFight().forEach(FlightListener::enableFlight);
    }
}
