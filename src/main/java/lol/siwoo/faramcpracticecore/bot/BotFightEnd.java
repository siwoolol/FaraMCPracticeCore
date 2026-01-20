package lol.siwoo.faramcpracticecore.bot;

import ga.strikepractice.events.BotDuelEndEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotFightEnd implements Listener {
    private final List<String> defeatMessages = new ArrayList<>();
    private final List<Sound> randomSound = new ArrayList<>();

    public BotFightEnd() {
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
        randomSound.add(Sound.BLOCK_ANVIL_USE);
        randomSound.add(Sound.ENTITY_WITHER_SPAWN);
        randomSound.add(Sound.ENTITY_ENDER_DRAGON_DEATH);
        randomSound.add(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
        randomSound.add(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST);
        randomSound.add(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE);
        randomSound.add(Sound.ENTITY_PLAYER_LEVELUP);
        randomSound.add(Sound.ENTITY_ZOMBIE_DEATH);
        randomSound.add(Sound.ENTITY_SKELETON_DEATH);
    }

    @EventHandler
    public void onFightEnd(BotDuelEndEvent event) {
        Player p = event.getPlayer();

        // Shuffle Random Messages
        List<String> shuffled = new ArrayList<>(defeatMessages);
        Collections.shuffle(shuffled);
        String selectedMessage = shuffled.get(0);

        List<Sound> soundshuffled = new ArrayList<>(randomSound);
        Collections.shuffle(soundshuffled);
        Sound randomSound = soundshuffled.get(0);

        String botname = event.getBot().getName();

        if (!(event.getWinnerType() == BotDuelEndEvent.WinnerType.BOT)) {
            // Winner Prompt
            p.playSound((p.getLocation()), randomSound, 1, 1);
            p.sendTitle(ChatColor.GREEN.toString() + ChatColor.BOLD + "VICTORY", ChatColor.GREEN + p.getName() + ChatColor.WHITE + selectedMessage + ChatColor.GREEN + botname);
        } else {
            // Loser Prompt
            p.playSound((p.getLocation()), randomSound, 1, 1);
            p.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + "DEFEAT", ChatColor.RED + botname + ChatColor.WHITE + selectedMessage + ChatColor.GREEN + p.getName());
        }
    }
}