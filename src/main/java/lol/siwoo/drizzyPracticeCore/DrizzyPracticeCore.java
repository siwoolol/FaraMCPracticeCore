package lol.siwoo.drizzyPracticeCore;

import lol.siwoo.drizzyPracticeCore.lobby.Flight;
import lol.siwoo.drizzyPracticeCore.status.WebhookMessage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DrizzyPracticeCore extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        registerEvents();
        registerCommands();

        WebhookMessage.webhookMessage("Back On");
    }

    @Override
    public void onDisable() {
        WebhookMessage.webhookMessage("Down");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new Flight());
    }
}
