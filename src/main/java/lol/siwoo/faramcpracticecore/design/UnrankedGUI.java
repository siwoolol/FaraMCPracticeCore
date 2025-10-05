package lol.siwoo.faramcpracticecore.design;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class UnrankedGUI implements CommandExecutor, Listener {

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

    public static Inventory createQueueGUI(Player p, int slot, String name) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Unranked Queue");

        // Fill background
        fillBackground(gui);

        // Add queue items
        addQueueItem(gui, 10, Material.DIAMOND_CHESTPLATE, "Boxing",
                ChatColor.GREEN + "Boxing",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_boxing%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_boxing%", p);
        addQueueItem(gui, 11, createRegenerationPotion().getType(), "Nodebuff",
                ChatColor.GOLD + "Nodebuff",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_nodebuff%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_nodebuff%", p);
        addQueueItem(gui, 12, Material.LAVA_BUCKET, "BuildUHC",
                ChatColor.YELLOW + "BuildUHC",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_builduhc%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_builduhc%", p);
        addQueueItem(gui, 13, Material.LEASH, "Sumo",
                ChatColor.LIGHT_PURPLE + "Sumo",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_sumo%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_sumo%", p);
        addQueueItem(gui, 14, Material.LEASH, "Sumo (Best of 3)",
                ChatColor.RED + "Sumo (Best of 3)",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_sumobestof3%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_sumobestof3%", p);
        addQueueItem(gui, 15, Material.MUSHROOM_SOUP, "Soup",
                ChatColor.AQUA + "Soup",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_soup%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_soup%", p);
        addQueueItem(gui, 16, Material.DIAMOND_AXE, "Axe",
                ChatColor.DARK_RED + "Axe",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_axepvp%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_axepvp%", p);
        addQueueItem(gui, 19, Material.FISHING_ROD, "Combo",
                ChatColor.DARK_GREEN + "Combo",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_combo%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_combo%", p);
        addQueueItem(gui, 20, Material.GOLDEN_APPLE, "Gapple",
                ChatColor.DARK_GREEN + "Gapple",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_gapple%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_gapple%", p);
        addQueueItem(gui, 21, Material.BED, "BedFight",
                ChatColor.DARK_GREEN + "BedFight",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_bedfight%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_bedfight%", p);
        addQueueItem(gui, 22, Material.FIREBALL, "Fireball Fight",
                ChatColor.DARK_GREEN + "Fireball Fight",
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_fireballfight%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_fireballfight%", p);

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
                                     String displayName, String queued, String playing, Player p) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayName);
        queued = PlaceholderAPI.setPlaceholders(p, queued);
        playing = PlaceholderAPI.setPlaceholders(p, playing);
        meta.setLore(Arrays.asList(
                queued,
                playing,
                "",
                ChatColor.GREEN + "Click to join!"
        ));

        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }

    @EventHandler
    public void onQueueCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (e.getMessage().equalsIgnoreCase("/queue") || e.getMessage().equalsIgnoreCase("/unranked")) {
            e.setCancelled(true);
            Bukkit.dispatchCommand(p, "/unrankedgui");
        }
    }
}