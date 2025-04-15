package lol.siwoo.drizzyPracticeCore.party;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.battlekit.BattleKit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.Bukkit.getPlayer;

public class SuggestPartyOwner implements CommandExecutor {

    public static final String GUI_TITLE = ChatColor.DARK_AQUA + "Select Kit to Suggest";
    StrikePracticeAPI api = StrikePractice.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Player p = (Player) sender;

        Inventory kitSelectionGui = createKitSelectionGUI();
        if (kitSelectionGui == null) {
            p.sendMessage(ChatColor.RED + "Could not load kits. Please contact an admin.");
            return true;
        }
        p.openInventory(kitSelectionGui);

        return true;
    }

    private Inventory createKitSelectionGUI() {
        int kitCount = api.getKits().size();
        int inventorySize = Math.min(54, Math.max(9, (int) Math.ceil(kitCount / 9.0) * 9));

        Inventory gui = Bukkit.createInventory(null, inventorySize, GUI_TITLE);

        int slot = 0;
        for (BattleKit kit : api.getKits()) {
            if (slot >= inventorySize) break;

            ItemStack kitIcon = kit.getIcon().clone();

            ItemMeta meta = kitIcon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + kit.getName());

                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Click to suggest this kit to the party owner.",
                        "",
                        ChatColor.DARK_GRAY + "Kit ID: " + kit.getName()
                ));
                kitIcon.setItemMeta(meta);
            }

            gui.setItem(slot++, kitIcon);
        }

        return gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        InventoryView view = event.getView();

        if (!view.getTitle().equals(GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return;
        }
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }

        List<String> lore = meta.getLore();
        String suggestedKitName = null;
        for (String line : lore) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith("Kit ID: ")) {
                suggestedKitName = strippedLine.substring("Kit ID: ".length());
                break;
            }
        }

        if (suggestedKitName == null) {
            p.sendMessage(ChatColor.RED + "Error determining which kit you selected.");
            p.closeInventory();
            return;
        }

        if (api.getParty(p) == null) {
            p.sendMessage(ChatColor.RED + "You not in a party!");
            p.closeInventory();
            return;
        }

        String ownerName = api.getParty(p).getOwner();
        Player owner = Bukkit.getPlayerExact(ownerName);

        if (owner == null || !owner.isOnline()) {
            p.sendMessage(ChatColor.RED + "The party owner (" + ChatColor.YELLOW + ownerName + ChatColor.RED + ") is currently offline.");
            p.closeInventory();
            return;
        }

        p.sendMessage(ChatColor.GREEN + "You suggested the kit '" + ChatColor.WHITE + suggestedKitName + ChatColor.GREEN + "' to the party owner!");
        owner.sendMessage(ChatColor.AQUA + p.getName() + ChatColor.GRAY + " suggested playing the kit: " + ChatColor.WHITE + suggestedKitName);

        p.closeInventory();
    }
}
