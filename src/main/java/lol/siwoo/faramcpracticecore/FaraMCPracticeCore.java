package lol.siwoo.faramcpracticecore;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.admin.*;
import lol.siwoo.faramcpracticecore.aicoach.AICoach;
import lol.siwoo.faramcpracticecore.aicoach.AICoachListener;
import lol.siwoo.faramcpracticecore.design.FightEnd;
import lol.siwoo.faramcpracticecore.design.WarningMessage;
import lol.siwoo.faramcpracticecore.gamemode.BedFight;
import lol.siwoo.faramcpracticecore.gamemode.Boxing;
import lol.siwoo.faramcpracticecore.gamemode.FireballFight;
import lol.siwoo.faramcpracticecore.lobby.Flight;
import lol.siwoo.faramcpracticecore.lobby.FlightListener;
import lol.siwoo.faramcpracticecore.party.HurryUpPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwnerListener;
import lol.siwoo.faramcpracticecore.util.WebhookMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class FaraMCPracticeCore extends JavaPlugin implements Listener {

    private StrikePracticeAPI strikePracticeAPI;
    private AICoach aiCoach;

    @Override
    public void onEnable() {
        // Make sure StrikePractice is loaded first
        if (getServer().getPluginManager().getPlugin("StrikePractice") == null) {
            getLogger().severe("StrikePractice not found! Make sure StrikePractice is installed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            // Try to get the API statically
            strikePracticeAPI = StrikePractice.getAPI();

            if (strikePracticeAPI == null) {
                getLogger().severe("Failed to get StrikePractice API! Make sure StrikePractice is installed and loaded.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } catch (Exception e) {
            getLogger().severe("Error while getting StrikePractice API: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        aiCoach = new AICoach(this, strikePracticeAPI);
        registerEvents();
        registerCommands();

        WebhookMessage.statusMessage("Back On");
    }

    @Override
    public void onDisable() {
        WebhookMessage.statusMessage("Down");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage("SIWOOLOL IS THE BEST");

        new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(ChatColor.GREEN + "give siwoolol some money meh hes greedy");
            }
        }.runTaskLater(this, 80L);
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(this, this);
        pm.registerEvents(new WarningMessage(), this);
        pm.registerEvents(new FightEnd(), this);
        pm.registerEvents(new FlightListener(), this);
        pm.registerEvents(new SuggestPartyOwnerListener(), this);
        pm.registerEvents(new Boxing(this), this);
        pm.registerEvents(new BedFight(this), this);
        pm.registerEvents(new FireballFight(this), this);
//        pm.registerEvents(new AICoachListener(aiCoach, strikePracticeAPI), this);
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new Flight());
//        getCommand("ai").setExecutor(aiCoach);
        getCommand("forcewin").setExecutor(new ForceWin());
        getCommand("hurryuppartyowner").setExecutor(new HurryUpPartyOwner());
        getCommand("suggestgamemodetopartyowner").setExecutor(new SuggestPartyOwner());
        getCommand("gmc").setExecutor(new GMC());
        getCommand("gms").setExecutor(new GMS());
        getCommand("gmsp").setExecutor(new GMSP());
        getCommand("gma").setExecutor(new GMA());
        getCommand("sudo").setExecutor(new Sudo());
    }
}
