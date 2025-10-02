package lol.siwoo.faramcpracticecore.train;

import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TrainingGUIListener implements Listener {

    private final FaraMCPracticeCore plugin;
    private final TrainingManager trainingManager;

    public TrainingGUIListener(FaraMCPracticeCore plugin, TrainingManager trainingManager) {
        this.plugin = plugin;
        this.trainingManager = trainingManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Training Modes")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 1);

        if (itemName.contains("Strafe Training")) {
            player.closeInventory();
            trainingManager.startTraining(player, TrainingMode.STRAFE);
        } else if (itemName.contains("Aim Tracker")) {
            player.closeInventory();
            trainingManager.startTraining(player, TrainingMode.AIM_TRACKER);
        } else if (itemName.contains("CPS Tester")) {
            player.closeInventory();
            trainingManager.startTraining(player, TrainingMode.CPS_TESTER);
        } else if (itemName.contains("W-Tap Trainer")) {
            player.closeInventory();
            trainingManager.startTraining(player, TrainingMode.W_TAP_TRAINER);
        } else if (clickedItem.getType() == Material.ARROW) {
            player.closeInventory();
            // You can add logic here to return to your main menu
        }
    }
}