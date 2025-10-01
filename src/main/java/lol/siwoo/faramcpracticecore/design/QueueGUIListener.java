package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.events.DuelStartEvent;
import ga.strikepractice.events.KitSelectEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.stream.events.StartDocument;

public class QueueGUIListener implements Listener {

    private final FaraMCPracticeCore plugin;

    public QueueGUIListener(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.equals(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Unranked Queue")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Execute queue commands based on clicked item
        if (clickedItem.getType().equals(Material.REDSTONE_BLOCK)) {
            player.performCommand("queue leave");
            newafterActivities(event);
        } else if (itemName.contains("Boxing")) {
            player.performCommand("queue boxing");
            afterActivities(event);
        } else if (itemName.contains("Nodebuff")) {
            player.performCommand("queue nodebuff");
            afterActivities(event);
        } else if (itemName.contains("BuildUHC")) {
            player.performCommand("queue builduhc");
            afterActivities(event);
        } else if (itemName.contains("Sumo (Best of 3)")) {
            player.performCommand("queue sumobestof3");
            afterActivities(event);
        } else if (itemName.contains("Sumo")) {
            player.performCommand("queue sumo");
            afterActivities(event);
        } else if (itemName.contains("Soup")) {
            player.performCommand("queue soup");
            afterActivities(event);
        } else if (itemName.contains("Axe")) {
            player.performCommand("queue axepvp");
            afterActivities(event);
        } else if (itemName.contains("Combo")) {
            player.performCommand("queue combo");
            afterActivities(event);
        } else if (itemName.contains("Gapple")) {
            player.performCommand("queue gapple");
            afterActivities(event);
        } else if (itemName.contains("BedFight")) {
            player.performCommand("queue bedfight");
            afterActivities(event);
        } else if (itemName.contains("Fireball Fight")) {
            player.performCommand("queue fireballfight");
            afterActivities(event);
        }
    }

    public void afterActivities(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.WOOD_CLICK, 1, 1);
        p.openInventory(UnrankedGUI.createQueueGUI(p, 0, "n word"));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.openInventory(UnrankedGUI.createQueueGUI(p, e.getSlot(), e.getCurrentItem().getItemMeta().getDisplayName()));
            }
        }.runTaskLater(plugin, 0L);
    }

    public void newafterActivities(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.WOOD_CLICK, 1, 1);
        p.openInventory(UnrankedGUI.createQueueGUI(p, 0, "n word"));
    }

    @EventHandler
    public void onMatchStart(KitSelectEvent e) {
        Player p = e.getPlayer();

        p.closeInventory();
    }
}