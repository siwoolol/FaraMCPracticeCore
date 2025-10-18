package lol.siwoo.faramcpracticecore.fix;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class PotThrowMech implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SPLASH_POTION
                || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) event.getEntity().getShooter();
        ThrownPotion potion = (ThrownPotion) event.getEntity();

        potion.setVelocity(p.getEyeLocation().getDirection().multiply(1.05D));
    }
}
