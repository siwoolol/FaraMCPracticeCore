package lol.siwoo.faramcpracticecore.arena;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class ArenaData {
    private final String name;
    private final Location spawn1;
    private final Location spawn2;
    private final Location center;

    public ArenaData(File file, Location base) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.name = config.getString("name", file.getName().replace(".yml", ""));
        // Vectors are offsets relative to the schematic's paste location
        this.spawn1 = base.clone().add(config.getVector("spawn1"));
        this.spawn2 = base.clone().add(config.getVector("spawn2"));
        this.center = base.clone().add(config.getVector("center"));
    }

    public String getName() { return name; }
    public Location getSpawn1() { return spawn1; }
    public Location getSpawn2() { return spawn2; }
}