package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.arena.Arena;
import ga.strikepractice.events.*;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class ArenaSelectionListener implements Listener {
    private final FaraMCPracticeCore plugin;
    private final ArenaManager manager;

    // Players waiting for arena paste to complete (stay in lobby)
    private final Set<UUID> pendingPaste = new HashSet<>();
    // Players in post-fight delay (stay in arena for 3s)
    private final Set<UUID> delayedPlayers = new HashSet<>();
    // Fights that already had their start handled (prevent double-fire from
    // DuelStartEvent + FightStartEvent)
    private final Set<Fight> handledStarts = Collections.newSetFromMap(new WeakHashMap<>());
    // Fights that already had their end handled (prevent double-fire)
    private final Set<Fight> handledEnds = Collections.newSetFromMap(new WeakHashMap<>());

    public ArenaSelectionListener(FaraMCPracticeCore plugin, ArenaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onKitSelect(KitSelectEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faramcpracticecore.selectarena") && StrikePractice.getAPI().isInQueue(player)) {
            ArenaSelectorGUI.open(player, manager, event.getKit().getName());
        }
    }

    /**
     * Block teleports for players waiting for paste OR in post-fight delay.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (pendingPaste.contains(uuid) || delayedPlayers.contains(uuid)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles ALL fight types: unranked, duels, and bot fights.
     * DuelStartEvent and BotDuelStartEvent extend FightStartEvent,
     * so this handler receives them all. We use handledStarts to prevent
     * processing the same fight twice.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFightStart(FightStartEvent event) {
        Fight fight = event.getFight();

        // Prevent double-fire: SP fires both FightStartEvent AND
        // DuelStartEvent/BotDuelStartEvent
        if (handledStarts.contains(fight))
            return;
        handledStarts.add(fight);

        Arena spArena = fight.getArena();
        if (spArena == null)
            return;

        String arenaName = spArena.getName().toLowerCase();

        // Only handle fights on dynamic arenas
        if (!arenaName.contains("dynamic"))
            return;

        plugin.getLogger().info("Handling fight start on arena: " + arenaName
                + " | Fight type: " + fight.getClass().getSimpleName()
                + " | Players: " + fight.getPlayersInFight().size());

        // Free up this SP arena immediately so SP can reuse it for the next queue
        Bukkit.getScheduler().runTaskLater(plugin, () -> spArena.setUsing(false), 2L);

        // Determine prefix for dynamic arena management
        boolean isBuild = fight.getKit() != null && fight.getKit().isBuild();
        String prefix = isBuild ? "dynamicbuild" : "dynamic";

        // Ensure another free dynamic arena exists for the next fight
        ensureDynamicArenaAvailable(prefix);

        // Pick the right ArenaConfig (user-selected or random for kit)
        List<Player> players = fight.getPlayersInFight();
        ArenaConfig selected = null;

        if (!players.isEmpty()) {
            selected = ArenaSelectorGUI.queuedSelections.remove(players.get(0).getUniqueId());
        }
        if (selected == null && fight.getKit() != null) {
            selected = manager.getRandomArenaForKit(fight.getKit().getName());
        }

        if (selected != null) {
            startMatch(fight, selected, spArena);
        } else {
            plugin.getLogger().warning("No arena config found for fight, skipping paste.");
        }
    }

    private void startMatch(Fight fight, ArenaConfig config, Arena spArena) {
        // Block SP's teleport — players stay in lobby during paste
        List<Player> players = fight.getPlayersInFight();
        for (Player p : players) {
            if (p != null)
                pendingPaste.add(p.getUniqueId());
        }

        manager.createSession(fight, config).thenAccept(session -> {
            if (session == null)
                return;

            // Store the SP arena reference for cleanup later
            session.setSpArena(spArena);

            // Teleport on the main thread after paste completes
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location origin = session.getCenter().clone().add(config.getCenter());

                Location s1 = origin.clone().add(config.getPos1());
                Location s2 = origin.clone().add(config.getPos2());

                Vector dir1 = s2.toVector().subtract(s1.toVector()).setY(0);
                s1.setDirection(dir1);

                Vector dir2 = s1.toVector().subtract(s2.toVector()).setY(0);
                s2.setDirection(dir2);

                // Update StrikePractice internal locations
                fight.getArena().setLoc1(s1);
                fight.getArena().setLoc2(s2);

                // Unblock teleports, then teleport
                for (Player p : players) {
                    if (p != null)
                        pendingPaste.remove(p.getUniqueId());
                }
                if (players.size() >= 1)
                    players.get(0).teleport(s1);
                if (players.size() >= 2)
                    players.get(1).teleport(s2);

                plugin.getLogger().info("Teleported " + players.size() + " players to arena '"
                        + config.getName() + "' in " + s1.getWorld().getName());
            });
        });
    }

    // ─── Dynamic Arena Management ────────────────────────────────────────

    /**
     * Ensures at least one free dynamic SP arena exists for the next queued fight.
     */
    private void ensureDynamicArenaAvailable(String prefix) {
        List<Arena> matching = new ArrayList<>();
        boolean hasFree = false;

        for (Arena a : StrikePractice.getAPI().getArenas()) {
            if (a.getName().toLowerCase().startsWith(prefix)) {
                matching.add(a);
                if (!a.isUsing())
                    hasFree = true;
            }
        }

        if (!hasFree && !matching.isEmpty()) {
            Arena template = matching.get(0);
            String newName = prefix + "_" + (matching.size() + 1);

            if (StrikePractice.getAPI().getArena(newName) != null)
                return;

            createSpArena(newName, template);
        }
    }

    /**
     * Creates a new StrikePractice arena by cloning a template.
     */
    private void createSpArena(String name, Arena template) {
        try {
            Location defaultLoc = new Location(Bukkit.getWorld("world"), 0, 100, 0);
            Map<String, Object> data = template.serialize();
            data.put("name", name);
            data.put("loc1", defaultLoc.clone());
            data.put("loc2", defaultLoc.clone());
            data.put("center", defaultLoc.clone());

            Arena newArena = (Arena) org.bukkit.configuration.serialization.ConfigurationSerialization
                    .deserializeObject(data, template.getClass());

            if (newArena != null) {
                newArena.setLoc1(defaultLoc.clone());
                newArena.setLoc2(defaultLoc.clone());
                newArena.setCenter(defaultLoc.clone());
                newArena.setBuild(template.isBuild());
                newArena.setUsing(false);
                newArena.setKits(template.getKits());
                newArena.saveForStrikePractice();
                plugin.getLogger()
                        .info("Created dynamic SP arena: " + name + " (cloned from " + template.getName() + ")");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create dynamic arena '" + name + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cleans up extra dynamic SP arenas — keeps at least 1 of each prefix.
     */
    private void cleanupDynamicArena(Fight fight) {
        FightSession session = manager.getSession(fight);
        if (session == null)
            return;

        Arena spArena = session.getSpArena();
        if (spArena == null)
            return;

        String arenaName = spArena.getName().toLowerCase();
        if (!arenaName.contains("dynamic"))
            return;

        boolean isBuild = arenaName.startsWith("dynamicbuild");
        String prefix = isBuild ? "dynamicbuild" : "dynamic";

        long count = StrikePractice.getAPI().getArenas().stream()
                .filter(a -> a.getName().toLowerCase().startsWith(prefix))
                .count();

        if (count > 1 && arenaName.contains("_")) {
            spArena.removeFromStrikePractice();
            plugin.getLogger().info("Removed extra dynamic SP arena: " + spArena.getName());
        }
    }

    // ─── Post-Fight Delay ────────────────────────────────────────────────

    /**
     * Single handler for ALL fight end types.
     * Uses handledEnds set to prevent double-handling.
     */
    private void handleFightEnd(Fight fight, List<Player> players) {
        if (handledEnds.contains(fight))
            return;
        handledEnds.add(fight);

        if (manager.getSession(fight) == null)
            return;

        Location spawn = StrikePractice.getAPI().getSpawnLocation();

        for (Player p : players) {
            if (p != null)
                delayedPlayers.add(p.getUniqueId());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cleanupDynamicArena(fight);

            for (Player p : players) {
                if (p != null && p.isOnline()) {
                    delayedPlayers.remove(p.getUniqueId());
                    p.teleport(spawn);
                }
            }

            manager.endSession(fight);
            handledEnds.remove(fight);
            handledStarts.remove(fight);
        }, 60L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFightEnd(FightEndEvent event) {
        handleFightEnd(event.getFight(), event.getFight().getPlayersInFight());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelEnd(DuelEndEvent event) {
        handleFightEnd(event.getFight(), event.getFight().getPlayersInFight());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBotDuelEnd(BotDuelEndEvent event) {
        handleFightEnd(event.getFight(), List.of(event.getPlayer()));
    }
}