package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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

                String displayName = "";
                if (event.getFight().getDifficulty().equals("EASY")) {
                    displayName = ChatColor.GREEN + "[★] Easy Bot";
                } else if (event.getFight().getDifficulty().equals("NORMAL")) {
                    displayName = ChatColor.YELLOW + "[★★] Normal Bot";
                } else if (event.getFight().getDifficulty().equals("HARD")) {
                    displayName = ChatColor.RED + "[★★★] Hard Bot";
                } else if (event.getFight().getDifficulty().equals("EXPERT")) {
                    displayName = ChatColor.DARK_RED + "[★★★★] Expert Bot";
                } else {
                    event.setCancelled(true);
                }

                n.setName(displayName);
                n.data().set("display-name", displayName);

                setNPCSpeed(nId, 0.77F);
            }
        }.runTaskLater(plugin, 5L);
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