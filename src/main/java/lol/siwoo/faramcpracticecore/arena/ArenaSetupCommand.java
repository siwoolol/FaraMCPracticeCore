package lol.siwoo.faramcpracticecore.arena;

import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import lol.siwoo.faramcpracticecore.design.MessageStyle;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ArenaSetupCommand implements CommandExecutor {
    private final FaraMCPracticeCore plugin;

    public ArenaSetupCommand(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;
        if (!player.hasPermission("faramcpracticecore.admin"))
            return true;

        if (args.length < 2) {
            player.sendMessage(MessageStyle.error("Usage: /faraarena <pos1|pos2|corner1|corner2|center> <mapName>"));
            return true;
        }

        String type = args[0].toLowerCase();
        String mapName = args[1].toLowerCase();
        ArenaConfig config = plugin.getArenaManager().getArenas().get(mapName);

        if (config == null) {
            player.sendMessage(MessageStyle.errorWithName(mapName, "not found."));
            return true;
        }

        // Identify the current match session the player is in
        // If not in a match, we assume they are standing at 0, 100, 0 in a void world
        Location currentLoc = player.getLocation();
        Location basePoint = new Location(currentLoc.getWorld(), 0, 100, 0);

        // Find the relative vector from the 0, 100, 0 origin
        Vector relative = currentLoc.toVector().subtract(basePoint.toVector());

        switch (type) {
            case "pos1" -> config.setPos1(relative);
            case "pos2" -> config.setPos2(relative);
            case "corner1" -> config.setCorner1(relative);
            case "corner2" -> config.setCorner2(relative);
            case "center" -> config.setCenter(relative);
            default -> {
                player.sendMessage(MessageStyle.error("Invalid type. Use: pos1, pos2, corner1, corner2, center"));
                return true;
            }
        }

        player.sendMessage(MessageStyle.successWithHighlight("Set " + type + " for", mapName, ""));
        return true;
    }
}