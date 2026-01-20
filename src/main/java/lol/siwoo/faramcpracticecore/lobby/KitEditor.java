package lol.siwoo.faramcpracticecore.lobby;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.DuelStartEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        if (api.isEditingKit(p)) {
            if (e.getMessage().toLowerCase().startsWith("/kiteditor")) {
                p.sendMessage(ChatColor.GRAY + "You have left the Kit Editor");
                return;
            }

            if (e.getMessage().toLowerCase().startsWith("/leave") || e.getMessage().toLowerCase().startsWith("/l")) {
                Bukkit.dispatchCommand(p, "kiteditor leave");

                p.sendMessage(ChatColor.GRAY + "You have left the Kit Editor");
                return;
            }

            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GRAY + "You need to leave the Kit Editor first! " + ChatColor.RED + "(/leave)");
        }
    }

    @EventHandler
    public void onPlayerQueue(DuelStartEvent e) {
        Player p1 = e.getPlayer1();
        Player p2 = e.getPlayer2();

        if (api.isEditingKit(p1)) {
            Bukkit.dispatchCommand(p1, "kiteditor leave");
            p1.sendMessage(ChatColor.GRAY + "You have been removed from the Kit Editor since you joined a duel.");
        }

        if (api.isEditingKit(p2)) {
            Bukkit.dispatchCommand(p2, "kiteditor leave");
            p2.sendMessage(ChatColor.GRAY + "You have been removed from the Kit Editor since you joined a duel.");
        }

    }
}
