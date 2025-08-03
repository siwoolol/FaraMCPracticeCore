package lol.siwoo.faramcpracticecore.gameplay;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Fireball {
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack hand = p.getItemInHand();

        if (hand.equals(Material.FIREBALL) && hand.getAmount() > 0) {
            hand.setAmount(hand.getAmount() - 1);
            shootFireball(p);
        }
    }

    public void shootFireball(Player p) {
        if (p.getItemInHand().getType() == Material.FIREBALL) {
            // Logic to shoot a fireball
            p.launchProjectile(org.bukkit.entity.Fireball.class);
            p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1); // Decrease fireball count
        }
    }
}
