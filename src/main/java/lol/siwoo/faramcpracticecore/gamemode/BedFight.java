package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BedFight implements Listener {

    private final FaraMCPracticeCore plugin;
    StrikePracticeAPI api = StrikePractice.getAPI();

    public BedFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("bedfight")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("bedfight match detected. players:" + e.getFight().getPlayersInFight());

                e.getFight().getPlayersInFight().forEach(p -> {
                    cooldown.put(UUID.fromString(p.getUniqueId().toString()), System.currentTimeMillis());
                });
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (cooldown.containsKey(e.getPlayer().getUniqueId())
            && cooldown.get(e.getPlayer().getUniqueId()) > System.currentTimeMillis() - 5000) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (cooldown.containsKey(e.getPlayer().getUniqueId())
                && cooldown.get(e.getPlayer().getUniqueId()) > System.currentTimeMillis() - 5000) {
            e.setCancelled(true);
        }
    }
}
