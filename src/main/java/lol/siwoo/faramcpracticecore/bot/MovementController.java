package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelStartEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MovementController implements Listener {

    @EventHandler
    public void onBotDuelStart(BotDuelStartEvent event) {
        NPC n = event.getBot();
        int nId = n.getId();

        if (event.getFight().getDifficulty().equals("easy")) {
            n.setName(ChatColor.GREEN + "Easy Bot");
        } else if (event.getFight().getDifficulty().equals("normal")) {
            n.setName(ChatColor.YELLOW + "Normal Bot");
        } else if (event.getFight().getDifficulty().equals("hard")) {
            n.setName(ChatColor.RED + "Hard Bot");
        } else if (event.getFight().getDifficulty().equals("expert")) {
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