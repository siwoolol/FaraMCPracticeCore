package lol.siwoo.drizzyPracticeCore;

import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.drizzyPracticeCore.admin.ForceWin;
import lol.siwoo.drizzyPracticeCore.lobby.Flight;
import lol.siwoo.drizzyPracticeCore.party.HurryUpPartyOwner;
import lol.siwoo.drizzyPracticeCore.party.SuggestPartyOwner;
import lol.siwoo.drizzyPracticeCore.party.SuggestPartyOwnerListener;
import lol.siwoo.drizzyPracticeCore.status.WebhookMessage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DrizzyPracticeCore extends JavaPlugin implements Listener {

    private StrikePracticeAPI strikePracticeAPI;

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
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(this, this);
        pm.registerEvents(new SuggestPartyOwnerListener(), this);
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new Flight());
        getCommand("forcewin").setExecutor(new ForceWin());
        getCommand("hurryuppartyowner").setExecutor(new HurryUpPartyOwner());
        getCommand("suggestgamemodetopartyowner").setExecutor(new SuggestPartyOwner());
    }
}
