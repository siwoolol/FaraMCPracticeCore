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

import static org.bukkit.GameRules.ADVANCE_TIME;
import static org.bukkit.GameRules.ADVANCE_WEATHER;

public class ArenaManager {
    private final FaraMCPracticeCore plugin;
    private final Map<String, ArenaConfig> arenas = new HashMap<>();
    private final Map<Fight, FightSession> activeSessions = new HashMap<>();
    private final List<World> pasteWorlds = new ArrayList<>();
    private final File arenaFolder;

    private int nextXOffset = 0;
    private int currentWorldIndex = 0;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.arenaFolder = new File(plugin.getDataFolder(), "arena");

        plugin.getLogger().info("Initializing Arena Manager...");
        createArenaDirectory();
        setupWorlds();
        loadArenas();
        plugin.getLogger().info("Arena Manager initialized with " + arenas.size() + " arenas loaded");
    }

    private void createArenaDirectory() {
        if (!arenaFolder.exists()) {
            if (arenaFolder.mkdirs()) {
                plugin.getLogger().info("Created arena directory: " + arenaFolder.getAbsolutePath());
                createExampleFiles();
            } else {
                plugin.getLogger().severe("Failed to create arena directory!");
            }
        }
    }

    private void createExampleFiles() {
        // Create a default arena schematic and config if none exist
        createDefaultArena();
        
        // Create example config file to show structure
        File exampleConfig = new File(arenaFolder, "example.yml");
        if (!exampleConfig.exists()) {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("name", "Example Arena");
            yaml.set("schematic", "example.schem");
            yaml.set("pos1", new Vector(10, 5, 0));
            yaml.set("pos2", new Vector(-10, 5, 0));
            yaml.set("corner1", new Vector(30, 30, 30));
            yaml.set("corner2", new Vector(-30, 0, -30));
            yaml.set("center", new Vector(0, 0, 0));
            yaml.set("kits", Arrays.asList("sword", "nodebuff", "gapple"));

            try {
                yaml.save(exampleConfig);
                plugin.getLogger().info("Created example arena config");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create example config: " + e.getMessage());
            }
        }
    }

    private void createDefaultArena() {
        File defaultConfig = new File(arenaFolder, "default.yml");
        
        // Only create if no arena configs exist at all
        File[] ymlFiles = arenaFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("example.yml"));
        if (ymlFiles == null || ymlFiles.length == 0) {
            plugin.getLogger().info("No arena configs found, creating default arena...");
            
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("name", "Default Arena");
            yaml.set("schematic", "default_generated");
            yaml.set("pos1", new Vector(15, 5, 0));
            yaml.set("pos2", new Vector(-15, 5, 0));
            yaml.set("corner1", new Vector(50, 50, 50));
            yaml.set("corner2", new Vector(-50, 0, -50));
            yaml.set("center", new Vector(0, 0, 0));
            yaml.set("kits", new ArrayList<>()); // Allow all kits

            try {
                yaml.save(defaultConfig);
                plugin.getLogger().info("Created default arena config");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create default config: " + e.getMessage());
            }
        }
    }

    private void setupWorlds() {
        String[] worldNames = {"pasteArena1", "pasteArena2", "pasteArena3"};

        for (String name : worldNames) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                plugin.getLogger().info("Creating paste world: " + name);
                WorldCreator creator = new WorldCreator(name);
                creator.type(WorldType.FLAT);
                creator.generateStructures(false);
                creator.generatorSettings("{\"layers\": [], \"biome\":\"minecraft:the_void\"}");
                world = creator.createWorld();
            }

            if (world != null) {
                pasteWorlds.add(world);
                // Set world properties for arena use
                world.setGameRule(ADVANCE_TIME, false);
                world.setGameRule(ADVANCE_WEATHER, false);
                world.setTime(6000); // Noon
                plugin.getLogger().info("Configured paste world: " + name);
            } else {
                plugin.getLogger().severe("Failed to create/load paste world: " + name);
            }
        }

        if (pasteWorlds.isEmpty()) {
            plugin.getLogger().severe("No paste worlds available! Arena system cannot function!");
        }
    }

    public void loadArenas() {
        arenas.clear();
        File[] files = arenaFolder.listFiles();

        if (files == null || files.length == 0) {
            plugin.getLogger().info("No arena files found in " + arenaFolder.getAbsolutePath());
            return;
        }

        int loadedCount = 0;
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".yml") && !file.getName().equals("example.yml")) {
                try {
                    ArenaConfig config = new ArenaConfig(file);

                    // Special handling for generated arenas (don't require schematic file)
                    if (!config.getSchematicName().equals("default_generated")) {
                        // Check if schematic file exists for non-generated arenas
                        File schematicFile = new File(arenaFolder, config.getSchematicName());
                        if (!schematicFile.exists()) {
                            plugin.getLogger().warning("Schematic file not found for arena '" + config.getName() +
                                    "': " + config.getSchematicName() + " (skipping)");
                            continue;
                        }
                    }

                    arenas.put(config.getName().toLowerCase(), config);
                    loadedCount++;
                    plugin.getLogger().info("Loaded arena: " + config.getName() +
                            " (schematic: " + config.getSchematicName() + ")");

                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load arena config " + file.getName() + ": " + e.getMessage());
                }
            }
        }

        plugin.getLogger().info("Loaded " + loadedCount + " arenas successfully");
        
        // If still no arenas loaded, something is wrong
        if (arenas.isEmpty()) {
            plugin.getLogger().warning("No arenas could be loaded! Creating emergency fallback arena...");
            createEmergencyArena();
        }
    }

    private void createEmergencyArena() {
        // Create a simple in-memory arena config as absolute fallback
        try {
            ArenaConfig emergencyConfig = new ArenaConfig() {
                @Override
                public String getName() { return "Emergency Arena"; }
                @Override
                public String getSchematicName() { return "emergency_generated"; }
                @Override
                public Vector getPos1() { return new Vector(10, 5, 0); }
                @Override
                public Vector getPos2() { return new Vector(-10, 5, 0); }
                @Override
                public Vector getCorner1() { return new Vector(30, 30, 30); }
                @Override
                public Vector getCorner2() { return new Vector(-30, 0, -30); }
                @Override
                public Vector getCenter() { return new Vector(0, 0, 0); }
                @Override
                public List<String> getKits() { return new ArrayList<>(); }
                @Override
                public boolean isKitAllowed(String kitName) { return true; }
            };
        
        arenas.put("emergency arena", emergencyConfig);
        plugin.getLogger().info("Created emergency fallback arena");
    } catch (Exception e) {
        plugin.getLogger().severe("Failed to create emergency arena: " + e.getMessage());
    }
}

    public FightSession createSession(Fight fight, ArenaConfig config) {
        if (pasteWorlds.isEmpty()) {
            plugin.getLogger().severe("No paste worlds available for arena session!");
            return null;
        }

        World world = pasteWorlds.get(currentWorldIndex);
        Location center = new Location(world, nextXOffset, 100, 0);

        // Update offsets for next arena
        currentWorldIndex = (currentWorldIndex + 1) % pasteWorlds.size();
        if (currentWorldIndex == 0) {
            nextXOffset += 5000; // Increased separation for safety
        }

        FightSession session = new FightSession(fight, config, center);
        activeSessions.put(fight, session);

        plugin.getLogger().info("Created arena session for " + config.getName() +
                " at " + center.getBlockX() + ", " + center.getBlockY() + ", " + center.getBlockZ());

        // Paste the arena
        pasteArena(config, center);

        return session;
    }

    public void endSession(Fight fight) {
        FightSession session = activeSessions.remove(fight);
        if (session != null) {
            plugin.getLogger().info("Ending arena session for " + session.getConfig().getName());
            clearArena(session.getConfig(), session.getCenter());
        }
    }

    private void pasteArena(ArenaConfig config, Location center) {
    // Handle generated arenas (create simple platform)
    if (config.getSchematicName().equals("default_generated") || config.getSchematicName().equals("emergency_generated")) {
        createSimplePlatform(center, config.getName());
        return;
    }
    
    File schematicFile = new File(arenaFolder, config.getSchematicName());

    if (!schematicFile.exists()) {
        plugin.getLogger().severe("Schematic file not found: " + schematicFile.getAbsolutePath());
        plugin.getLogger().info("Creating simple platform instead...");
        createSimplePlatform(center, config.getName());
        return;
    }

    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
    if (format == null) {
        plugin.getLogger().severe("Unsupported schematic format: " + schematicFile.getName());
        plugin.getLogger().info("Creating simple platform instead...");
        createSimplePlatform(center, config.getName());
        return;
    }

    try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
        Clipboard clipboard = reader.read();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
            editSession.setFastMode(true); // Speed up pasting

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                    .ignoreAirBlocks(false)
                    .build();

            Operations.complete(operation);
            plugin.getLogger().info("Successfully pasted arena: " + config.getName());

        } catch (WorldEditException e) {
            plugin.getLogger().severe("WorldEdit error while pasting arena: " + e.getMessage());
            plugin.getLogger().info("Creating simple platform instead...");
            createSimplePlatform(center, config.getName());
        }

    } catch (IOException e) {
        plugin.getLogger().severe("Failed to read schematic file " + schematicFile.getName() + ": " + e.getMessage());
        plugin.getLogger().info("Creating simple platform instead...");
        createSimplePlatform(center, config.getName());
    }
}

