package lol.siwoo.faramcpracticecore.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import ga.strikepractice.fights.Fight;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ArenaManager {
    private final FaraMCPracticeCore plugin;
    private final Map<String, ArenaConfig> arenas = new HashMap<>();
    private final Map<Fight, FightSession> activeSessions = new HashMap<>();
    private final List<World> pasteWorlds = new ArrayList<>();
    private final File folder;

    private int nextXOffset = 0;
    private int currentWorldIndex = 0;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "arena");
        if (!folder.exists()) folder.mkdirs();

        setupWorlds();
        load();
    }

    private void setupWorlds() {
        String[] names = {"pasteArena1", "pasteArena2", "pasteArena3"};
        for (String name : names) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                WorldCreator creator = new WorldCreator(name);
                creator.type(WorldType.FLAT).generateStructures(false);
                creator.generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}");
                world = creator.createWorld();
            }
            if (world != null) pasteWorlds.add(world);
        }
    }

    public FightSession createSession(Fight fight, ArenaConfig config) {
        World world = pasteWorlds.get(currentWorldIndex);
        Location center = new Location(world, nextXOffset, 100, 0); // The 'center' logic

        currentWorldIndex = (currentWorldIndex + 1) % pasteWorlds.size();
        if (currentWorldIndex == 0) nextXOffset += 3000; // 3000 block separation

        FightSession session = new FightSession(fight, config, center);
        activeSessions.put(fight, session);
        paste(config, center);
        return session;
    }

    public void endSession(Fight fight) {
        FightSession session = activeSessions.remove(fight);
        if (session != null) clear(session.getConfig(), session.getCenter());
    }

    public void load() {
        arenas.clear();
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.getName().endsWith(".schem") || f.getName().endsWith(".schematic")) {
                File configFile = new File(folder, f.getName().split("\\.")[0] + ".yml");
                if (!configFile.exists()) saveDefault(f, configFile); // Auto-generate config

                ArenaConfig config = new ArenaConfig(configFile);
                arenas.put(config.getName().toLowerCase(), config);
            }
        }
    }

    private void saveDefault(File schemFile, File configFile) {
        YamlConfiguration yaml = new YamlConfiguration();
        String name = schemFile.getName().split("\\.")[0];
        yaml.set("name", name);
        yaml.set("schematic", schemFile.getName());
        yaml.set("pos1", new Vector(10, 0, 0));
        yaml.set("pos2", new Vector(-10, 0, 0));
        yaml.set("corner1", new Vector(50, 50, 50));
        yaml.set("corner2", new Vector(-50, -50, -50));
        yaml.set("center", new Vector(0, 0, 0)); // Save default center
        try {
            yaml.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void paste(ArenaConfig config, Location center) {
        File file = new File(folder, config.getSchematicName());
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) return;

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                Operation op = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                        .ignoreAirBlocks(false).build();
                Operations.complete(op);
            }
        } catch (IOException | WorldEditException e) { e.printStackTrace(); }
    }

    public void clear(ArenaConfig config, Location center) {
        Location c1 = center.clone().add(config.getCorner1());
        Location c2 = center.clone().add(config.getCorner2());
        int x1 = Math.min(c1.getBlockX(), c2.getBlockX()), x2 = Math.max(c1.getBlockX(), c2.getBlockX());
        int y1 = Math.min(c1.getBlockY(), c2.getBlockY()), y2 = Math.max(c1.getBlockY(), c2.getBlockY());
        int z1 = Math.min(c1.getBlockZ(), c2.getBlockZ()), z2 = Math.max(c1.getBlockZ(), c2.getBlockZ());

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    center.getWorld().getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    public Map<String, ArenaConfig> getArenas() { return arenas; }
}