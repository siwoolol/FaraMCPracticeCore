package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import java.io.File;

public class ArenaConfig {
    private final String name;
    private final String schematicName;
    private final Vector pos1; // StrikePractice Boundary 1
    private final Vector pos2; // StrikePractice Boundary 2
    private final Vector corner1; // Physical Cleanup Corner 1
    private final Vector corner2; // Physical Cleanup Corner 2

    public ArenaConfig(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.name = config.getString("name", file.getName().replace(".yml", ""));
        this.schematicName = config.getString("schematic");
        this.pos1 = config.getVector("pos1");
        this.pos2 = config.getVector("pos2");
        this.corner1 = config.getVector("corner1");
        this.corner2 = config.getVector("corner2");
    }

    public String getName() { return name; }
    public String getSchematicName() { return schematicName; }
    public Vector getPos1() { return pos1; }
    public Vector getPos2() { return pos2; }
    public Vector getCorner1() { return corner1; }
    public Vector getCorner2() { return corner2; }
}