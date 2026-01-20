package lol.siwoo.faramcpracticecore.aa.status;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatusChecker{
    private final String STATUS_CHECK_URL = "https://api.siwoo.lol/faramcpracticecore/status.txt";
    private final JavaPlugin plugin;

    public StatusChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    public void check() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Starting Authentication check...");

                try {
                    URL url = new URL(STATUS_CHECK_URL);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty("User-Agent", "FaraMCPracticeCore-StatusChecker/1.0");

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String status = reader.readLine();
                        reader.close();

                        if (status != null && status.trim().equalsIgnoreCase("disable")) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (plugin instanceof shutDown) {
                                        ((shutDown) plugin).emergencyShutDown();
                                    }
                                }
                            }.runTask(plugin);
                        } else {
                            Bukkit.getServer().getLogger().info("Authentication status: Operational");
                        }
                    } else {
                        Bukkit.getServer().getLogger().severe("Could not Authenticate. Please Try again later.");
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        Bukkit.getServer().shutdown();
                    }
                } catch (Exception e) {
                    Bukkit.getServer().getLogger().severe("An severe error occurred while authenticating:");
                    Bukkit.getPluginManager().disablePlugin(plugin);
                    Bukkit.getServer().shutdown();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public interface shutDown {
        void emergencyShutDown();
    }
}