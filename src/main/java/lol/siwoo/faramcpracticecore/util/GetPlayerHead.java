package lol.siwoo.faramcpracticecore.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.UUID;

public class GetPlayerHead {
    public static ItemStack getPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.SKELETON_SKULL, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwner(playerName);
            head.setItemMeta(skullMeta);
        }
        return head;
    }

    public static ItemStack getPlayerHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.SKELETON_SKULL, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
            head.setItemMeta(skullMeta);
        }
        return head;
    }
}
