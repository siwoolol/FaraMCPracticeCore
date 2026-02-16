package lol.siwoo.faramcpracticecore.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import ga.strikepractice.StrikePractice;
import ga.strikepractice.arena.Arena;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.*;
import org.bukkit.util.Vector;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final FaraMCPracticeCore plugin;
    private final Map<String, ArenaConfig> arenas = new HashMap<>();
    private final Map<Fight, FightSession> activeSessions = new ConcurrentHashMap<>();
    private final List<World> pasteWorlds = new ArrayList<>();
    private final File arenaFolder;
    private int nextXOffset = 0, currentWorldIndex = 0;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.arenaFolder = new File(plugin.getDataFolder(), "arena");
        if (!arenaFolder.exists())
            arenaFolder.mkdirs();
        setupWorlds();
        loadArenas();

        // Initialize dynamic SP arenas after StrikePractice is fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, this::initDynamicSpArenas, 40L);
    }

    private void setupWorlds() {
        String[] names = { "pasteArena1", "pasteArena2", "pasteArena3" };
        for (String name : names) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                world = new WorldCreator(name).type(WorldType.FLAT)
                        .generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}")
                        .generateStructures(false).createWorld();
            }
            if (world != null) {
                world.addPluginChunkTicket(0, 0, plugin);
                pasteWorlds.add(world);
            }
        }
    }

    /**
     * On startup: remove all dynamic/dynamicbuild SP arenas, then create fresh
     * ones.
     * This prevents StrikePractice from glitching with stale arena configs.
     */
    private void initDynamicSpArenas() {
        try {
            List<Arena> toRemove = new ArrayList<>();
            for (Arena a : StrikePractice.getAPI().getArenas()) {
                String name = a.getName().toLowerCase();
                if (name.startsWith("dynamic")) {
                    toRemove.add(a);
                }
            }

            for (Arena a : toRemove) {
                a.removeFromStrikePractice();
                plugin.getLogger().info("Removed stale SP arena: " + a.getName());
            }

            // Create fresh "dynamic" and "dynamicbuild" arenas
            Location defaultLoc = new Location(Bukkit.getWorlds().get(0), 0, 100, 0);
            createFreshSpArena("dynamic", defaultLoc, false);
            createFreshSpArena("dynamicbuild", defaultLoc, true);

            plugin.getLogger().info("Dynamic SP arenas initialized.");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to init dynamic SP arenas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a fresh SP arena with the given name using the SP API.
     */
    private void createFreshSpArena(String name, Location loc, boolean isBuild) {
        try {
            // Check if any existing arena can be used as a template for serialization
            List<Arena> existing = StrikePractice.getAPI().getArenas();
            if (existing.isEmpty()) {
                plugin.getLogger().warning("No existing SP arenas to use as template for " + name);
                return;
            }

            Arena template = existing.get(0);
            Map<String, Object> data = template.serialize();
            data.put("name", name);
            data.put("loc1", loc.clone());
            data.put("loc2", loc.clone());
            data.put("center", loc.clone());

            Arena newArena = (Arena) org.bukkit.configuration.serialization.ConfigurationSerialization
                    .deserializeObject(data, template.getClass());

            if (newArena != null) {
                newArena.setUsing(false);
                if (isBuild)
                    newArena.setBuild(true);
                // Allow all kits
                newArena.setKits(new ArrayList<>());
                newArena.saveForStrikePractice();
                plugin.getLogger().info("Created fresh SP arena: " + name);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create SP arena '" + name + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadArenas() {
        arenas.clear();
        File[] files = arenaFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files)
                arenas.put(f.getName().replace(".yml", "").toLowerCase(), new ArenaConfig(f));
        }
    }

    public CompletableFuture<FightSession> createSession(Fight fight, ArenaConfig config) {
        if (pasteWorlds.isEmpty())
            return CompletableFuture.completedFuture(null);
        World world = pasteWorlds.get(currentWorldIndex);
        Location center = new Location(world, nextXOffset, 100, 0);

        // Track chunk for this match to keep it loaded
        world.addPluginChunkTicket(center.getBlockX() >> 4, center.getBlockZ() >> 4, plugin);

        currentWorldIndex = (currentWorldIndex + 1) % pasteWorlds.size();
        if (currentWorldIndex == 0)
            nextXOffset += 5000;

        FightSession session = new FightSession(fight, config, center);
        activeSessions.put(fight, session);

        return pasteArena(config, center).thenApply(v -> session);
    }

    private CompletableFuture<Void> pasteArena(ArenaConfig config, Location center) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        File file = new File(arenaFolder, config.getSchematicName());
        if (!file.exists()) {
            future.complete(null);
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
                Clipboard cb = reader.read();

                try (EditSession session = WorldEdit.getInstance()
                        .newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                    session.setSideEffectApplier(SideEffectSet.none());

                    Vector offset = config.getCenter();
                    BlockVector3 pastePos = BlockVector3.at(
                            center.getX() + offset.getX(),
                            center.getY() + offset.getY(),
                            center.getZ() + offset.getZ());

                    Operations.complete(new ClipboardHolder(cb).createPaste(session)
                            .to(pastePos)
                            .ignoreAirBlocks(false).build());
                }
                future.complete(null);
            } catch (Exception e) {
                e.printStackTrace();
                future.complete(null);
            }
        });
        return future;
    }

    public void endSession(Fight fight) {
        FightSession s = activeSessions.remove(fight);

        if (s != null) {
            // Clear blocks first, THEN release the chunk ticket
            clearArenaAsync(s.getConfig(), s.getCenter()).thenRun(() -> {
                // Release chunk ticket on the main thread after blocks are cleared
                Bukkit.getScheduler().runTask(plugin, () -> {
                    s.getCenter().getWorld().removePluginChunkTicket(
                            s.getCenter().getBlockX() >> 4,
                            s.getCenter().getBlockZ() >> 4,
                            plugin);
                });
            });
        }
    }

    private CompletableFuture<Void> clearArenaAsync(ArenaConfig config, Location center) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance()
                    .newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                session.setSideEffectApplier(SideEffectSet.none());

                Vector c1Offset = config.getCorner1();
                Vector c2Offset = config.getCorner2();

                BlockVector3 min = BlockVector3.at(
                        center.getBlockX() + Math.min(c1Offset.getBlockX(), c2Offset.getBlockX()),
                        center.getBlockY() + Math.min(c1Offset.getBlockY(), c2Offset.getBlockY()),
                        center.getBlockZ() + Math.min(c1Offset.getBlockZ(), c2Offset.getBlockZ()));
                BlockVector3 max = BlockVector3.at(
                        center.getBlockX() + Math.max(c1Offset.getBlockX(), c2Offset.getBlockX()),
                        center.getBlockY() + Math.max(c1Offset.getBlockY(), c2Offset.getBlockY()),
                        center.getBlockZ() + Math.max(c1Offset.getBlockZ(), c2Offset.getBlockZ()));

                CuboidRegion region = new CuboidRegion(min, max);
                session.setBlocks((Region) region, BukkitAdapter.adapt(Material.AIR.createBlockData()));
                session.flushQueue();

                plugin.getLogger().info("Cleared arena blocks at " + center.getBlockX() + ", " + center.getBlockY()
                        + ", " + center.getBlockZ());
                future.complete(null);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to clear arena: " + e.getMessage());
                e.printStackTrace();
                future.complete(null);
            }
        });
        return future;
    }

    public ArenaConfig getRandomArenaForKit(String kit) {
        List<ArenaConfig> valid = arenas.values().stream().filter(c -> c.isKitAllowed(kit)).toList();
        return valid.isEmpty() ? null : valid.get(new Random().nextInt(valid.size()));
    }

    public FightSession getSession(Fight fight) {
        return activeSessions.get(fight);
    }

    public Map<String, ArenaConfig> getArenas() {
        return new HashMap<>(arenas);
    }

    public void shutdown() {
        activeSessions.keySet().forEach(this::endSession);
    }
}