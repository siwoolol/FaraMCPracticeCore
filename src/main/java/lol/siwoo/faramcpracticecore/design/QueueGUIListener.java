package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.battlekit.BattleKit;
import ga.strikepractice.events.DuelStartEvent;
import ga.strikepractice.events.FightStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class QueueGUIListener implements Listener {

    private static FaraMCPracticeCore plugin;
    StrikePracticeAPI api = StrikePractice.getAPI();

    public QueueGUIListener(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Unranked Queue")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        plugin.getLogger().info("DEBUG: Clicked " + itemName + " (" + clickedItem.getType() + ")");

        // Execute queue commands based on clicked item
        if (clickedItem.getType().equals(Material.REDSTONE_BLOCK)) {
            Bukkit.dispatchCommand(player, "queue leave");
            newafterActivities(event);
        } else if (itemName.contains("WindFight")) {
            api.joinQueue(player, BattleKit.getKit("windfight"));
            afterActivities(event);
        } else if (itemName.contains("Boxing")) {
            api.joinQueue(player, BattleKit.getKit("boxing"));
            afterActivities(event);
        } else if (itemName.contains("Nodebuff")) {
            api.joinQueue(player, BattleKit.getKit("nodebuff"));
            afterActivities(event);
        } else if (itemName.contains("BuildUHC")) {
            api.joinQueue(player, BattleKit.getKit("builduhc"));
            afterActivities(event);
        } else if (itemName.contains("Sumo (Best of 3)")) {
            api.joinQueue(player, BattleKit.getKit("sumobestof3"));
            afterActivities(event);
        } else if (itemName.contains("Sumo")) {
            api.joinQueue(player, BattleKit.getKit("sumo"));
            afterActivities(event);
        } else if (itemName.contains("Soup")) {
            api.joinQueue(player, BattleKit.getKit("soup"));
            afterActivities(event);
        } else if (itemName.contains("Axe")) {
            api.joinQueue(player, BattleKit.getKit("axepvp"));
            afterActivities(event);
        } else if (itemName.contains("Gapple")) {
            api.joinQueue(player, BattleKit.getKit("gapple"));
            afterActivities(event);
        } else if (itemName.contains("BedFight")) {
            api.joinQueue(player, BattleKit.getKit("bedfight"));
            afterActivities(event);
        } else if (itemName.contains("Fireball Fight")) {
            api.joinQueue(player, BattleKit.getKit("fireballfight"));
            afterActivities(event);
        } else if (itemName.contains("SkyWars")) {
            api.joinQueue(player, BattleKit.getKit("skywars"));
            afterActivities(event);
        } else if (itemName.contains("Archer")) {
            api.joinQueue(player, BattleKit.getKit("archer"));
            afterActivities(event);
        } else if (itemName.contains("No Enchant")) {
            api.joinQueue(player, BattleKit.getKit("noenchant"));
            afterActivities(event);
        } else if (itemName.contains("Spleef")) {
            api.joinQueue(player, BattleKit.getKit("spleef"));
            afterActivities(event);
        } else if (itemName.contains("SG")) {
            api.joinQueue(player, BattleKit.getKit("sg"));
            afterActivities(event);
        } else if (itemName.contains("Sword")) {
            api.joinQueue(player, BattleKit.getKit("sword"));
            afterActivities(event);
        } else if (itemName.contains("Combo Tag")) {
            api.joinQueue(player, BattleKit.getKit("combotag"));
            afterActivities(event);
        } else if (itemName.contains("Combo")) {
            api.joinQueue(player, BattleKit.getKit("combo"));
            afterActivities(event);
        }
    }

    public void afterActivities(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        // Change the clicked item to "leave queue" item
        ItemStack leaveItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        String originalName = clickedItem.getItemMeta().getDisplayName();

        leaveMeta.setDisplayName(ChatColor.GOLD + "Queued for " + originalName);
        leaveMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "You are currently Queued for the " + originalName + " Queue",
                "",
                ChatColor.RED + "Click Again to Leave the Queue!"
        ));
        leaveItem.setItemMeta(leaveMeta);
        e.getInventory().setItem(e.getSlot(), leaveItem);
    }

    public static void updateInventory(Player p, Inventory i) {
        updateQueueItem(p, i, 10,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_windfight%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_windfight%");
        updateQueueItem(p, i, 11,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_sword%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_fight_count_sword%");
        updateQueueItem(p, i, 12,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_axe%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_axe%");
        updateQueueItem(p, i, 13,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_boxing%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_boxing%");
        updateQueueItem(p, i, 14,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_nodebuff%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_nodebuff%");
        updateQueueItem(p, i, 15,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_builduhc%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_builduhc%");
        updateQueueItem(p, i, 16,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_sumo%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_sumo%");
        updateQueueItem(p, i, 19,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_combo%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_combo%");
        updateQueueItem(p, i, 20,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_gapple%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_gapple%");
        updateQueueItem(p, i, 21,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_bedfight%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_bedfight%");
        updateQueueItem(p, i, 22,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_fireballfight%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_fireballfight%");
        updateQueueItem(p, i, 23,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_skywars%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_skywars%");
        updateQueueItem(p, i, 24,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_archer%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_archer%");
        updateQueueItem(p, i, 25,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_noenchant%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_noenchant%");
        updateQueueItem(p, i, 28,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_spleef%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_spleef%");
        updateQueueItem(p, i, 29,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_sg%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_sg%");
        updateQueueItem(p, i, 30,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_soup%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_soup%");
        updateQueueItem(p, i, 31,
                ChatColor.GRAY + "Queued: "  + ChatColor.AQUA + "%strikepractice_in_queue_count_combotag%",
                ChatColor.GRAY + "Playing: " + ChatColor.AQUA + "%strikepractice_in_queue_count_combotag%");
    }

    public static void updateQueueItem(Player p, Inventory gui, int slot, String queued, String playing) {
        ItemStack item = gui.getItem(slot);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (item.getType() == Material.REDSTONE_BLOCK) {
                    String displayName = meta.getDisplayName();
                    int forIndex = displayName.indexOf(" for ");
                    if (forIndex != -1) {
                        String originalNameWithColor = displayName.substring(forIndex + 5);
                        meta.setLore(Arrays.asList(
                                ChatColor.GRAY + "You are currently Queued for the " + originalNameWithColor + " Queue",
                                "",
                                PlaceholderAPI.setPlaceholders(p, queued),
                                PlaceholderAPI.setPlaceholders(p, playing),
                                "",
                                ChatColor.RED + "Click Again to Leave the Queue!"
                        ));

                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                        meta.addItemFlags(ItemFlag.HIDE_DYE);
                        meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);

                        item.setItemMeta(meta);
                    }
                } else {
                    meta.setLore(Arrays.asList(
                            PlaceholderAPI.setPlaceholders(p, queued),
                            PlaceholderAPI.setPlaceholders(p, playing),
                            "",
                            ChatColor.GREEN + "Click to join!"
                    ));

                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                    meta.addItemFlags(ItemFlag.HIDE_DYE);
                    meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);

                    item.setItemMeta(meta);
                }
            }
        }
    }

    public void newafterActivities(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
        p.openInventory(UnrankedGUI.createQueueGUI(p, 0, "n word"));
    }

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        e.getFight().getPlayersInFight().forEach(p -> {
            p.closeInventory();
        });
    }

    @EventHandler
    public void onDuelStart(DuelStartEvent e) {
        e.getFight().getPlayersInFight().forEach(p -> {
            p.closeInventory();
        });
    }
}
