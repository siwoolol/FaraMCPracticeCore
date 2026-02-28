package lol.siwoo.faramcpracticecore.design;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ga.strikepractice.events.KitSelectEvent;

public class WarningMessage implements Listener {

    @EventHandler
    public void onKitSelect(KitSelectEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(MessageStyle.warning(
                "Butterfly/Drag Clicking is not recommended and is false-punishable."));
    }
}