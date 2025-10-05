package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.battlekit.BattleKit;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BotHitDelayFix implements Listener {
    private static final long minHitDelay = 490L;
    private final Map<UUID, Long> cooldown = new HashMap();

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.NORMAL
    )
    public void onDamage(EntityDamageEvent e) {
        BattleKit kit = BattleKit.getCurrentKit(e.getEntity());
        if ((kit == null || !kit.isCombo()) && Bukkit.getPlayer(e.getEntity().getUniqueId()) == null) {
            UUID uuid = e.getEntity().getUniqueId();
            if (this.cooldown.containsKey(uuid)) {
                long l = (Long)this.cooldown.get(uuid);
                if (System.currentTimeMillis() - l < 490L) {
                    e.setCancelled(true);
                    return;
                }
            }

            this.cooldown.put(uuid, System.currentTimeMillis());
        }
    }
}