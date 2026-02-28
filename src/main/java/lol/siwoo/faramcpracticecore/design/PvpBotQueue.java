package lol.siwoo.faramcpracticecore.design;

import lol.siwoo.faramcpracticecore.FaraMCPracticeCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

public class PvpBotQueue implements CommandExecutor, Listener {

        private static FaraMCPracticeCore plugin;
        public static final String TITLE = ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Bot Duel";

        public PvpBotQueue(FaraMCPracticeCore plugin) {
                PvpBotQueue.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageStyle.error("This command can only be used by a player."));
                        return true;
                }

                Player player = (Player) sender;
                player.openInventory(createBotQueueGUI(player));
                return true;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onBotDuelCommand(PlayerCommandPreprocessEvent e) {
                String msg = e.getMessage();
                String cmd = msg.split(" ")[0].toLowerCase();
                if (cmd.equals("/botduel") || cmd.equals("/strikepractice:botduel")) {
                        e.setCancelled(true);
                        Player p = e.getPlayer();
                        p.openInventory(createBotQueueGUI(p));
                }
        }

        public static Inventory createBotQueueGUI(Player p) {
                Inventory gui = Bukkit.createInventory(null, 45, TITLE);

                fillBackground(gui);

                addQueueItem(gui, 10, Material.SOUL_SAND, "WindFight",
                                ChatColor.AQUA.toString() + ChatColor.BOLD + "WindFight");
                addQueueItem(gui, 11, Material.DIAMOND_SWORD, "Sword",
                                ChatColor.AQUA + "Sword");
                addQueueItem(gui, 12, Material.DIAMOND_AXE, "Axe",
                                ChatColor.AQUA + "Axe");
                addQueueItem(gui, 13, Material.DIAMOND_CHESTPLATE, "Boxing",
                                ChatColor.AQUA + "Boxing");
                addQueueItem(gui, 14, createNodebuffPotion(), "Nodebuff",
                                ChatColor.LIGHT_PURPLE + "Nodebuff");
                addQueueItem(gui, 15, Material.LAVA_BUCKET, "BuildUHC",
                                ChatColor.YELLOW + "BuildUHC");
                addQueueItem(gui, 16, Material.LEAD, "Sumo",
                                ChatColor.GOLD + "Sumo");
                addQueueItem(gui, 19, Material.PUFFERFISH, "Combo",
                                ChatColor.RED + "Combo");
                addQueueItem(gui, 20, Material.GOLDEN_APPLE, "Gapple",
                                ChatColor.GOLD + "Gapple");
                addQueueItem(gui, 21, Material.RED_BED, "BedFight",
                                ChatColor.RED + "BedFight");
                addQueueItem(gui, 22, Material.FIRE_CHARGE, "Fireball Fight",
                                ChatColor.RED + "Fireball Fight");
                addQueueItem(gui, 23, Material.ENDER_EYE, "SkyWars",
                                ChatColor.AQUA + "SkyWars");
                addQueueItem(gui, 24, Material.BOW, "Archer",
                                ChatColor.YELLOW + "Archer");
                addQueueItem(gui, 25, Material.IRON_SWORD, "No Enchant",
                                ChatColor.YELLOW + "No Enchant");
                addQueueItem(gui, 28, Material.IRON_SHOVEL, "Spleef",
                                ChatColor.YELLOW + "Spleef");
                addQueueItem(gui, 29, Material.WOODEN_SWORD, "SG",
                                ChatColor.RED + "SG");
                addQueueItem(gui, 30, Material.MUSHROOM_STEW, "Soup",
                                ChatColor.YELLOW + "Soup");
                addQueueItem(gui, 31, Material.NAME_TAG, "Combo Tag",
                                ChatColor.YELLOW + "Combo Tag");

                return gui;
        }

        private static void fillBackground(Inventory gui) {
                ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta glassMeta = glass.getItemMeta();
                glassMeta.setDisplayName(" ");
                glass.setItemMeta(glassMeta);

                for (int i = 0; i < gui.getSize(); i++) {
                        if (gui.getItem(i) == null)
                                gui.setItem(i, glass);
                }
        }

        private static ItemStack createNodebuffPotion() {
                ItemStack potion = new ItemStack(Material.SPLASH_POTION);
                PotionMeta meta = (PotionMeta) potion.getItemMeta();
                if (meta != null) {
                        meta.setBasePotionType(PotionType.REGENERATION);
                        potion.setItemMeta(meta);
                }
                return potion;
        }

        private static void addQueueItem(Inventory gui, int slot, Material material, String gameMode,
                        String displayName) {
                addQueueItem(gui, slot, new ItemStack(material), gameMode, displayName);
        }

        private static void addQueueItem(Inventory gui, int slot, ItemStack item, String gameMode,
                        String displayName) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(displayName);

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ARMOR_TRIM,
                                ItemFlag.HIDE_DYE, ItemFlag.HIDE_STORED_ENCHANTS);

                meta.setLore(Arrays.asList(
                                "",
                                ChatColor.GREEN + "Click to fight a bot!"));

                item.setItemMeta(meta);
                gui.setItem(slot, item);
        }
}