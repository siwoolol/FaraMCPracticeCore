package lol.siwoo.faramcpracticecore.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class Kit implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        Bukkit.dispatchCommand(sender, "kiteditor " + Arrays.toString(args));
        return true;
    }
}
