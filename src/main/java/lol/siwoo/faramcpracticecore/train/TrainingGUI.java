package lol.siwoo.faramcpracticecore.train;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TrainingGUI {

    public static Inventory createTrainingGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GOLD + "" + ChatColor.BOLD + "Training Modes");

        // Strafe Training
        ItemStack strafeItem = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta strafeMeta = strafeItem.getItemMeta();
        strafeMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Strafe Training");
        strafeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Practice your movement skills",
                ChatColor.GRAY + "Learn to strafe effectively",
                "",
                ChatColor.YELLOW + "Click to start training!"
        ));
        strafeItem.setItemMeta(strafeMeta);
        inventory.setItem(10, strafeItem);

        // Aim Tracker
        ItemStack aimItem = new ItemStack(Material.BOW);
        ItemMeta aimMeta = aimItem.getItemMeta();
        aimMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Aim Tracker");
        aimMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Improve your aim precision",
                ChatColor.GRAY + "Track moving targets",
                "",
                ChatColor.YELLOW + "Click to start training!"
        ));
        aimItem.setItemMeta(aimMeta);
        inventory.setItem(12, aimItem);

        // CPS Tester
        ItemStack cpsItem = new ItemStack(Material.COMPARATOR);
        ItemMeta cpsMeta = cpsItem.getItemMeta();
        cpsMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "CPS Tester");
        cpsMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Test your clicks per second",
                ChatColor.GRAY + "Measure your clicking speed",
                "",
                ChatColor.YELLOW + "Click to start testing!"
        ));
        cpsItem.setItemMeta(cpsMeta);
        inventory.setItem(14, cpsItem);

        // W-Tap Trainer
        ItemStack wtapItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta wtapMeta = wtapItem.getItemMeta();
        wtapMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "W-Tap Trainer");
        wtapMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Master the w-tap technique",
                ChatColor.GRAY + "Learn advanced PvP mechanics",
                "",
                ChatColor.YELLOW + "Click to start training!"
        ));
        wtapItem.setItemMeta(wtapMeta);
        inventory.setItem(16, wtapItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return to main menu"));
        backItem.setItemMeta(backMeta);
        inventory.setItem(22, backItem);

        return inventory;
    }
}