package lol.siwoo.faramcpracticecore.aa.aicoach;

import ga.strikepractice.api.StrikePracticeAPI;
import ga.strikepractice.events.DuelEndEvent;
import ga.strikepractice.events.DuelStartEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AICoachListener implements Listener {
    private final AICoach aiCoach;
    private final StrikePracticeAPI api;

    public AICoachListener(AICoach aiCoach, StrikePracticeAPI api) {
        this.aiCoach = aiCoach;
        this.api = api;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(ChatColor.GREEN + "✨ AI Coach is here! Type /ai to experience top-quality AI Experiences!");
    }

    @EventHandler
    public void onDuelStart(DuelStartEvent event) {
        Player player1 = event.getPlayer1();
        Player player2 = event.getPlayer2();

        if (aiCoach.hasAICoach(player1.getUniqueId())) {
            player1.sendMessage(ChatColor.GREEN + "✨ AI Coach activating - monitoring your duel!");
            aiCoach.startMonitoring(player1, player2);
        }

        if (aiCoach.hasAICoach(player2.getUniqueId())) {
            player2.sendMessage(ChatColor.GREEN + "✨ AI Coach activating - monitoring your duel!");
            aiCoach.startMonitoring(player2, player1);
        }
    }

    @EventHandler
    public void onDuelEnd(DuelEndEvent event) {
        Player winner = event.getWinner();
        Player loser = event.getLoser();

        if (aiCoach.hasAICoach(winner.getUniqueId())) {
            winner.sendMessage(ChatColor.GREEN + "✨ AI Coach deactivating - duel ended!");
        }
        if (aiCoach.hasAICoach(loser.getUniqueId())) {
            loser.sendMessage(ChatColor.GREEN + "✨ AI Coach deactivating - duel ended!");
        }

        aiCoach.stopMonitoring(winner.getUniqueId());
        aiCoach.stopMonitoring(loser.getUniqueId());
    }
}
