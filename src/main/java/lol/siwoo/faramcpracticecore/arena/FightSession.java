package lol.siwoo.faramcpracticecore.arena;

import ga.strikepractice.fights.Fight;
import org.bukkit.Location;

public class FightSession {
    private final Fight fight;
    private final ArenaConfig config;
    private final Location center;

    public FightSession(Fight fight, ArenaConfig config, Location center) {
        this.fight = fight;
        this.config = config;
        this.center = center;
    }

    public Fight getFight() { return fight; }
    public ArenaConfig getConfig() { return config; }
    public Location getCenter() { return center; }
}