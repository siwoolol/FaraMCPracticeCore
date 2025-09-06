package lol.siwoo.faramcpracticecore.design;

import lol.siwoo.faramcpracticecore.gui.QueueGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueGUICommand implements CommandExecutor {
    private final QueueGUI queueGUI;

    public QueueGUICommand() {
        this.queueGUI = new QueueGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(queueGUI.createQueueGUI(player));

        return true;
    }
}