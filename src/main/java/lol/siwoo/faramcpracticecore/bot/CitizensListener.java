//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.npc.CitizensNPC;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

public class CitizensListener implements Listener {

    @EventHandler
    public void onPearl(final ProjectileHitEvent e) {
        if (e.getEntity().hasMetadata("BOT_PEARL") && e.getEntity().getShooter() != null && e.getEntity().getShooter() instanceof LivingEntity) {
            for(final CitizensNPC npc : CitizensNPC.npcs) {
                if (npc.getBukkitEntity() == e.getEntity().getShooter()) {
                    npc.getNPC().getNavigator().cancelNavigation();
                    (new BukkitRunnable() {
                        public void run() {
                            if (npc.getNPC() != null && npc.getBukkitEntity() != null) {
                                npc.getNPC().teleport(e.getEntity().getLocation().add((double)0.0F, (double)1.0F, (double)0.0F), TeleportCause.ENDER_PEARL);
                                npc.getBukkitEntity().damage(0.2);
                            }

                        }
                    }).runTaskLater(StrikePractice.getInstance(), 1L);
                    return;
                }
            }
        }

    }

    @EventHandler
    public void onSpawn(NPCSpawnEvent e) {
        if (e.getNPC() != null && e.getNPC().getEntity() instanceof Damageable) {
            Damageable damageable = (Damageable)e.getNPC().getEntity();
            if (damageable != null && damageable.getHealth() <= (double)0.0F) {
                e.setCancelled(true);
            }
        }
    }
}
