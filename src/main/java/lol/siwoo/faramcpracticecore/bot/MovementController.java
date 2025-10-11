package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCKnockbackEvent;
import net.citizensnpcs.api.event.NPCLinkToPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class MovementController implements Listener {
    private final FaraMCPracticeCore plugin;

    public MovementController(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBotDuelStart(BotDuelStartEvent event) {
        NPC n = event.getBot();
        int nId = n.getId();

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().getLogger().info("bot_difficulty: " + event.getFight().getDifficulty() + ", bot_name: " + n.getName());

                String displayName = ChatColor.RED + "[★★★★★] Bugged Bot";
                if (event.getFight().getDifficulty().toString().equals("EASY")) {
                    displayName = ChatColor.GREEN + "[★] Easy Bot";
                } else if (event.getFight().getDifficulty().toString().equals("NORMAL")) {
                    displayName = ChatColor.YELLOW + "[★★] Normal Bot";
                } else if (event.getFight().getDifficulty().toString().equals("HARD")) {
                    displayName = ChatColor.RED + "[★★★] Hard Bot";
                } else if (event.getFight().getDifficulty().toString().equals("EXPERT")) {
                    displayName = ChatColor.DARK_RED + "[★★★★] Expert Bot";
                } else {
                    event.setCancelled(true);
                }

                n.setName(displayName);
                n.data().set("display-name", displayName);
                n.getEntity().setCustomNameVisible(true);
                n.getEntity().setCustomName(displayName);

                setNPCSpeed(nId, 0.38F);
            }
        }.runTaskLater(plugin, 5L);
    }

    @EventHandler
    public void onBotMovement(NPCLinkToPlayerEvent e) {
        NPC npc = e.getNPC();
        UUID playerUUID = e.getPlayer().getUniqueId();
        int nId = npc.getId();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPlayer(playerUUID) != null && npc.isSpawned()) {
                    setNPCSpeed(nId, 2.33F);
                    npc.faceLocation(Bukkit.getPlayer(playerUUID).getLocation());
                } else {
                    resetNPCSpeed(nId);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onBotHit(NPCDamageByEntityEvent e) {
        e.getNPC().faceLocation(e.getDamager().getLocation());

        NPC npc = e.getNPC();
        if (npc.getEntity() != null && e.getDamager() != null) {
            npc.getNavigator().getLocalParameters().speed(0.77F);

            Vector knockbackDirection = npc.getEntity().getLocation().toVector()
                    .subtract(e.getDamager().getLocation().toVector()).normalize();

            knockbackDirection.multiply(0.4F);
            knockbackDirection.setY(0.2F);

            npc.getEntity().setVelocity(knockbackDirection);
        }
    }

    @EventHandler
    public void onBotKnockBack(NPCKnockbackEvent e) {
        e.getKnockbackVector().multiply(0.4F).setY(0.2F);
    }

    public void setNPCSpeed(int nId, float speed) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.getById(nId);

        if (npc != null && npc.isSpawned()) {
            npc.getNavigator().getLocalParameters().speed(speed);
        }
    }

    public void resetNPCSpeed(int nId) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.getById(nId);

        if (npc != null && npc.isSpawned()) {
            npc.getNavigator().getLocalParameters().speed(1.0F);
        }
    }
}