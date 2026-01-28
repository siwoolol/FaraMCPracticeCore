package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArenaConfig {
    private final String name;
    private final String schematicName;
    private final Vector pos1, pos2, corner1, corner2, center;
    private final List<String> kits;

    public ArenaConfig(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.name = config.getString("name", file.getName().replace(".yml", ""));
        this.schematicName = config.getString("schematic", "generated_" + this.name.toLowerCase());
        this.pos1 = config.getVector("pos1", new Vector(10, 5, 0));
        this.pos2 = config.getVector("pos2", new Vector(-10, 5, 0));
        this.corner1 = config.getVector("corner1", new Vector(30, 30, 30));
        this.corner2 = config.getVector("corner2", new Vector(-30, 0, -30));
        this.center = config.getVector("center", new Vector(0, 0, 0));
        this.kits = new ArrayList<>();
        List<String> list = config.getStringList("kits");
        if (list != null) for (String k : list) kits.add(k.toLowerCase());
    }

    public String getName() { return name; }
    public String getSchematicName() { return schematicName; }
    public Vector getPos1() { return pos1.clone(); }
    public Vector getPos2() { return pos2.clone(); }
    public Vector getCorner1() { return corner1.clone(); }
    public Vector getCorner2() { return corner2.clone(); }
    public Vector getCenter() { return center.clone(); }
    public boolean isKitAllowed(String kit) { return kits.isEmpty() || kits.contains(kit.toLowerCase()); }
}