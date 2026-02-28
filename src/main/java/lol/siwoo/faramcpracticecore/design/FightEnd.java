package lol.siwoo.faramcpracticecore.design;

import ga.strikepractice.events.DuelEndEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
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
        defeatMessages.add(" obliterated ");
        defeatMessages.add(" defeated ");
        defeatMessages.add(" destroyed ");
        defeatMessages.add(" outplayed ");
        defeatMessages.add(" demolished ");
        defeatMessages.add(" crushed ");
        defeatMessages.add(" deleted ");
        defeatMessages.add(" erased ");
        defeatMessages.add(" dominated ");
        defeatMessages.add(" vaporized ");
        defeatMessages.add(" beat ");
        defeatMessages.add(" pummeled ");
        defeatMessages.add(" trounced ");
        defeatMessages.add(" walloped ");
        defeatMessages.add(" vanquished ");
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

        // Modern title timing
        Title.Times times = Title.Times.times(
                Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(400));

        // Build subtitle: "winner verb loser"
        Component victorySubtitle = Component.empty()
                .append(Component.text(w.getName(), NamedTextColor.AQUA))
                .append(Component.text(selectedMessage, NamedTextColor.GRAY))
                .append(Component.text(l.getName(), NamedTextColor.AQUA));

        // Winner Title
        w.playSound(w.getLocation(), randomSound, 1, 1);
        w.showTitle(Title.title(
                Component.text("VICTORY", NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                victorySubtitle,
                times));

        // Loser Title
        l.playSound(l.getLocation(), randomSound, 1, 1);
        l.showTitle(Title.title(
                Component.text("DEFEAT", NamedTextColor.RED).decorate(TextDecoration.BOLD),
                victorySubtitle,
                times));
    }
}