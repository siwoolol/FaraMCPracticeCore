package lol.siwoo.faramcpracticecore.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.*;
import org.bukkit.util.Vector;
import java.io.*;
import java.util.*;
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
        if (!arenaFolder.exists()) arenaFolder.mkdirs();
        setupWorlds();
        loadArenas();
    }

    private void setupWorlds() {
        String[] names = {"pasteArena1", "pasteArena2", "pasteArena3"};
        for (String name : names) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                world = new WorldCreator(name).type(WorldType.FLAT)
                        .generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}")
                        .generateStructures(false).createWorld();
            }
            if (world != null) {
                // Persistent loading for Paper 1.21.1+
                world.addPluginChunkTicket(0, 0, plugin);
                pasteWorlds.add(world);
            }
        }
    }

    public void loadArenas() {
        arenas.clear();
        File[] files = arenaFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) arenas.put(f.getName().replace(".yml", "").toLowerCase(), new ArenaConfig(f));
        }
    }

    public FightSession createSession(Fight fight, ArenaConfig config) {
        if (pasteWorlds.isEmpty()) return null;
        World world = pasteWorlds.get(currentWorldIndex);
        Location center = new Location(world, nextXOffset, 100, 0);

        // Track chunk for this match to keep it loaded
        world.addPluginChunkTicket(center.getBlockX() >> 4, center.getBlockZ() >> 4, plugin);

        currentWorldIndex = (currentWorldIndex + 1) % pasteWorlds.size();
        if (currentWorldIndex == 0) nextXOffset += 5000;

        FightSession session = new FightSession(fight, config, center);
        activeSessions.put(fight, session);

        pasteArena(config, center);
        return session;
    }

    private void pasteArena(ArenaConfig config, Location center) {
        File file = new File(arenaFolder, config.getSchematicName());
        if (!file.exists()) return;

        // Step 1: Read the file off-thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
                Clipboard cb = reader.read();

                // Step 2: Perform the actual block placement on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                        // CRITICAL: Disable side-effects to prevent the "Async block onPlace" error and speed up paste
                        session.setSideEffectApplier(SideEffectSet.none());

                        Vector offset = config.getCenter();
                        BlockVector3 pastePos = BlockVector3.at(
                                center.getX() + offset.getX(),
                                center.getY() + offset.getY(),
                                center.getZ() + offset.getZ()
                        );

                        Operations.complete(new ClipboardHolder(cb).createPaste(session)
                                .to(pastePos)
                                .ignoreAirBlocks(false).build());
                    } catch (Exception e) { e.printStackTrace(); }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void endSession(Fight fight) {
        FightSession s = activeSessions.remove(fight);

        if (s != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                clearArena(s.getConfig(), s.getCenter());

                s.getCenter().getWorld().removePluginChunkTicket(
                        s.getCenter().getBlockX() >> 4,
                        s.getCenter().getBlockZ() >> 4,
                        plugin
                );
            }, 20L);
        }
    }

    private void clearArena(ArenaConfig config, Location center) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
            session.setSideEffectApplier(SideEffectSet.none());

            Vector c1Offset = config.getCorner1();
            Vector c2Offset = config.getCorner2();

            BlockVector3 min = BlockVector3.at(
                    center.getBlockX() + Math.min(c1Offset.getBlockX(), c2Offset.getBlockX()),
                    center.getBlockY() + Math.min(c1Offset.getBlockY(), c2Offset.getBlockY()),
                    center.getBlockZ() + Math.min(c1Offset.getBlockZ(), c2Offset.getBlockZ())
            );
            BlockVector3 max = BlockVector3.at(
                    center.getBlockX() + Math.max(c1Offset.getBlockX(), c2Offset.getBlockX()),
                    center.getBlockY() + Math.max(c1Offset.getBlockY(), c2Offset.getBlockY()),
                    center.getBlockZ() + Math.max(c1Offset.getBlockZ(), c2Offset.getBlockZ())
            );

            CuboidRegion region = new CuboidRegion(min, max);
            session.setBlocks(region, BukkitAdapter.adapt(Material.AIR.createBlockData()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArenaConfig getRandomArenaForKit(String kit) {
        List<ArenaConfig> valid = arenas.values().stream().filter(c -> c.isKitAllowed(kit)).toList();
        return valid.isEmpty() ? null : valid.get(new Random().nextInt(valid.size()));
    }

    public Map<String, ArenaConfig> getArenas() { return new HashMap<>(arenas); }
    public void shutdown() { activeSessions.keySet().forEach(this::endSession); }
}