
package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArenaConfig {
    private final String name;
    private final String schematicName;
    private final Vector pos1;
    private final Vector pos2;
    private final Vector corner1;
    private final Vector corner2;
    private final Vector center;
    private final List<String> kits;

    public ArenaConfig(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        this.name = config.getString("name", file.getName().replace(".yml", ""));
        this.schematicName = config.getString("schematic", "missing.schem");

        // Player spawn positions (relative to center)
        this.pos1 = config.getVector("pos1", new Vector(10, 5, 0));
        this.pos2 = config.getVector("pos2", new Vector(-10, 5, 0));

        // Arena clearing boundaries (relative to center)
        this.corner1 = config.getVector("corner1", new Vector(30, 30, 30));
        this.corner2 = config.getVector("corner2", new Vector(-30, 0, -30));

        // Center offset (usually 0,0,0 but allows for fine-tuning)
        this.center = config.getVector("center", new Vector(0, 0, 0));

        // Kits allowed for this arena (empty list = all kits allowed)
        this.kits = new ArrayList<>();
        List<String> configKits = config.getStringList("kits");
        for (String kit : configKits) {
            this.kits.add(kit.toLowerCase());
        }

        validateConfig();
    }

    private void validateConfig() {
        if (schematicName == null || schematicName.isEmpty()) {
            throw new IllegalArgumentException("Arena " + name + " has no schematic file specified");
        }

        if (pos1 == null) {
            throw new IllegalArgumentException("Arena " + name + " is missing pos1 coordinates");
        }

        if (pos2 == null) {
            throw new IllegalArgumentException("Arena " + name + " is missing pos2 coordinates");
        }

        if (corner1 == null || corner2 == null) {
            throw new IllegalArgumentException("Arena " + name + " is missing corner coordinates for clearing");
        }
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public Vector getPos1() {
        return pos1.clone();
    }

    public Vector getPos2() {
        return pos2.clone();
    }

    public Vector getCorner1() {
        return corner1.clone();
    }

    public Vector getCorner2() {
        return corner2.clone();
    }

    public Vector getCenter() {
        return center.clone();
    }

    public List<String> getKits() {
        return new ArrayList<>(kits);
    }

    // Utility methods
    public boolean isKitAllowed(String kitName) {
        return kits.isEmpty() || kits.contains(kitName.toLowerCase());
    }

    @Override
    public String toString() {
        return "ArenaConfig{" +
                "name='" + name + '\'' +
                ", schematicName='" + schematicName + '\'' +
                ", kitsAllowed=" + (kits.isEmpty() ? "all" : kits.size()) +
                '}';
    }
}