package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelStartEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MovementController implements Listener {

    @EventHandler
    public void onBotDuelStart(BotDuelStartEvent event) {
        NPC n = event.getBot();
        int nId = n.getId();

        Bukkit.getServer().getLogger().info(String.valueOf(event.getFight().getDifficulty()));
        Bukkit.getServer().getLogger().info(String.valueOf(n.getName()));

        if (event.getFight().getDifficulty().equals("EASY")) {
            n.setName(ChatColor.GREEN + "Easy Bot");
        } else if (event.getFight().getDifficulty().equals("NORMAL")) {
            n.setName(ChatColor.YELLOW + "Normal Bot");
        } else if (event.getFight().getDifficulty().equals("HARD")) {
            n.setName(ChatColor.RED + "Hard Bot");
        } else if (event.getFight().getDifficulty().equals("EXPERT")) {
            n.setName(ChatColor.DARK_RED + "Expert Bot");
        } else {
            event.setCancelled(true);
        }

        setNPCSpeed(nId, 1.44F);
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