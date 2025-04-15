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

        if (api.getParty(p).getOwner().equals(p)) {
            p.sendMessage(ChatColor.RED + "Shut the fuck up and start your own event.");
            return true;
        }

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
}
