package lol.siwoo.faramcpracticecore.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
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
    private final Map<UUID, FightSession> activeSessions = new ConcurrentHashMap<>();
    private final List<World> pasteWorlds = new ArrayList<>();
    private final File arenaFolder;

    private int nextXOffset = 0;
    private int currentWorldIndex = 0;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.arenaFolder = new File(plugin.getDataFolder(), "arena");
        setupWorlds();
        loadArenas();
    }

    private void setupWorlds() {
        String[] names = {"pasteArena1", "pasteArena2", "pasteArena3"};
        for (String name : names) {
            World w = Bukkit.getWorld(name);
            if (w == null) {
                WorldCreator creator = new WorldCreator(name);
                creator.type(WorldType.FLAT).generateStructures(false);
                creator.generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}");
                w = creator.createWorld();
            }
            if (w != null) pasteWorlds.add(w);
        }
    }

    public void loadArenas() {
        arenas.clear();
        File[] files = arenaFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            ArenaConfig config = new ArenaConfig(file);
            arenas.put(config.getName().toLowerCase(), config);
        }
    }

    public FightSession createSession(Fight fight, ArenaConfig config) {
        if (pasteWorlds.isEmpty()) return null;

        World world = pasteWorlds.get(currentWorldIndex);
        Location center = new Location(world, nextXOffset, 100, 0);

        // Maintain 3000 block distance
        currentWorldIndex = (currentWorldIndex + 1) % pasteWorlds.size();
        if (currentWorldIndex == 0) nextXOffset += 3000;

        FightSession session = new FightSession(fight, config, center);
        activeSessions.put(fight.getUniqueId(), session);
        pasteArena(config, center);
        return session;
    }

    private void pasteArena(ArenaConfig config, Location center) {
        File schematicFile = new File(arenaFolder, config.getSchematicName());
        if (!schematicFile.exists()) return;

        try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                Operations.complete(new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                        .ignoreAirBlocks(false).build());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void endSession(Fight fight) {
        FightSession session = activeSessions.remove(fight.getUniqueId());
        if (session != null) {
            clearArena(session.getConfig(), session.getCenter());
        }
    }

    private void clearArena(ArenaConfig config, Location center) {
        Location c1 = center.clone().add(config.getCorner1());
        Location c2 = center.clone().add(config.getCorner2());
        int minX = Math.min(c1.getBlockX(), c2.getBlockX()), maxX = Math.max(c1.getBlockX(), c2.getBlockX());
        int minY = Math.min(c1.getBlockY(), c2.getBlockY()), maxY = Math.max(c1.getBlockY(), c2.getBlockY());
        int minZ = Math.min(c1.getBlockZ(), c2.getBlockZ()), maxZ = Math.max(c1.getBlockZ(), c2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    center.getWorld().getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    public ArenaConfig getRandomArenaForKit(String kitName) {
        List<ArenaConfig> valid = arenas.values().stream().filter(c -> c.isKitAllowed(kitName)).toList();
        return valid.isEmpty() ? null : valid.get(new Random().nextInt(valid.size()));
    }

    public Map<String, ArenaConfig> getArenas() { return new HashMap<>(arenas); }
    public FightSession getSession(Fight fight) { return activeSessions.get(fight.getUniqueId()); }
}