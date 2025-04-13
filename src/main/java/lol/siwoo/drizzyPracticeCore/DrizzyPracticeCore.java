package lol.siwoo.drizzyPracticeCore;

import lol.siwoo.drizzyPracticeCore.status.WebhookMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class DrizzyPracticeCore extends JavaPlugin {

    @Override
    public void onEnable() {
        WebhookMessage.webhookMessage("Back On");
    }

    @Override
    public void onDisable() {
        WebhookMessage.webhookMessage("Down");
    }
}
