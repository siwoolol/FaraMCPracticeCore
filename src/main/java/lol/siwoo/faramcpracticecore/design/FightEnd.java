package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.events.DuelEndEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FightEnd implements Listener {
    private final List<String> defeatMessages = new ArrayList<>();
    private final List<Sound> randomSound = new ArrayList<>();

    public FightEnd() {
        randomDefeatMessage();
        randomSound();
    }

    public void randomDefeatMessage() {
        defeatMessages.add(" Has Obliterated ");
        defeatMessages.add(" Has Touched Up Against ");
        defeatMessages.add(" Has Defeated ");
        defeatMessages.add(" Destroyed ");
        defeatMessages.add(" Has Won Against ");
        defeatMessages.add(" Has Annihilated ");
        defeatMessages.add(" Outplayed ");
        defeatMessages.add(" Has Demolished ");
        defeatMessages.add(" Has Crushed ");
        defeatMessages.add(" Deleted ");
        defeatMessages.add(" Erased ");
        defeatMessages.add(" Has Dominated ");
        defeatMessages.add(" Sent ");
        defeatMessages.add(" Vaporized ");
        defeatMessages.add(" Beat ");
        defeatMessages.add(" Pummeled ");
        defeatMessages.add(" Trounced ");
        defeatMessages.add(" Rout ");
        defeatMessages.add(" Drubbed ");
        defeatMessages.add(" Walloped ");
        defeatMessages.add(" Whipped ");
        defeatMessages.add(" Trounced ");
        defeatMessages.add(" Annihilated ");
        defeatMessages.add(" Vanquished ");
    }

    public void randomSound() {
        randomSound.add(Sound.ANVIL_USE);
        randomSound.add(Sound.WITHER_SPAWN);
        randomSound.add(Sound.ENDERDRAGON_DEATH);
        randomSound.add(Sound.FIREWORK_BLAST2);
        randomSound.add(Sound.FIREWORK_LARGE_BLAST);
        randomSound.add(Sound.FIREWORK_TWINKLE);
        randomSound.add(Sound.LEVEL_UP);
        randomSound.add(Sound.SUCCESSFUL_HIT);
        randomSound.add(Sound.ZOMBIE_DEATH);
        randomSound.add(Sound.SKELETON_DEATH);
    }

    @EventHandler
    public void onFightEnd(DuelEndEvent event) {
        Player w = event.getWinner();
        Player l = event.getLoser();

        // Shuffle Random Messages
        List<String> shuffled = new ArrayList<>(defeatMessages);
        Collections.shuffle(shuffled);
        String selectedMessage = shuffled.get(0);

        List<Sound> soundshuffled = new ArrayList<>(randomSound);
        Collections.shuffle(soundshuffled);
        Sound randomSound = soundshuffled.get(0);

        // Winner Prompt
        w.playSound((w.getLocation()), randomSound, 1, 1);
        w.sendTitle(ChatColor.GREEN.toString() + ChatColor.BOLD + "VICTORY", ChatColor.GREEN + w.getName() + ChatColor.WHITE + selectedMessage + ChatColor.GREEN + l.getName());

        // Loser Prompt
        l.playSound((l.getLocation()), randomSound, 1, 1);
        l.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + "DEFEAT", ChatColor.RED + w.getName() + ChatColor.WHITE + selectedMessage + ChatColor.GREEN + l.getName());
    }
}