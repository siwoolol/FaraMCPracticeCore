package lol.siwoo.acemcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Boxing implements Listener {

    StrikePracticeAPI api = StrikePractice.getAPI();

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        // Check if the kit is boxing
        if (!e.getFight().getKit().getName().equalsIgnoreCase("boxing")) {
            return;
        }

        // Give speed effect to all players in the fight
        e.getFight().getPlayersInFight().forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        });
    }
}
