package lol.siwoo.faramcpracticecore;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.aa.aegis.CommandBlocker;
import lol.siwoo.faramcpracticecore.aa.aegis.preventServerStop;
import lol.siwoo.faramcpracticecore.aa.status.StatusChecker;
import lol.siwoo.faramcpracticecore.admin.*;
import lol.siwoo.faramcpracticecore.arena.ArenaManager;
import lol.siwoo.faramcpracticecore.arena.ArenaSelectionListener;
import lol.siwoo.faramcpracticecore.arena.ArenaSelectorGUI;
import lol.siwoo.faramcpracticecore.bot.BotFightEnd;
import lol.siwoo.faramcpracticecore.design.*;
import lol.siwoo.faramcpracticecore.fix.PotThrowMech;
import lol.siwoo.faramcpracticecore.gamemode.BedFight;
import lol.siwoo.faramcpracticecore.gamemode.Boxing;
import lol.siwoo.faramcpracticecore.gamemode.RBWFFA;
import lol.siwoo.faramcpracticecore.gamemode.WindFight;
import lol.siwoo.faramcpracticecore.lobby.Flight;
import lol.siwoo.faramcpracticecore.lobby.FlightListener;
import lol.siwoo.faramcpracticecore.party.HurryUpPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwner;
import lol.siwoo.faramcpracticecore.party.SuggestPartyOwnerListener;
import lol.siwoo.faramcpracticecore.lobby.KitEditor;
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

public final class FaraMCPracticeCore extends JavaPlugin implements Listener {
    private StrikePracticeAPI strikePracticeAPI;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        getLogger().info("Starting FaraMCPracticeCore...");

        StatusChecker statusChecker = new StatusChecker(this);
        statusChecker.check();

        if (!apiCheck()) return;

        // Initialize Arena Manager
        try {
            this.arenaManager = new ArenaManager(this);
            getLogger().info("Arena system initialized successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize Arena Manager: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerEvents();
        registerCommands();

        WebhookMessage.statusMessage("Back Up");
        getLogger().info("FaraMCPracticeCore enabled successfully!");
    }

    public boolean apiCheck() {
        PluginManager pm = getServer().getPluginManager();
        if (pm.getPlugin("StrikePractice") == null || pm.getPlugin("WorldEdit") == null) {
            getLogger().severe("Missing StrikePractice or WorldEdit! Disabling...");
            pm.disablePlugin(this);
            return false;
        }
        strikePracticeAPI = StrikePractice.getAPI();
        return strikePracticeAPI != null;
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) arenaManager.shutdown();
        WebhookMessage.statusMessage("Down");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) { e.joinMessage(null); }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) { e.setQuitMessage(null); }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) { e.message(null); }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        // Dynamic Arena System
        pm.registerEvents(new ArenaSelectionListener(this, arenaManager), this);
        pm.registerEvents(new ArenaSelectorGUI(arenaManager), this);

        // Core Components
        pm.registerEvents(new preventServerStop(), this);
        pm.registerEvents(new CommandBlocker(), this);
        pm.registerEvents(new PotThrowMech(), this);
        pm.registerEvents(this, this);
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
        pm.registerEvents(new WindFight(this), this);
        pm.registerEvents(new RBWFFA(this), this);
        pm.registerEvents(new QueueLastGame(), this);
    }

    private void registerCommands() {
        getCommand("unranked").setExecutor(new UnrankedGUI(this));
        getCommand("ranked").setExecutor(new RankedQueue());
        getCommand("fly").setExecutor(new Flight());
        getCommand("forcewin").setExecutor(new ForceWin());
        getCommand("gmc").setExecutor(new GMC());
        getCommand("gms").setExecutor(new GMS());
        getCommand("gmsp").setExecutor(new GMSP());
        getCommand("gma").setExecutor(new GMA());
        getCommand("sudo").setExecutor(new Sudo());
    }

    public ArenaManager getArenaManager() { return arenaManager; }
}