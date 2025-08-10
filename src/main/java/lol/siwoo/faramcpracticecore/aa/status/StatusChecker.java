package lol.siwoo.faramcpracticecore.aa.status;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatusChecker{
    private final String STATUS_CHECK_URL = "https://your-website.com/plugin-status.txt";
    private final JavaPlugin plugin;

    public StatusChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    public void check() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(STATUS_CHECK_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String status = reader.readLine();
                        reader.close();

                        if (status != null && status.trim().equalsIgnoreCase("disable")) {
                            Bukkit.getServer().getLogger().warning("Remote killswitch signal ('disable') received!");

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (plugin instanceof shutDown) {
                                        ((shutDown) plugin).emergencyShutDown();
                                    }
                                }
                            }.runTask(plugin);
                        } else {
                            Bukkit.getServer().getLogger().info("Status is OK. Plugin will continue to operate normally.");
                        }
                    } else {
                        Bukkit.getServer().getLogger().warning("Could not check status: " + responseCode);
                    }
                } catch (Exception e) {
                    Bukkit.getServer().getLogger().severe("An severe error occurred while authenticating: Unknown");
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public interface shutDown {
        void emergencyShutDown();
    }
}