package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.events.FightEndEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WindFight implements Listener {

    private final FaraMCPracticeCore plugin;
    private final Map<String, String> fightIds;
    private int fightCounter = 0;

    public WindFight(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.fightIds = new HashMap<>();
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("windfight")) {
            return;
        }

        String fightId = "windfight_" + (++fightCounter) + "_" + System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getFight().getPlayersInFight().forEach(p -> {
                    UUID playerId = p.getUniqueId();
                    fightIds.put(playerId.toString(), fightId);
                });
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("windfight")) {
            return;
        }

        String fightId = null;
        for (Player p : e.getFight().getPlayersInFight()) {
            fightId = fightIds.get(p.getUniqueId().toString());
            if (fightId != null) {
                break;
            }
        }

        if (fightId != null) {
            final String finalFightId = fightId;
            plugin.getLogger().info("Fight ended with ID: " + fightId + ".");
        }

        e.getFight().getPlayersInFight().forEach(p -> {
            UUID playerId = p.getUniqueId();
            fightIds.remove(playerId.toString());
        });
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getItem().getItemMeta().getDisplayName().equals("Launch")) {
            launchPlayer(p);
        } else if (e.getItem().getItemMeta().getDisplayName().equals("Push")) {
            pushEntity(p);
        }
    }

    public void launchPlayer(Player p) {
        World world = p.getWorld();
        Location center = p.getLocation();
        Vector direction = p.getLocation().getDirection().normalize();

        p.setVelocity(direction.multiply(6));
        world.playEffect(center, Effect.EXPLOSION_HUGE, 0);
    }

    public void pushEntity(Player p) {
        World world = p.getWorld();
        Location center = p.getLocation();

        world.getNearbyEntities(center, 10, 10, 10).forEach(entity -> {
            if (entity != p) {
                Location entityLoc = entity.getLocation();
                Vector push = entityLoc.toVector().subtract(center.toVector());

                if (push.length() > 0) {
                    push.normalize().multiply(3.0);
                    push.setY(1.2);
                    entity.setVelocity(push);

                    world.playEffect(center, Effect.CLOUD, 0);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        fightIds.remove(playerId.toString());
    }
}