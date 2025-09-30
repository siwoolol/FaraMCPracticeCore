package lol.siwoo.faramcpracticecore.safety;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class KitEditor implements Listener {
    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api;

    public KitEditor(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
    }

    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (api.isEditingKit(p) && !e.getMessage().toLowerCase().startsWith("/kiteditor")) {
            if (e.getMessage().toLowerCase().startsWith("/leave")) {
                Bukkit.dispatchCommand(p, "kiteditor leave");

                p.sendMessage(ChatColor.GREEN + "You have left the Kit Editor.");
                return;
            }

            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GRAY + "You need to leave the Kit Editor first!" + ChatColor.RED + "(/leave)");
        }
    }
}
