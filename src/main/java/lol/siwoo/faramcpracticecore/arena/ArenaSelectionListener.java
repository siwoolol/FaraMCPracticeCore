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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFightStart(FightStartEvent event) {
        Fight fight = event.getFight();
        String arenaName = fight.getArena().getName().toLowerCase();

        if (arenaName.contains("dynamic")) {
            // Free up this SP arena immediately so SP can reuse it for the next queue
            Bukkit.getScheduler().runTaskLater(plugin, () -> fight.getArena().setUsing(false), 2L);

            // Ensure another free dynamic arena exists for the next fight
            ensureDynamicArenaAvailable(fight);
        }

        Player p = fight.getPlayersInFight().get(0);
        ArenaConfig selected = ArenaSelectorGUI.queuedSelections.remove(p.getUniqueId());
        if (selected == null)
            selected = manager.getRandomArenaForKit(fight.getKit().getName());

        if (selected != null)
            startMatch(fight, selected);
    }

    public void startMatch(Fight fight, ArenaConfig config) {
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
            session.setSpArena(fight.getArena());

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
            });
        });
    }

    // ─── Dynamic Arena Management ────────────────────────────────────────

    /**
     * Ensures at least one free dynamic SP arena exists for the next queued fight.
     * If all dynamic arenas of the matching type are in use, clones one.
     */
    private void ensureDynamicArenaAvailable(Fight fight) {
        boolean isBuild = fight.getKit() != null && fight.getKit().isBuild();
        String prefix = isBuild ? "dynamicbuild" : "dynamic";

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
            // All dynamic arenas are in use — create a new one
            Arena template = matching.get(0);
            String newName = prefix + "_" + (matching.size() + 1);

            Arena existing = StrikePractice.getAPI().getArena(newName);
            if (existing != null)
                return; // already exists

            // Clone the template arena with a new name
            Arena clone = StrikePractice.getAPI().getArena(template.getName());
            if (clone == null)
                return;

            // Create a new arena via the SP API by getting the template and saving a copy
            // We use the template's locations — our plugin overrides them per-fight anyway
            Location defaultLoc = new Location(Bukkit.getWorlds().get(0), 0, 100, 0);
            createSpArena(newName, template, defaultLoc);
        }
    }

    /**
     * Creates a new StrikePractice arena by cloning a template's settings.
     */
    private void createSpArena(String name, Arena template, Location defaultLoc) {
        try {
            // Use BattleKit.createKit pattern — SP arenas implement
            // ConfigurationSerializable
            // We serialize the template, modify the name, and save
            Map<String, Object> data = template.serialize();
            data.put("name", name);
            data.put("loc1", defaultLoc);
            data.put("loc2", defaultLoc);
            data.put("center", defaultLoc);

            // Deserialize back into an Arena and register it
            Arena newArena = (Arena) org.bukkit.configuration.serialization.ConfigurationSerialization
                    .deserializeObject(data, template.getClass());

            if (newArena != null) {
                newArena.setUsing(false);
                newArena.setKits(template.getKits());
                newArena.saveForStrikePractice();
                plugin.getLogger().info("Created dynamic SP arena: " + name);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create dynamic arena '" + name + "': " + e.getMessage());
        }
    }

    /**
     * Cleans up extra dynamic SP arenas after a fight — keeps at least 1 of each
     * prefix.
     */
    private void cleanupDynamicArena(FightSession session) {
        Arena spArena = session.getSpArena();
        if (spArena == null)
            return;

        String arenaName = spArena.getName().toLowerCase();
        if (!arenaName.contains("dynamic"))
            return;

        boolean isBuild = arenaName.startsWith("dynamicbuild");
        String prefix = isBuild ? "dynamicbuild" : "dynamic";

        // Count how many arenas share this prefix
        long count = StrikePractice.getAPI().getArenas().stream()
                .filter(a -> a.getName().toLowerCase().startsWith(prefix))
                .count();

        // Only remove if there's more than 1 (keep at least the base arena)
        if (count > 1 && arenaName.contains("_")) {
            spArena.removeFromStrikePractice();
            plugin.getLogger().info("Removed extra dynamic SP arena: " + spArena.getName());
        }
    }

    // ─── Post-Fight Delay ────────────────────────────────────────────────

    /**
     * Keeps players in the arena for 3 seconds after fight ends,
     * then teleports to spawn and cleans up.
     */
    private void handleDelayedTeleport(Fight fight, List<Player> players) {
        Location spawn = StrikePractice.getAPI().getSpawnLocation();

        for (Player p : players) {
            if (p != null)
                delayedPlayers.add(p.getUniqueId());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : players) {
                if (p != null && p.isOnline()) {
                    delayedPlayers.remove(p.getUniqueId());
                    p.teleport(spawn);
                }
            }

            FightSession session = manager.getSession(fight);
            if (session != null) {
                cleanupDynamicArena(session);
            }
            manager.endSession(fight);
        }, 60L); // 60 ticks = 3 seconds
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFightEnd(FightEndEvent event) {
        Fight fight = event.getFight();
        handleDelayedTeleport(fight, fight.getPlayersInFight());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelEnd(DuelEndEvent event) {
        Fight fight = event.getFight();
        handleDelayedTeleport(fight, fight.getPlayersInFight());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBotDuelEnd(BotDuelEndEvent event) {
        Fight fight = event.getFight();
        handleDelayedTeleport(fight, List.of(event.getPlayer()));
    }
}