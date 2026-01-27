package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaSelectorGUI {
    public static void open(Player player, ArenaManager manager) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Select Arena");
        manager.getArenas().values().forEach(config -> {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + config.getName());
            item.setItemMeta(meta);
            inv.addItem(item);
        });
        player.openInventory(inv);
    }
}