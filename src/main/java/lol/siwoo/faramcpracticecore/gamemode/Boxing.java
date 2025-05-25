package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Boxing implements Listener {

    private final FaraMCPracticeCore plugin;
    StrikePracticeAPI api = StrikePractice.getAPI();

    public Boxing(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("boxing")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("boxing match detected. players:" + e.getFight().getPlayersInFight());

                e.getFight().getPlayersInFight().forEach(p -> {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
                });
            }
        }.runTaskLater(plugin, 20L);
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
