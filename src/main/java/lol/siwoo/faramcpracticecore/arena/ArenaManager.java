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
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final FaraMCPracticeCore plugin;
    private final Map<String, ArenaConfig> arenas = new HashMap<>();
    private final Map<Fight, FightSession> activeSessions = new ConcurrentHashMap<>();
    private final List<World> worlds = new ArrayList<>();
    private final File folder;
    private int nextX = 0, worldIdx = 0;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "arena");
        if (!folder.exists()) folder.mkdirs();
        setupWorlds();
        loadArenas();
    }

    private void setupWorlds() {
        String[] names = {"pasteArena1", "pasteArena2", "pasteArena3"};
        for (String n : names) {
            World w = Bukkit.getWorld(n);
            if (w == null) w = new WorldCreator(n).type(WorldType.FLAT).generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}").generateStructures(false).createWorld();
            if (w != null) worlds.add(w);
        }
    }

    public void loadArenas() {
        arenas.clear();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) for (File f : files) arenas.put(f.getName().replace(".yml", "").toLowerCase(), new ArenaConfig(f));
    }

    public FightSession createSession(Fight fight, ArenaConfig config) {
        if (worlds.isEmpty()) return null;
        World w = worlds.get(worldIdx);
        Location center = new Location(w, nextX, 100, 0);
        worldIdx = (worldIdx + 1) % worlds.size();
        if (worldIdx == 0) nextX += 5000;

        FightSession session = new FightSession(fight, config, center);
        activeSessions.put(fight, session);
        pasteArena(config, center);
        return session;
    }

    private void pasteArena(ArenaConfig config, Location center) {
        File file = new File(folder, config.getSchematicName());

        // Log a warning if the schematic is missing so you know why players see void/nothing
        if (!file.exists()) {
            plugin.getLogger().warning("Schematic file not found: " + file.getName());
            return;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
            Clipboard cb = reader.read();
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                Operations.complete(new ClipboardHolder(cb).createPaste(session)
                        .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                        .ignoreAirBlocks(false).build());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void endSession(Fight fight) {
        FightSession s = activeSessions.remove(fight);
        if (s != null) clearArena(s.getConfig(), s.getCenter());
    }

    private void clearArena(ArenaConfig config, Location center) {
        Location c1 = center.clone().add(config.getCorner1()), c2 = center.clone().add(config.getCorner2());
        for (int x = Math.min(c1.getBlockX(), c2.getBlockX()); x <= Math.max(c1.getBlockX(), c2.getBlockX()); x++) {
            for (int y = Math.min(c1.getBlockY(), c2.getBlockY()); y <= Math.max(c1.getBlockY(), c2.getBlockY()); y++) {
                for (int z = Math.min(c1.getBlockZ(), c2.getBlockZ()); z <= Math.max(c1.getBlockZ(), c2.getBlockZ()); z++) {
                    center.getWorld().getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    public ArenaConfig getRandomArenaForKit(String kit) {
        List<ArenaConfig> valid = arenas.values().stream().filter(c -> c.isKitAllowed(kit)).toList();
        return valid.isEmpty() ? null : valid.get(new Random().nextInt(valid.size()));
    }

    public Map<String, ArenaConfig> getArenas() { return arenas; }
    public void shutdown() { activeSessions.keySet().forEach(this::endSession); }
}