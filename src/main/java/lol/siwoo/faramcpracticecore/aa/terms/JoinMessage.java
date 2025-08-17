package lol.siwoo.faramcpracticecore.aa.terms;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinMessage {
    public static void sendJoinMessage(Player p) {
        p.sendMessage(
                ChatColor.DARK_GRAY + "Beta Test and Data Analytics Agreement\n" +
                        ChatColor.GRAY + "By accepting this agreement, you consent to provide FaraMC (operating under the online names \"siwoo,\" \"velocated,\" and \"worldy\" at faramc.uk) with access to collect gameplay data during our beta testing period.\n" +
                        "1. Data Collection: You grant FaraMC permission to collect and analyze gameplay analytics and performance data (\"Data\") resulting from your use of the software.\n" +
                        "2. Use of Data: FaraMC will use this Data for the purpose of product improvement, which includes, but is not limited to, enhancing the player experience, identifying and fixing bugs, and training specific data models.\n" +
                        "3. Disclaimer of Warranty & Limitation of Liability: The beta software is provided on an \"as-is\" and \"as-available\" basis. By participating, you understand and agree that FaraMC shall not be liable for any direct, indirect, incidental, or consequential damages arising from your participation in the beta program or from the collection and use of Data as outlined in this agreement. You hereby waive any right to bring a claim or lawsuit against FaraMC for such damages.\n" +
                        "By proceeding, you confirm that you have read, understood, and agree to be bound by all statements in this agreement.");
        p.sendMessage(ChatColor.YELLOW + "Please read the agreement carefully before proceeding." + ChatColor.DARK_GRAY);

        TextComponent agree = new TextComponent(ChatColor.BOLD.toString() + ChatColor.GREEN + "Agree | ");
        agree.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(ChatColor.GRAY + "By Clicking This,\nYou confirm that you have read, understood, and agree\nto be bound by all statements in this agreement.")
        }));
        agree.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/terms_agree"));
        TextComponent disagree = new TextComponent(ChatColor.RED + "Disagree");
        disagree.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(ChatColor.RED + "You have to agree in to continue playing\n" + ChatColor.RED + "the beta version of the server.")
        }));
        disagree.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/terms_disagree"));

        p.spigot().sendMessage(agree, disagree);
        p.sendMessage("\n");
    }
}
