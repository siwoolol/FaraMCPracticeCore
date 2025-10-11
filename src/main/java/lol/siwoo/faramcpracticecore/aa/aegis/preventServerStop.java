package lol.siwoo.faramcpracticecore.aa.aegis;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class preventServerStop implements Listener {

    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (e.getMessage().equalsIgnoreCase("/stop")
                || e.getMessage().equalsIgnoreCase("/bukkit:stop")
                || e.getMessage().equalsIgnoreCase("/minecraft:stop")
                || e.getMessage().equalsIgnoreCase("/reload")
                || e.getMessage().equalsIgnoreCase("/bukkit:reload")
                || e.getMessage().equalsIgnoreCase("/minecraft:reload")
                || e.getMessage().equalsIgnoreCase("/restart")
                || e.getMessage().equalsIgnoreCase("/bukkit:restart")
                || e.getMessage().equalsIgnoreCase("/minecraft:restart")
                || e.getMessage().equalsIgnoreCase("/bukkit:plugin disable faramcpracticecore")
                || e.getMessage().equalsIgnoreCase("/bukkit:plugin disable strikepractice")
                || e.getMessage().equalsIgnoreCase("/bukkit:pl disable faramcpracticecore")
                || e.getMessage().equalsIgnoreCase("/bukkit:pl disable strikepractice")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.GRAY + "Unknown command. Type" + ChatColor.RED + " /help " + ChatColor.GRAY + "for help.");
        }
    }
}
