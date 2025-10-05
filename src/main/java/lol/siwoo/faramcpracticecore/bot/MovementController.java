package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelStartEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MovementController implements Listener {

    @EventHandler
    public void onBotDuelStart(BotDuelStartEvent event) {
        NPC n = event.getBot();
        int nId = n.getId();

        setNPCSpeed(nId, 1.33F);
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