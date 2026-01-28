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
import org.bukkit.configuration.file.YamlConfiguration;
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
        if (!arenaFolder.exists()) arenaFolder.mkdirs(); // Ensure folder exists
        setupWorlds();
        loadArenas();
    }

    private void setupWorlds() {
        String[] names = {"pasteArena1", "pasteArena2", "pasteArena3"};
        for (String name : names) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                world = new WorldCreator(name).type(WorldType.FLAT).generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}").generateStructures(false).createWorld();
            }
            if (world != null) pasteWorlds.add(world);
        }
    }

    public void loadArenas() {
        arenas.clear();
        File[] files = arenaFolder.listFiles();
        if (files == null) return;

        // Iterate through all files to find schematics first
        for (File f : files) {
            String fileName = f.getName().toLowerCase();
            if (fileName.endsWith(".schem") || fileName.endsWith(".schematic")) {
                String baseName = f.getName().split("\\.")[0];
                File configFile = new File(arenaFolder, baseName + ".yml");

                // If the config doesn't exist, create the default one
                if (!configFile.exists()) {
                    saveDefault(f, configFile);
                }
            }
        }

        // Now load all YAML files into the arenas map
        File[] configFiles = arenaFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (configFiles != null) {
            for (File f : configFiles) {
                arenas.put(f.getName().replace(".yml", "").toLowerCase(), new ArenaConfig(f));
            }
        }
    }

    private void saveDefault(File schemFile, File configFile) {
        YamlConfiguration yaml = new YamlConfiguration();
        String name = schemFile.getName().split("\\.")[0];

        yaml.set("name", name);
        yaml.set("schematic", schemFile.getName());
        yaml.set("pos1", new Vector(10, 5, 0));
        yaml.set("pos2", new Vector(-10, 5, 0));
        yaml.set("corner1", new Vector(30, 30, 30));
        yaml.set("corner2", new Vector(-30, 0, -30));
        yaml.set("center", new Vector(0, 0, 0));
        yaml.set("kits", Collections.singletonList("sword"));

        try {
            yaml.save(configFile);
            plugin.getLogger().info("Generated default config for: " + schemFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FightSession createSession(Fight fight, ArenaConfig config) {
        if (pasteWorlds.isEmpty()) return null;
        World world = pasteWorlds.get(currentWorldIndex);
        Location center = new Location(world, nextXOffset, 100, 0);

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
        try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
            Clipboard cb = reader.read();
            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                Operations.complete(new ClipboardHolder(cb).createPaste(session).to(BlockVector3.at(center.getX(), center.getY(), center.getZ())).ignoreAirBlocks(false).build());
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
    public FightSession getSession(Fight fight) { return activeSessions.get(fight); }
    public void shutdown() { activeSessions.keySet().forEach(this::endSession); }
}