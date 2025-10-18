package lol.siwoo.faramcpracticecore.fix;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * Modifies the velocity of thrown splash potions to mimic the mechanics of servers like Minemen.club.
 * This results in a faster and flatter potion trajectory.
 */
public class PotThrowMech implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SPLASH_POTION) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) event.getEntity().getShooter();
        ThrownPotion potion = (ThrownPotion) event.getEntity();

        potion.setVelocity(p.getEyeLocation().getDirection().multiply(0.67));
    }
}
