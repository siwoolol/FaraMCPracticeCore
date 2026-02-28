package lol.siwoo.faramcpracticecore.design;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Centralized message styling utility.
 * All player-facing messages should go through these methods
 * for a consistent, modern look.
 */
public final class MessageStyle {

    private MessageStyle() {
    }

    // ── Core Builders ──

    /** Error message: " ✗ text" in red */
    public static Component error(String text) {
        return Component.empty()
                .append(Component.text("  ✗ ", NamedTextColor.RED))
                .append(Component.text(text, NamedTextColor.RED));
    }

    /** Success message: " ✓ text" in green */
    public static Component success(String text) {
        return Component.empty()
                .append(Component.text("  ✓ ", NamedTextColor.GREEN))
                .append(Component.text(text, NamedTextColor.GREEN));
    }

    /** Info message: " ● text" in gray */
    public static Component info(String text) {
        return Component.empty()
                .append(Component.text("  ● ", NamedTextColor.GRAY))
                .append(Component.text(text, NamedTextColor.GRAY));
    }

    /** Warning message: " ⚠ text" in gold */
    public static Component warning(String text) {
        return Component.empty()
                .append(Component.text("  ⚠ ", NamedTextColor.GOLD))
                .append(Component.text(text, NamedTextColor.GOLD));
    }

    // ── Composite Builders ──

    /** Error with a highlighted player name: " ✗ " + name (aqua) + " rest" (red) */
    public static Component errorWithName(String name, String suffix) {
        return Component.empty()
                .append(Component.text("  ✗ ", NamedTextColor.RED))
                .append(Component.text(name, NamedTextColor.AQUA))
                .append(Component.text(" " + suffix, NamedTextColor.RED));
    }

    /**
     * Success with a highlighted value: " ✓ prefix " + value (aqua) + " suffix"
     * (green)
     */
    public static Component successWithHighlight(String prefix, String highlight, String suffix) {
        Component c = Component.empty()
                .append(Component.text("  ✓ " + prefix + " ", NamedTextColor.GREEN))
                .append(Component.text(highlight, NamedTextColor.AQUA));
        if (suffix != null && !suffix.isEmpty()) {
            c = c.append(Component.text(" " + suffix, NamedTextColor.GREEN));
        }
        return c;
    }

    /** Info with a player name highlighted: " " + name (aqua) + " text" (gray) */
    public static Component infoFromPlayer(String name, String text) {
        return Component.empty()
                .append(Component.text("  ", NamedTextColor.GRAY))
                .append(Component.text(name, NamedTextColor.AQUA))
                .append(Component.text(" " + text, NamedTextColor.GRAY));
    }

    /** Error with highlight: " ✗ prefix " + highlight (aqua) + " suffix" (red) */
    public static Component errorWithHighlight(String prefix, String highlight, String suffix) {
        Component c = Component.empty()
                .append(Component.text("  ✗ " + prefix + " ", NamedTextColor.RED))
                .append(Component.text(highlight, NamedTextColor.AQUA));
        if (suffix != null && !suffix.isEmpty()) {
            c = c.append(Component.text(" " + suffix, NamedTextColor.RED));
        }
        return c;
    }

    // ── Shortcut Send Methods ──

    public static void sendError(Player p, String text) {
        p.sendMessage(error(text));
    }

    public static void sendSuccess(Player p, String text) {
        p.sendMessage(success(text));
    }

    public static void sendInfo(Player p, String text) {
        p.sendMessage(info(text));
    }

    public static void sendWarning(Player p, String text) {
        p.sendMessage(warning(text));
    }
}
