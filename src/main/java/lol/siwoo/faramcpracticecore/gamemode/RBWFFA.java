package lol.siwoo.faramcpracticecore.gamemode;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.api.StrikePracticeAPI;
import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class RBWFFA implements Listener {
    private final FaraMCPracticeCore plugin;
    private final StrikePracticeAPI api;

    public RBWFFA(FaraMCPracticeCore plugin) {
        this.plugin = plugin;
        this.api = StrikePractice.getAPI();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (api.isInFight(p) && api.getFight(p).getArena().getName().equals("rbwffa")) {
            if (p.getLocation().getY() < api.getFight(p).getArena().getLoc1().getY() - 60) {
                p.damage(69420.0);
                for (Player player : p.getWorld().getPlayers()) {
                    player.sendMessage(Component.text(p.getName() + " died").color(NamedTextColor.GRAY));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        Player v = (Player) e.getDamager();

        if (api.isInFight(p) && api.getFight(p).getArena().getName().equals("rbwffa")) {
            if (p.getY() > api.getFight(p).getArena().getCenter().getY() - 10) {
                e.setCancelled(true);
            } else if (p.getHealth() - e.getFinalDamage() <= 1f) {
                for (Player player : p.getWorld().getPlayers()) {
                    player.sendMessage(Component.text(p.getName() + " was killed by " + v.getName()).color(NamedTextColor.GRAY));
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack block = p.getItemInHand();
        if (api.getFight(p).getArena().getName().equals("rbwffa")) {
            if (block == null) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    block.add(1);
                }
            }.runTaskLater(plugin, 100L);
        }
    }


    @EventHandler
    public void onBlockDestroy(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block placedBlock = e.getBlock();

        if (api.getFight(p).getArena().getName().equals("rbwffa")) {
            e.setCancelled(true);
            placedBlock.setType(Material.AIR);
            p.give((ItemStack) placedBlock);
        }
    }
}
