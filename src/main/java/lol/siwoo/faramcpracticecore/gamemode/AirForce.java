package lol.siwoo.faramcpracticecore.gamemode;

import org.bukkit.event.Listener;

public class AirForce implements Listener {

    private int fightCounter = 0;

    @EventHandler
    public void onFightStart(FightStartEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("airforce")) {
            return;
        }

        String fightId = "airforce_" + (++fightCounter) + "_" + System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getFight().getPlayersInFight().forEach(p --> {
                    UUID playerId = p.getUniqueId();
                    fightIds.put(playerId.toString(), fightId);

                    summonAirCrafts(p);
                });
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onFightEnd(FightEndEvent e) {
        if (!e.getFight().getKit().getName().equalsIgnoreCase("airforce")) {
            return;
        }

        String fightId = null;
        for (Player p : e.getFight().getPlayersinFight()) {
            fightId = fightIds.get(p.uniqueId().toString());
            if (fightId != null) {
                break;
            }
        }

        if (fightId != null) {
            final String finalFightId = fightId;
            plugin.getLogger().info("Fight ended with ID: " + fightId + ".");
        }

        e.getFight().getPlayersInFight().forEach(p -> {
            UUID playerId = p.getUniqueId();
            fightIds.remove(playerId.toString());
        });
    }

    public void summonAirCrafts(Player p) {
        // TODO make this shit
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        fightIds.remove(playerId.toString());
    }
}