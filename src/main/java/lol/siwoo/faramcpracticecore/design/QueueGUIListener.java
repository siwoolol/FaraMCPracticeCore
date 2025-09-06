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
        if (itemName.contains("Boxing")) {
            player.performCommand("queue boxing");
            player.closeInventory();
        } else if (itemName.contains("Nodebuff")) {
            player.performCommand("queue nodebuff");
            player.closeInventory();
        } else if (itemName.contains("BuildUHC")) {
            player.performCommand("queue builduhc");
            player.closeInventory();
        } else if (itemName.contains("Sumo")) {
            player.performCommand("queue sumo");
            player.closeInventory();
        } else if (itemName.contains("Sumo (Best of 3)")) {
            player.performCommand("queue sumobestof3");
            player.closeInventory();
        } else if (itemName.contains("Soup")) {
            player.performCommand("queue soup");
            player.closeInventory();
        } else if (itemName.contains("Axe")) {
            player.performCommand("queue axe");
            player.closeInventory();
        } else if (itemName.contains("Combo")) {
            player.performCommand("queue combo");
            player.closeInventory();
        } else if (itemName.contains("BedFight")) {
            player.performCommand("queue bedfight");
            player.closeInventory();
        } else if (itemName.contains("Fireball Fight")) {
            player.performCommand("fireballfight");
            player.closeInventory();
        }
    }
}