package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelStartEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GiveEffect implements Listener {

    @EventHandler
    public void onBotDuelStart(BotDuelStartEvent event) {
        Player p = event.getPlayer();
        NPC n = event.getBot();

        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false);
        speedEffect.apply((LivingEntity) n);

        p.sendMessage("Debug: Speed boost activated for bot duel!");
    }
}