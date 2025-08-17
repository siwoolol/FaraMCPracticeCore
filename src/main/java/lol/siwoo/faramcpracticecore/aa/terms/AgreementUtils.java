package lol.siwoo.faramcpracticecore.aa.terms;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class AgreementUtils {

    private final JavaPlugin plugin;

    public AgreementUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasPlayerAgreed(Player player) {
        try {
            File playerDir = new File(plugin.getDataFolder(), "player");
            File playerFile = new File(playerDir, player.getUniqueId().toString() + ".txt");

            if (!playerFile.exists()) {
                return false;
            }

            List<String> lines = Files.readAllLines(playerFile.toPath());
            for (String line : lines) {
                if (line.trim().equals("Agreed: true")) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to check agreement status for player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
}