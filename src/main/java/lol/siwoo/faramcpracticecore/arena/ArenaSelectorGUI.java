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

    public ArenaSelectorGUI(ArenaManager manager) { this.manager = manager; }

    public static void open(Player p, ArenaManager m, String kit) {
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.DARK_GREEN + "Select Arena for " + kit);
        m.getArenas().values().stream().filter(c -> c.isKitAllowed(kit)).forEach(c -> {
            ItemStack i = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = i.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + c.getName());
            i.setItemMeta(meta);
            gui.addItem(i);
        });
        p.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Select Arena for")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
        ArenaConfig cfg = manager.getArenas().get(name.toLowerCase());
        if (cfg != null) {
            queuedSelections.put(p.getUniqueId(), cfg);
            p.sendMessage(ChatColor.GREEN + "Arena choice saved!");
            p.closeInventory();
        }
    }
}