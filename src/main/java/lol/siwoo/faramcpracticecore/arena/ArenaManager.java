package lol.siwoo.faramcpracticecore.arena;

import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Location;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArenaManager {
    private final FaraMCPracticeCore plugin;
    private final File folder;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "schemas");
        if (!folder.exists()) folder.mkdirs();
    }

    public List<String> getMaps() {
        List<String> maps = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) maps.add(f.getName().replace(".yml", ""));
        }
        return maps;
    }

    public void prepareArena(String mapName, Location location) {
        // Logic to paste schematic (e.g., using WorldEdit or internal NBT)
        plugin.getLogger().info("Loading dynamic arena: " + mapName);
    }
}