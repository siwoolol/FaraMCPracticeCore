package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaSelectorGUI {
    public static final String TITLE = ChatColor.GOLD + "Select Arena";

    public static void open(Player player, ArenaManager manager) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (String mapName : manager.getMaps()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + mapName);
            item.setItemMeta(meta);
            inv.addItem(item);
        }
        player.openInventory(inv);
    }
}