
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArenaSelectorGUI implements Listener {
    private final ArenaManager manager;
    private final ArenaSelectionListener listener;

    public ArenaSelectorGUI(ArenaManager manager, ArenaSelectionListener listener) {
        this.manager = manager;
        this.listener = listener;
    }

    public static void open(Player player, ArenaManager manager, Fight fight) {
        Map<String, ArenaConfig> arenas = manager.getArenas();

        if (arenas.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No arenas available!");
            return;
        }

        int size = Math.min(54, ((arenas.size() + 8) / 9) * 9);
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_GREEN + "Select Arena");

        int slot = 0;
        for (ArenaConfig config : arenas.values()) {
            if (slot >= size - 9) break; // Leave space for controls

            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(ChatColor.GREEN + config.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Schematic: " + ChatColor.WHITE + config.getSchematicName());
            lore.add(ChatColor.GRAY + "Spawn 1: " + ChatColor.WHITE + formatVector(config.getPos1()));
            lore.add(ChatColor.GRAY + "Spawn 2: " + ChatColor.WHITE + formatVector(config.getPos2()));

            if (!config.getKits().isEmpty()) {
                lore.add(ChatColor.GRAY + "Kits: " + ChatColor.WHITE + String.join(", ", config.getKits()));
            } else {
                lore.add(ChatColor.GRAY + "Kits: " + ChatColor.WHITE + "All allowed");
            }

            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to select this arena!");

            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(slot, item);
            slot++;
        }

        // Add random selection option
        ItemStack randomItem = new ItemStack(Material.EMERALD);
        ItemMeta randomMeta = randomItem.getItemMeta();
        randomMeta.setDisplayName(ChatColor.AQUA + "Random Arena");
        randomMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Let the system choose",
                ChatColor.GRAY + "a random arena for you!",
                "",
                ChatColor.YELLOW + "Click to select random!"
        ));
        randomItem.setItemMeta(randomMeta);
        gui.setItem(size - 5, randomItem);

        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Cancel");
        closeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Close this menu without",
                ChatColor.GRAY + "selecting an arena"
        ));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(size - 1, closeItem);

        player.openInventory(gui);
    }

    private static String formatVector(org.bukkit.util.Vector vector) {
        return String.format("%.1f, %.1f, %.1f", vector.getX(), vector.getY(), vector.getZ());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Select Arena")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Handle close button
        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Arena selection cancelled.");
            return;
        }

        // Handle random selection
        if (clicked.getType() == Material.EMERALD) {
            ArenaConfig randomArena = manager.getRandomArena();
            if (randomArena != null) {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Selected random arena: " + randomArena.getName());
                // Note: Fight retrieval would need to be stored/tracked for admin selections
                // This is a simplified version - in practice you'd need to track pending fights
            }
            return;
        }

        // Handle specific arena selection
        if (clicked.getType() == Material.GRASS_BLOCK) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String arenaName = ChatColor.stripColor(meta.getDisplayName());
                ArenaConfig selected = manager.getArenaByName(arenaName);

                if (selected != null) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "Selected arena: " + selected.getName());
                    // Note: Same as above - would need fight tracking for admin selections
                }
            }
        }
    }
}