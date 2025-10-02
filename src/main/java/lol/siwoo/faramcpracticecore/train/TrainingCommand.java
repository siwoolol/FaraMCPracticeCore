package lol.siwoo.faramcpracticecore.train;

import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrainingCommand implements CommandExecutor {

    private final FaraMCPracticeCore plugin;
    private final TrainingManager trainingManager;

    public TrainingCommand(FaraMCPracticeCore plugin, TrainingManager trainingManager) {
        this.plugin = plugin;
        this.trainingManager = trainingManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open training GUI
            player.openInventory(TrainingGUI.createTrainingGUI(player));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "stop":
            case "end":
            case "quit":
                if (trainingManager.isInTraining(player)) {
                    trainingManager.endTraining(player);
                    player.sendMessage("§cTraining session ended.");
                } else {
                    player.sendMessage("§cYou are not in a training session!");
                }
                break;
            case "strafe":
                trainingManager.startTraining(player, TrainingMode.STRAFE);
                break;
            case "aim":
                trainingManager.startTraining(player, TrainingMode.AIM_TRACKER);
                break;
            case "cps":
                trainingManager.startTraining(player, TrainingMode.CPS_TESTER);
                break;
            case "wtap":
                trainingManager.startTraining(player, TrainingMode.W_TAP_TRAINER);
                break;
            default:
                player.sendMessage("§cUsage: /train [strafe|aim|cps|wtap|stop]");
                break;
        }

        return true;
    }
}