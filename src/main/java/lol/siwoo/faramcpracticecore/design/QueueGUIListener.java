package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.battlekit.BattleKit;
import ga.strikepractice.events.DuelStartEvent;
import ga.strikepractice.events.FightStartEvent;
import ga.strikepractice.events.KitSelectEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.stream.events.StartDocument;

public class QueueGUIListener implements Listener {

    private final FaraMCPracticeCore plugin;
    StrikePracticeAPI api = StrikePractice.getAPI();

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
            Bukkit.dispatchCommand(player, "queue leave");
            newafterActivities(event);
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
        } else if (itemName.contains("Combo")) {
            api.joinQueue(player, BattleKit.getKit("combo"));
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
        }
    }

    public void afterActivities(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.WOOD_CLICK, 1, 1);
        p.openInventory(UnrankedGUI.createQueueGUI(p, 0, "n word"));
        p.openInventory(UnrankedGUI.createQueueGUI(p, e.getSlot(), e.getCurrentItem().getItemMeta().getDisplayName()));
    }

    public void updateInventory(Inventory i) {
        i.getSize();
    }

    public void newafterActivities(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.WOOD_CLICK, 1, 1);
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