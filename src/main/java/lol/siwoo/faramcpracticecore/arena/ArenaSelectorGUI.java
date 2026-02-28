package lol.siwoo.faramcpracticecore.arena;

import lol.siwoo.faramcpracticecore.design.MessageStyle;
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

import java.util.*;

public class ArenaSelectorGUI implements Listener {
    private final ArenaManager manager;
    public static final Map<UUID, ArenaConfig> queuedSelections = new HashMap<>();
    private static final String TITLE_PREFIX = ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Map Select"
            + ChatColor.DARK_GRAY + " ● ";

    public ArenaSelectorGUI(ArenaManager manager) {
        this.manager = manager;
    }

    public static void open(Player p, ArenaManager m, String kit) {
        // Collect matching arenas
        List<ArenaConfig> arenas = new ArrayList<>();
        m.getArenas().values().stream()
                .filter(c -> c.isKitAllowed(kit))
                .forEach(arenas::add);

        // Size: 27 (3 rows) or 45 (5 rows) depending on count
        int size = arenas.size() > 7 ? 45 : 27;
        String title = TITLE_PREFIX + ChatColor.GRAY + kit;
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Fill background
        fillBackground(gui);

        // Place arena items in the center row(s)
        int[] slots;
        if (size == 27) {
            // Single row: slots 10-16 (middle row of 27)
            slots = new int[] { 10, 11, 12, 13, 14, 15, 16 };
        } else {
            // Two rows: slots 10-16, 19-25 (middle rows of 45)
            slots = new int[] { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
        }

        for (int i = 0; i < arenas.size() && i < slots.length; i++) {
            ArenaConfig cfg = arenas.get(i);
            gui.setItem(slots[i], createArenaItem(cfg));
        }

        p.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String viewTitle = e.getView().getTitle();
        if (!viewTitle.contains("Map Select"))
            return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR)
            return;
        if (item.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        ArenaConfig cfg = manager.getArenas().get(name.toLowerCase());
        if (cfg != null) {
            queuedSelections.put(p.getUniqueId(), cfg);
            p.sendMessage(MessageStyle.successWithHighlight("Selected map", name, ""));
            p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
            p.closeInventory();
        }
    }

    private static ItemStack createArenaItem(ArenaConfig cfg) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + cfg.getName());

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ARMOR_TRIM,
                ItemFlag.HIDE_DYE, ItemFlag.HIDE_STORED_ENCHANTS);

        meta.setLore(Arrays.asList(
                ChatColor.DARK_GRAY + cfg.getSchematicName(),
                "",
                ChatColor.GREEN + "Click to select this map!"));

        item.setItemMeta(meta);
        return item;
    }

    private static void fillBackground(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null)
                gui.setItem(i, glass);
        }
    }
}