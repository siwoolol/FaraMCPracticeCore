package lol.siwoo.acemcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.acemcpracticecore.FaraMCPracticeCore;
import lol.siwoo.acemcpracticecore.design.FightEnd;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Boxing implements Listener {

    private final FaraMCPracticeCore plugin;
    StrikePracticeAPI api = StrikePractice.getAPI();

    public Boxing(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        // Check if the kit is boxing
        if (!e.getFight().getKit().getName().equalsIgnoreCase("boxing")) {
            return;
        }

        plugin.getLogger().info("Boxing has been detected");

        // Give speed effect to all players in the fight
        e.getFight().getPlayersInFight().forEach(player -> {
            player.removePotionEffect(PotionEffectType.SPEED); // Remove any existing speed effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
        });
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("boxing")) {
            return;
        }

        e.getFight().getPlayersInFight().forEach(player -> {
            player.removePotionEffect(PotionEffectType.SPEED);
        });
    }
}
