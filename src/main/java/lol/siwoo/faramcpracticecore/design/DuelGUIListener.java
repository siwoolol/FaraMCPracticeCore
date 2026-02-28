package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.arena.Arena;
import ga.strikepractice.battlekit.BattleKit;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import lol.siwoo.faramcpracticecore.arena.ArenaSelectorGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DuelGUIListener implements Listener {

    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api = StrikePractice.getAPI();

    public DuelGUIListener(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.startsWith(DuelGUI.TITLE_PREFIX))
            return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta())
            return;
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        String kitId = BotQueueListener.resolveKitId(itemName);

        if (kitId == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f);
            player.sendActionBar(Component.text("This kit is not available right now.").color(NamedTextColor.RED));
            return;
        }

        BattleKit kit = BattleKit.getKit(kitId);
        if (kit == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f);
            player.sendActionBar(Component.text("This kit is not available right now.").color(NamedTextColor.RED));
            return;
        }

        // Get the target from the pending map
        UUID targetUUID = DuelGUI.pendingDuelTargets.remove(player.getUniqueId());
        if (targetUUID == null) {
            player.sendMessage(MessageStyle.error("Duel target not found. Try again."));
            player.closeInventory();
            return;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            player.sendMessage(MessageStyle.error("That player is no longer online."));
            player.closeInventory();
            return;
        }

        // Get the dynamic arena using centralized manager (auto-creates if all busy)
        Arena dynamicArena = plugin.getArenaManager().getOrAllocateDynamicArena(kit.isBuild());

        if (dynamicArena == null) {
            player.sendMessage(MessageStyle.error("No arena available. Try again."));
            player.closeInventory();
            return;
        }

        try {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);

            // Open map selector for permissioned players
            if (player.hasPermission("faramcpracticecore.selectarena")) {
                ArenaSelectorGUI.open(player, plugin.getArenaManager(), kitId);
            }

            // Send duel request via SP API (silent = true, we send our own message)
            api.sendDuelRequest(player, target, kit, dynamicArena, true);

            // Send our custom styled messages to both players
            String kitDisplayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            DuelRequestMessage.sendDuelRequestMessage(player, target, kitDisplayName);

            player.sendActionBar(
                    Component.text("Duel request sent to " + target.getName() + "!").color(NamedTextColor.GREEN));
        } catch (Exception e) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f);
            player.sendMessage(MessageStyle.error("Failed to send duel request."));
            plugin.getLogger().warning("Failed to send duel request from " + player.getName() + " to "
                    + target.getName() + ": " + e.getMessage());
        }
    }
}
