package lol.siwoo.faramcpracticecore.design;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class UnrankedGUI implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "this command can only be used by a player retard.");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(createQueueGUI(player, 0, "n word"));

        return true;
    }

    public static Inventory createQueueGUI(Player player, int slot, String name) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Unranked Queue");

        // Fill background
        fillBackground(gui);

        // Add queue items
        addQueueItem(gui, 10, Material.DIAMOND_CHESTPLATE, "Boxing",
                ChatColor.GREEN + "Boxing",
                "Click to join Boxing queue!");
        addQueueItem(gui, 11, createRegenerationPotion().getType(), "Nodebuff",
                ChatColor.GOLD + "Nodebuff",
                "Click to join Nodebuff queue!");
        addQueueItem(gui, 12, Material.LAVA_BUCKET, "BuildUHC",
                ChatColor.YELLOW + "BuildUHC",
                "Click to join BuildUHC queue!");
        addQueueItem(gui, 13, Material.LEASH, "Sumo",
                ChatColor.LIGHT_PURPLE + "Sumo",
                "Click to join Sumo queue!");
        addQueueItem(gui, 14, Material.LEASH, "Sumo (Best of 3)",
                ChatColor.RED + "Sumo (Best of 3)",
                "Click to join Sumo (Best of 3) queue!");
        addQueueItem(gui, 15, Material.MUSHROOM_SOUP, "Soup",
                ChatColor.AQUA + "Soup",
                "Click to join Soup queue!");
        addQueueItem(gui, 16, Material.DIAMOND_AXE, "Axe",
                ChatColor.DARK_RED + "Axe",
                "Click to join Axe queue!");
        addQueueItem(gui, 19, Material.FISHING_ROD, "Combo",
                ChatColor.DARK_GREEN + "Combo",
                "Click to join Combo queue!");
        addQueueItem(gui, 20, Material.GOLDEN_APPLE, "Gapple",
                ChatColor.DARK_GREEN + "Gapple",
                "Click to join Gapple queue!");
        addQueueItem(gui, 21, Material.BED, "BedFight",
                ChatColor.DARK_GREEN + "BedFight",
                "Click to join BedFight queue!");
        addQueueItem(gui, 22, Material.FIREBALL, "Fireball Fight",
                ChatColor.DARK_GREEN + "Fireball Fight",
                "Click to join Weird Ass Fireball Fight queue!");

        if (slot != 0) {
            ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(ChatColor.GOLD + "Queued for " + name);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "You are currently Queued for the " + name + " Queue",
                    "",
                    ChatColor.RED + "Click Again to Leave the Queue!"
            ));

            item.setItemMeta(meta);
            gui.setItem(slot, item);
        }

        return gui;
    }

    private static void fillBackground(Inventory gui) {
        ItemStack glass = new ItemStack(Material.THIN_GLASS, 1, (short) 7);
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

    public static ItemStack createRegenerationPotion() {
        ItemStack regenerationPotion = new ItemStack(Material.POTION);

        PotionMeta potionMeta = (PotionMeta) regenerationPotion.getItemMeta();

        PotionEffect regenerationEffect = new PotionEffect(
                PotionEffectType.REGENERATION,
                10 * 20,
                10
        );

        potionMeta.addCustomEffect(regenerationEffect, true);
        regenerationPotion.setItemMeta(potionMeta);
        return regenerationPotion;
    }

    private static void addQueueItem(Inventory gui, int slot, Material material, String gameMode,
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