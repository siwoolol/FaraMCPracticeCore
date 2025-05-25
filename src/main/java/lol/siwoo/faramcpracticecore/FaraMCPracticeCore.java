package lol.siwoo.faramcpracticecore;

import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.admin.*;
import lol.siwoo.faramcpracticecore.design.FightEnd;
import lol.siwoo.faramcpracticecore.design.WarningMessage;
import lol.siwoo.faramcpracticecore.gamemode.Boxing;
import lol.siwoo.faramcpracticecore.lobby.Flight;
import lol.siwoo.faramcpracticecore.lobby.FlightListener;
import lol.siwoo.faramcpracticecore.party.HurryUpPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwnerListener;
import lol.siwoo.faramcpracticecore.util.WebhookMessage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FaraMCPracticeCore extends JavaPlugin implements Listener {

    private StrikePracticeAPI strikePracticeAPI;

    @Override
    public void onEnable() {
        registerEvents();
        registerCommands();

        WebhookMessage.statusMessage("Back On");
    }

    @Override
    public void onDisable() {
        WebhookMessage.statusMessage("Down");
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(this, this);
        pm.registerEvents(new WarningMessage(), this);
        pm.registerEvents(new FightEnd(), this);
        pm.registerEvents(new FlightListener(), this);
        pm.registerEvents(new SuggestPartyOwnerListener(), this);
        pm.registerEvents(new Boxing(this), this);
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new Flight());
        getCommand("forcewin").setExecutor(new ForceWin());
        getCommand("hurryuppartyowner").setExecutor(new HurryUpPartyOwner());
        getCommand("suggestgamemodetopartyowner").setExecutor(new SuggestPartyOwner());
        getCommand("gmc").setExecutor(new GMC());
        getCommand("gms").setExecutor(new GMS());
        getCommand("gmsp").setExecutor(new GMSP());
        getCommand("gma").setExecutor(new GMA());
    }
}
