package lol.siwoo.faramcpracticecore.aa.terms;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;

public class Agree implements CommandExecutor {

    private final JavaPlugin plugin;

    public Agree(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cant agree to this legal agreement as a console.");
            return true;
        }

        agreeTerms((Player) sender);
        return true;
    }

    public void agreeTerms(Player p) {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        String status = saveAgreementData(p, date, time);
        if (status.equals("failed")) {
            p.sendMessage(ChatColor.RED + "An error occurred while saving your agreement data. Please try again later.");
            return;
        } else {
            p.sendMessage(ChatColor.GREEN + "You have agreed to the terms and conditions to play on this server." +
                ChatColor.GRAY + " (Agreed time: " + date + ", " + time + ")");
        }
    }

    private String saveAgreementData(Player player, LocalDate date, LocalTime time) {
        try {
            File playerDir = new File(plugin.getDataFolder(), "player");
            if (!playerDir.exists()) {
                playerDir.mkdirs();
            }

            File playerFile = new File(playerDir, player.getUniqueId().toString() + ".txt");

            try (FileWriter writer = new FileWriter(playerFile)) {
                writer.write("Player: " + player.getName() + "\n");
                writer.write("UUID: " + player.getUniqueId().toString() + "\n");
                writer.write("Agreement Date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "\n");
                writer.write("Agreement Time: " + time.format(DateTimeFormatter.ISO_LOCAL_TIME) + "\n");
                writer.write("Agreed: true\n");
                writer.write("Timestamp: " + System.currentTimeMillis() + "\n");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save agreement data for player " + player.getName() + ": " + e.getMessage());
            return "failed";
        }
        return "success";
    }
}
