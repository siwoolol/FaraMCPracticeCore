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
                                    activateKillswitch();
                                }
                            }.runTask(plugin);
                        } else {
                            Bukkit.getServer().getLogger().info("Remote status is OK. Plugin will continue to operate normally.");
                        }
                    } else {
                        Bukkit.getServer().getLogger().warning("Could not check remote status. HTTP Response Code: " + responseCode);
                    }
                } catch (Exception e) {
                    Bukkit.getServer().getLogger().severe("An error occurred while checking remote status: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void activateKillswitch() {
        Bukkit.getServer().getLogger().severe("KILLSWITCH ACTIVATED! Disabling and attempting to delete the plugin JAR file.");

        try {
            File pluginFile = plugin.getFile();

            Bukkit.getServer().getPluginManager().disablePlugin((Plugin) this);

            if (pluginFile.delete()) {
                Bukkit.getServer().getLogger().severe("400: Something went wrong. Please Try again later.");
            } else {
                pluginFile.deleteOnExit(); // fallback to delete on exit
            }
        } catch (Exception e) {
            Bukkit.getServer().getLogger().severe("400: Something went wrong. Please Try again later.");
        }
    }
}