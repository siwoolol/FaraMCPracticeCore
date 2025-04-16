package lol.siwoo.acemcpracticecore;

import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.acemcpracticecore.admin.ForceWin;
import lol.siwoo.acemcpracticecore.design.FightEnd;
import lol.siwoo.acemcpracticecore.design.WarningMessage;
import lol.siwoo.acemcpracticecore.lobby.Flight;
import lol.siwoo.acemcpracticecore.lobby.FlightListener;
import lol.siwoo.acemcpracticecore.party.HurryUpPartyOwner;
import lol.siwoo.acemcpracticecore.party.SuggestPartyOwner;
import lol.siwoo.acemcpracticecore.party.SuggestPartyOwnerListener;
import lol.siwoo.acemcpracticecore.status.WebhookMessage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AceMCPracticeCore extends JavaPlugin implements Listener {

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
        pm.registerEvents(new WarningMessage(), this);
        pm.registerEvents(new FightEnd(), this);
        pm.registerEvents(new FlightListener(), this);
        pm.registerEvents(new SuggestPartyOwnerListener(), this);
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new Flight());
        getCommand("forcewin").setExecutor(new ForceWin());
        getCommand("hurryuppartyowner").setExecutor(new HurryUpPartyOwner());
        getCommand("suggestgamemodetopartyowner").setExecutor(new SuggestPartyOwner());
    }
}