private void createSimplePlatform(Location center, String arenaName) {
    World world = center.getWorld();
    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();
    
    // Create a simple 30x30 stone platform
    for (int x = centerX - 15; x <= centerX + 15; x++) {
        for (int z = centerZ - 15; z <= centerZ + 15; z++) {
            world.getBlockAt(x, centerY - 1, z).setType(Material.STONE);
            
            // Add some barriers around the edge for safety
            if (x == centerX - 15 || x == centerX + 15 || z == centerZ - 15 || z == centerZ + 15) {
                world.getBlockAt(x, centerY, z).setType(Material.BARRIER);
                world.getBlockAt(x, centerY + 1, z).setType(Material.BARRIER);
            }
        }
    }
    
    // Add spawn platforms
    world.getBlockAt(centerX + 10, centerY, centerZ).setType(Material.EMERALD_BLOCK);
    world.getBlockAt(centerX - 10, centerY, centerZ).setType(Material.REDSTONE_BLOCK);
    
    plugin.getLogger().info("Created simple platform for arena: " + arenaName);
}

    private void clearArena(ArenaConfig config, Location center) {
        if (config.getCorner1() == null || config.getCorner2() == null) {
            plugin.getLogger().warning("Cannot clear arena " + config.getName() + " - missing corner coordinates");
            return;
        }

        Location corner1 = center.clone().add(config.getCorner1());
        Location corner2 = center.clone().add(config.getCorner2());

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        World world = center.getWorld();
        int blocksCleared = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    blocksCleared++;
                }
            }
        }

        plugin.getLogger().info("Cleared arena " + config.getName() + " (" + blocksCleared + " blocks)");
    }

    public void reloadArenas() {
        plugin.getLogger().info("Reloading arena configurations...");
        loadArenas();
    }

    public ArenaConfig getArenaByName(String name) {
        return arenas.get(name.toLowerCase());
    }

    public ArenaConfig getRandomArena() {
        if (arenas.isEmpty()) {
            return null;
        }
        List<ArenaConfig> configs = new ArrayList<>(arenas.values());
        return configs.get(new Random().nextInt(configs.size()));
    }

    public ArenaConfig getRandomArenaForKit(String kitName) {
        List<ArenaConfig> validArenas = arenas.values().stream()
                .filter(config -> config.getKits().isEmpty() ||
                        config.getKits().contains(kitName.toLowerCase()))
                .toList();

        if (validArenas.isEmpty()) {
            return getRandomArena(); // Fallback to any arena
        }

        return validArenas.get(new Random().nextInt(validArenas.size()));
    }

    public Map<String, ArenaConfig> getArenas() {
        return new HashMap<>(arenas);
    }

    public FightSession getSession(Fight fight) {
        return activeSessions.get(fight);
    }

    public boolean isArenaFolder(File file) {
        return file.equals(arenaFolder);
    }

    public File getArenaFolder() {
        return arenaFolder;
    }

    public int getLoadedArenaCount() {
        return arenas.size();
    }

    public void shutdown() {
        plugin.getLogger().info("Shutting down Arena Manager...");

        // Clear all active sessions
        for (Map.Entry<Fight, FightSession> entry : new HashMap<>(activeSessions).entrySet()) {
            endSession(entry.getKey());
        }

        activeSessions.clear();
        arenas.clear();
        pasteWorlds.clear();

        plugin.getLogger().info("Arena Manager shutdown complete");
    }
}