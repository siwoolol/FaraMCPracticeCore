package lol.siwoo.faramcpracticecore.party;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static lol.siwoo.faramcpracticecore.party.SuggestPartyOwner.GUI_TITLE;

public class SuggestPartyOwnerListener implements Listener {

    StrikePracticeAPI api = StrikePractice.getAPI();

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
