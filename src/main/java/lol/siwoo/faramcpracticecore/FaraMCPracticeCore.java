package lol.siwoo.faramcpracticecore;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.aa.aegis.CommandBlocker;
import lol.siwoo.faramcpracticecore.aa.aegis.preventServerStop;
import lol.siwoo.faramcpracticecore.aa.silas_pvp.DataLogger;
import lol.siwoo.faramcpracticecore.aa.status.StatusChecker;
import lol.siwoo.faramcpracticecore.aa.terms.Agree;
import lol.siwoo.faramcpracticecore.aa.terms.Disagree;
import lol.siwoo.faramcpracticecore.aa.terms.JoinMessage;
import lol.siwoo.faramcpracticecore.admin.*;
import lol.siwoo.faramcpracticecore.aa.aicoach.AICoach;
import lol.siwoo.faramcpracticecore.arena.ArenaManager;
import lol.siwoo.faramcpracticecore.arena.ArenaSelectionListener;
import lol.siwoo.faramcpracticecore.arena.ArenaSelectorGUI;
import lol.siwoo.faramcpracticecore.bot.BotFightEnd;
import lol.siwoo.faramcpracticecore.design.*;
import lol.siwoo.faramcpracticecore.fix.PotThrowMech;
import lol.siwoo.faramcpracticecore.gamemode.BedFight;
import lol.siwoo.faramcpracticecore.gamemode.Boxing;
//import lol.siwoo.faramcpracticecore.gamemode.FireballFight;
import lol.siwoo.faramcpracticecore.gamemode.RBWFFA;
import lol.siwoo.faramcpracticecore.gamemode.WindFight;
import lol.siwoo.faramcpracticecore.lobby.Flight;
import lol.siwoo.faramcpracticecore.lobby.FlightListener;
import lol.siwoo.faramcpracticecore.party.HurryUpPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwnerListener;
import lol.siwoo.faramcpracticecore.lobby.KitEditor;
import lol.siwoo.faramcpracticecore.train.TrainingManager;
import lol.siwoo.faramcpracticecore.util.WebhookMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FaraMCPracticeCore extends JavaPlugin implements Listener {
    private StrikePracticeAPI strikePracticeAPI;
    private ArenaManager arenaManager;
    private TrainingManager trainingManager;
    private AICoach aiCoach;

    @Override
    public void onEnable() {
        StatusChecker statusChecker = new StatusChecker(this);
        statusChecker.check();
        apiCheck();
        registerEvents();

//        JoinMessage.initialize(this);

        WebhookMessage.statusMessage("Back Up");
    }

    public void apiCheck() {
        // StrikePractice check
        if (getServer().getPluginManager().getPlugin("StrikePractice") == null) {
            getLogger().severe("StrikePractice not found! Make sure StrikePractice is installed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // PlaceHolderAPI check
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("PlaceholderAPI not found! Make sure PlaceholderAPI is installed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ProtocolLib check
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib is required for training features!\n");
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
    }
    @Override
    public void onDisable() {
        WebhookMessage.statusMessage("Down");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.joinMessage(null);

        Player p = e.getPlayer();
//        JoinMessage.sendJoinMessage(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        e.message(null);
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        // Aegis
        pm.registerEvents(new preventServerStop(), this);
        pm.registerEvents(new CommandBlocker(), this);

        pm.registerEvents(new ArenaSelectionListener(this, arenaManager), this);
        pm.registerEvents(new ArenaSelectorGUI(arenaManager), this);

//        aiCoach = new AICoach(this, strikePracticeAPI);
//        trainingManager = new TrainingManager(this);

        // Fixes
        pm.registerEvents(new PotThrowMech(), this);

        pm.registerEvents(this, this);
//        pm.registerEvents(new TrainingGUIListener(this, trainingManager), this);
        pm.registerEvents(new QueueGUIListener(this), this);
        pm.registerEvents(new KitEditor(this), this);
        pm.registerEvents(new WarningMessage(), this);
        pm.registerEvents(new UnrankedGUI(this), this);
        pm.registerEvents(new FightEnd(), this);
        pm.registerEvents(new BotFightEnd(), this);
        pm.registerEvents(new FlightListener(), this);
        pm.registerEvents(new SuggestPartyOwnerListener(), this);
        pm.registerEvents(new Boxing(this), this);
        pm.registerEvents(new BedFight(this), this);
//        pm.registerEvents(new FireballFight(this), this);
        pm.registerEvents(new WindFight(this), this);
        pm.registerEvents(new RBWFFA(this), this);
//        pm.registerEvents(new AICoachListener(aiCoach, strikePracticeAPI), this);

//        DataLogger dataLogger = new DataLogger(this);
//        pm.registerEvents(dataLogger, this);
//        pm.registerEvents(new JoinMessage(), this);
        QueueLastGame queueLastGame = new QueueLastGame();
        pm.registerEvents(queueLastGame, this);

        getCommand("unrankedgui").setExecutor(new UnrankedGUI(this));
        getCommand("unranked").setExecutor(new UnrankedGUI(this));
        getCommand("queue").setExecutor(new UnrankedGUI(this));
        getCommand("ranked").setExecutor(new RankedQueue());
        getCommand("botduel").setExecutor(new PvpBotQueue());
        getCommand("queuelastgame").setExecutor(queueLastGame);
//        getCommand("train").setExecutor(new TrainingCommand(this, trainingManager));

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
//        getCommand("terms_agree").setExecutor(new Agree(this));
//        getCommand("terms_disagree").setExecutor(new Disagree());
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public void emergencyShutDown() {
        getLogger().severe("800: Something went wrong. Please try again later.");

        try {
            File pluginFile = getFile();

            getServer().getPluginManager().disablePlugin(this);

            if (pluginFile.delete()) {
                getLogger().severe("400: Something went wrong. Please try again later.");
            } else {
                getLogger().severe("400: Something went wrong. Please try again later.");
                pluginFile.deleteOnExit(); // fallback to delete on exit
                Bukkit.getServer().shutdown();
            }
        } catch (Exception e) {
            getLogger().severe("400: Something went wrong. Please try again later.");
            Bukkit.getServer().shutdown();
        }
    }

// Basic permission check example
//    if (!p.hasPermission("faramcpracticecore.admin")) {
//        p.sendMessage(ChatColor.GRAY + "Unknown command. Type" + ChatColor.RED + " /help " + ChatColor.GRAY + "for help.");
//        return true;
//    }
}