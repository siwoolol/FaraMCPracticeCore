package lol.siwoo.faramcpracticecore.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class QueueGUI {

    public Inventory createQueueGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_PURPLE + "Queue Selection");

        // Fill background
        fillBackground(gui);

        // Add queue items
        addQueueItem(gui, 10, Material.DIAMOND_SWORD, "Unranked",
                ChatColor.GREEN + "Unranked",
                "Click to join unranked queue!");

        addQueueItem(gui, 11, Material.GOLD_SWORD, "Ranked",
                ChatColor.GOLD + "Ranked",
                "Click to join ranked queue!");

        addQueueItem(gui, 12, Material.GOLDEN_APPLE, "BuildUHC",
                ChatColor.YELLOW + "BuildUHC",
                "Click to join BuildUHC queue!");

        addQueueItem(gui, 13, Material.POTION, "NoDebuff",
                ChatColor.LIGHT_PURPLE + "NoDebuff",
                "Click to join NoDebuff queue!");

        addQueueItem(gui, 14, Material.STICK, "Combo",
                ChatColor.RED + "Combo",
                "Click to join Combo queue!");

        addQueueItem(gui, 15, Material.SLIME_BALL, "Sumo",
                ChatColor.AQUA + "Sumo",
                "Click to join Sumo queue!");

        addQueueItem(gui, 16, Material.LEATHER_CHESTPLATE, "Boxing",
                ChatColor.DARK_RED + "Boxing",
                "Click to join Boxing queue!");

        addQueueItem(gui, 19, Material.BOW, "Archer",
                ChatColor.DARK_GREEN + "Archer",
                "Click to join Archer queue!");

        return gui;
    }

    private void fillBackground(Inventory gui) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        // Fill empty slots
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }
    }

    private void addQueueItem(Inventory gui, int slot, Material material, String gameMode,
                              String displayName, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + description,
                "",
                ChatColor.GREEN + "Click to join!"
        ));

        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
}