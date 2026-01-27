package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.fights.Fight;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaSelectorGUI implements Listener {
    private final ArenaManager manager;
    private final ArenaSelectionListener selectionListener;
    private static final Map<UUID, Fight> pendingFights = new HashMap<>();

    public ArenaSelectorGUI(ArenaManager manager, ArenaSelectionListener selectionListener) {
        this.manager = manager;
        this.selectionListener = selectionListener;
    }

    public static void open(Player player, ArenaManager manager, Fight fight) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Select Arena");
        pendingFights.put(player.getUniqueId(), fight);

        manager.getArenas().values().forEach(config -> {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + config.getName());
            item.setItemMeta(meta);
            inv.addItem(item);
        });
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Select Arena")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String mapName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        ArenaConfig selected = manager.getArenas().get(mapName.toLowerCase());
        Fight fight = pendingFights.remove(player.getUniqueId());

        if (selected != null && fight != null) {
            player.closeInventory();
            selectionListener.startMatch(fight, selected); // Manually trigger the match start
        }
    }
}