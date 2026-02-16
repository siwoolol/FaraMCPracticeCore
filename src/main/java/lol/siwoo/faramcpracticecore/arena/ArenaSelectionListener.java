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

    private final Set<UUID> pendingPaste = new HashSet<>();
    private final Set<UUID> delayedPlayers = new HashSet<>();
    private final Set<Fight> handledStarts = new HashSet<>();
    private final Set<Fight> handledEnds = new HashSet<>();

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (pendingPaste.contains(uuid) || delayedPlayers.contains(uuid)) {
            event.setCancelled(true);
        }
    }

    // ─── Fight Start Handlers ─────────────────────────────────────────────
    //
    // Bot fights: Skip our paste system entirely — Citizens NPCs can't cross
    // worlds.
    // SP handles bot fights normally on the dynamic arena.
    //
    // Duels + Unranked: SP fires DuelStartEvent BEFORE the arena is assigned
    // (arena=null),
    // then fires FightStartEvent AFTER the arena is set.
    // So we skip DuelStartEvent and only process FightStartEvent.

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBotDuelStart(BotDuelStartEvent event) {
        // Bot fights: Citizens NPCs can't teleport across worlds.
        // We just handle the dynamic arena management (freeing + ensuring availability)
        // but DON'T paste or teleport — let SP handle it normally.
        Fight fight = event.getFight();
        Arena spArena = fight.getArena();
        if (spArena == null)
            return;

        String arenaName = spArena.getName().toLowerCase();
        if (!arenaName.contains("dynamic"))
            return;

        plugin.getLogger().info("[Arena] Bot fight on '" + arenaName + "' — letting SP handle (no paste)");

        // Free up and ensure availability for next fight
        Bukkit.getScheduler().runTaskLater(plugin, () -> spArena.setUsing(false), 2L);
        boolean isBuild = fight.getKit() != null && fight.getKit().isBuild();
        ensureDynamicArenaAvailable(isBuild ? "dynamicbuild" : "dynamic");

        // Mark as handled so onFightStart doesn't also process it
        handledStarts.add(fight);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFightStart(FightStartEvent event) {
        // This catches ALL fight types including DuelStartEvent.
        // BotDuelStartEvent is already handled above and marked in handledStarts.
        Fight fight = event.getFight();

        if (handledStarts.contains(fight)) {
            return;
        }
        handledStarts.add(fight);

        Arena spArena = fight.getArena();
        if (spArena == null) {
            // Arena not assigned yet (SP fires DuelStartEvent before assigning arena).
            // Remove from handledStarts so the next event for this fight can try again.
            handledStarts.remove(fight);
            plugin.getLogger().info("[Arena] Arena is null, will retry on next event for this fight");
            return;
        }

        String arenaName = spArena.getName().toLowerCase();
        if (!arenaName.contains("dynamic")) {
            return;
        }

        List<Player> players = fight.getPlayersInFight();
        plugin.getLogger().info("[Arena] Processing fight on '" + arenaName + "' | type="
                + fight.getClass().getSimpleName() + " | players=" + players.size());

        Bukkit.getScheduler().runTaskLater(plugin, () -> spArena.setUsing(false), 2L);

        boolean isBuild = fight.getKit() != null && fight.getKit().isBuild();
        String prefix = isBuild ? "dynamicbuild" : "dynamic";
        ensureDynamicArenaAvailable(prefix);

        ArenaConfig selected = null;
        if (!players.isEmpty()) {
            selected = ArenaSelectorGUI.queuedSelections.remove(players.get(0).getUniqueId());
        }
        if (selected == null && fight.getKit() != null) {
            selected = manager.getRandomArenaForKit(fight.getKit().getName());
        }

        if (selected != null) {
            plugin.getLogger().info("[Arena] Using config '" + selected.getName() + "'");
            startMatch(fight, selected, spArena, players);
        } else {
            plugin.getLogger().warning("[Arena] No arena config found! Kit="
                    + (fight.getKit() != null ? fight.getKit().getName() : "null")
                    + " | Available configs=" + manager.getArenas().keySet());
        }
    }

    private void startMatch(Fight fight, ArenaConfig config, Arena spArena, List<Player> players) {
        for (Player p : players) {
            if (p != null)
                pendingPaste.add(p.getUniqueId());
        }

        manager.createSession(fight, config).thenAccept(session -> {
            if (session == null)
                return;
            session.setSpArena(spArena);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Location origin = session.getCenter().clone().add(config.getCenter());

                Location s1 = origin.clone().add(config.getPos1());
                Location s2 = origin.clone().add(config.getPos2());

                Vector dir1 = s2.toVector().subtract(s1.toVector()).setY(0);
                s1.setDirection(dir1);
                Vector dir2 = s1.toVector().subtract(s2.toVector()).setY(0);
                s2.setDirection(dir2);

                fight.getArena().setLoc1(s1);
                fight.getArena().setLoc2(s2);

                for (Player p : players) {
                    if (p != null)
                        pendingPaste.remove(p.getUniqueId());
                }
                if (players.size() >= 1)
                    players.get(0).teleport(s1);
                if (players.size() >= 2)
                    players.get(1).teleport(s2);

                plugin.getLogger().info("[Arena] Teleported " + players.size() + " player(s) to '"
                        + config.getName() + "' in " + s1.getWorld().getName());
            });
        });
    }

    // ─── Dynamic Arena Management ────────────────────────────────────────

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
                plugin.getLogger().info("[Arena] Created dynamic SP arena: " + name);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Arena] Failed to create '" + name + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            plugin.getLogger().info("[Arena] Removed extra SP arena: " + spArena.getName());
        }
    }

    // ─── Fight End Handlers ──────────────────────────────────────────────

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