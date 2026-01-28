package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class ArenaSelectorGUI implements Listener {
    private final ArenaManager manager;
    public static final Map<UUID, ArenaConfig> queuedSelections = new HashMap<>();

    public ArenaSelectorGUI(ArenaManager manager) {
        this.manager = manager;
    }

    public static void open(Player player, ArenaManager manager, String kitName) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Select Arena for " + kitName);
        manager.getArenas().values().stream()
                .filter(c -> c.isKitAllowed(kitName))
                .forEach(config -> {
                    ItemStack item = new ItemStack(Material.GRASS_BLOCK);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + config.getName());
                    item.setItemMeta(meta);
                    gui.addItem(item);
                });
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Select Arena for")) return;
        event.setCancelled(true);
        Player p = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        ArenaConfig cfg = manager.getArenas().get(name.toLowerCase());
        if (cfg != null) {
            queuedSelections.put(p.getUniqueId(), cfg);
            p.sendMessage(ChatColor.GREEN + "Arena selected for your next match!");
            p.closeInventory();
        }
    }
}