package lol.siwoo.faramcpracticecore.aa.aegis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandBlocker implements Listener {
    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (!p.hasPermission("faramcpracticecore.admin")) {
            if (e.getMessage().equalsIgnoreCase("/plugin") ||
                    e.getMessage().equalsIgnoreCase("/pl") ||
                    e.getMessage().equalsIgnoreCase("/bukkit:pl") ||
                    e.getMessage().equalsIgnoreCase("/bukkit:plugin") ||
                    e.getMessage().equalsIgnoreCase("/?") ||
                    e.getMessage().equalsIgnoreCase("/help") ||
                    e.getMessage().equalsIgnoreCase("/bukkit:help") ||
                    e.getMessage().equalsIgnoreCase("/version") ||
                    e.getMessage().equalsIgnoreCase("/ver") ||
                    e.getMessage().equalsIgnoreCase("/bukkit:version") ||
                    e.getMessage().equalsIgnoreCase("/bukkit:ver") ||
                    e.getMessage().toLowerCase().startsWith("/me") ||
                    e.getMessage().toLowerCase().startsWith("/bukkit:me")) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "Unknown command. Type" + ChatColor.RED + " /help " + ChatColor.GRAY + "for help.");
            }
        }
    }
}
