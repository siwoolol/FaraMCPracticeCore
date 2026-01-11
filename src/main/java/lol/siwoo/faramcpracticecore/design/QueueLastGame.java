package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.battlekit.BattleKit;
import ga.strikepractice.events.KitSelectEvent;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class QueueLastGame implements CommandExecutor, Listener {
    private final StrikePracticeAPI api = StrikePractice.getAPI();
    public final Map<UUID, String> lastKitData = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String [] args) {
        Player p = (Player) sender;
        UUID u = p.getUniqueId();

        if (!lastKitData.containsKey(u)) {
            Bukkit.dispatchCommand(sender, "unranked");
            return true;
        } else {
            if (BattleKit.getKit(lastKitData.get(u)) == null) {
                Bukkit.dispatchCommand(sender, "unranked");
                return true;
            }

            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f);
            api.joinQueue(p, BattleKit.getKit(lastKitData.get(u)));
            return true;
        }
    }

    @EventHandler
    public void onGameQueue(KitSelectEvent e) {
        Bukkit.getLogger().info("e");
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();

        String kitName = e.getKit().getName();
        lastKitData.remove(u);
        lastKitData.put(u, kitName);
    }
}
