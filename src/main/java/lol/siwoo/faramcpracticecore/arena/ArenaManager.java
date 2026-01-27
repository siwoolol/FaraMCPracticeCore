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
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Location;
import org.bukkit.Material;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArenaManager {
    private final FaraMCPracticeCore plugin;
    private final Map<String, ArenaConfig> arenas = new HashMap<>();
    private final File folder;

    public ArenaManager(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "arena");
        if (!folder.exists()) folder.mkdirs();
        load();
    }

    public void load() {
        arenas.clear();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                ArenaConfig config = new ArenaConfig(f);
                arenas.put(config.getName().toLowerCase(), config);
            }
        }
    }

    public void paste(ArenaConfig config, Location center) {
        File file = new File(folder, config.getSchematicName());
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) return;

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }
        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
        }
    }

    public void clear(ArenaConfig config, Location center) {
        Location c1 = center.clone().add(config.getCorner1());
        Location c2 = center.clone().add(config.getCorner2());

        int x1 = Math.min(c1.getBlockX(), c2.getBlockX());
        int y1 = Math.min(c1.getBlockY(), c2.getBlockY());
        int z1 = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int x2 = Math.max(c1.getBlockX(), c2.getBlockX());
        int y2 = Math.max(c1.getBlockY(), c2.getBlockY());
        int z2 = Math.max(c1.getBlockZ(), c2.getBlockZ());

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