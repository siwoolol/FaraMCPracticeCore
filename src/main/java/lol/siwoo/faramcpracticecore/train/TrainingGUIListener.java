package lol.siwoo.faramcpracticecore.train;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
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

        // Register packet listener for NPC clicks [[1]](https://bukkit.org/threads/entity-shown-only-to-specific-player-s.155715/)
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (trainingManager.isInTraining(player)) {
                    int entityId = event.getPacket().getIntegers().read(0);
                    trainingManager.handleNPCClick(player, entityId);
                }
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Training Modes")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);

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
            // Return to main menu logic
        }
    }
}