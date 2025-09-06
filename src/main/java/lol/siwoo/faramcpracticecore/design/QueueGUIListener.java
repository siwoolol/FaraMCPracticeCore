package lol.siwoo.faramcpracticecore.design;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class QueueGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.equals(ChatColor.DARK_PURPLE + "Queue Selection")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Execute queue commands based on clicked item
        if (itemName.contains("Unranked")) {
            player.performCommand("queue unranked");
            player.closeInventory();
        } else if (itemName.contains("Ranked")) {
            player.performCommand("queue ranked");
            player.closeInventory();
        } else if (itemName.contains("BuildUHC")) {
            player.performCommand("queue builduHC");
            player.closeInventory();
        } else if (itemName.contains("NoDebuff")) {
            player.performCommand("queue nodebuff");
            player.closeInventory();
        } else if (itemName.contains("Combo")) {
            player.performCommand("queue combo");
            player.closeInventory();
        } else if (itemName.contains("Sumo")) {
            player.performCommand("queue sumo");
            player.closeInventory();
        } else if (itemName.contains("Boxing")) {
            player.performCommand("queue boxing");
            player.closeInventory();
        } else if (itemName.contains("Archer")) {
            player.performCommand("queue archer");
            player.closeInventory();
        }
    }
}