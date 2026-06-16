package com.purplezone.shop.gui;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.managers.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class MainShopGUI {

    private static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ PurpleZone Shop ✦";

    public static void open(Player player) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        fillBorder(inv);

        // Balance display
        inv.setItem(4, makeItem(Material.SUNFLOWER,
                ChatColor.GOLD + "Your Balance",
                ChatColor.YELLOW + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player))));

        // Leaderboard button
        inv.setItem(22, makeItem(Material.GOLD_INGOT,
                ChatColor.GOLD + "✦ Leaderboard",
                ChatColor.GRAY + "See top richest players"));

        // Category icons
        for (ShopManager.ShopCategory cat : plugin.getShopManager().getCategories().values()) {
            int slot = Math.min(cat.slot, 26);
            ItemStack icon = makeItem(cat.icon,
                    ChatColor.translateAlternateColorCodes('&', cat.name),
                    ChatColor.GRAY + "Click to browse");
            inv.setItem(slot, icon);
        }

        player.openInventory(inv);
    }

    private static void fillBorder(Inventory inv) {
        ItemStack glass = makeItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        int size = inv.getSize();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9, col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8)
                inv.setItem(i, glass);
        }
    }

    public static ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore.length > 0) meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static String getTitle() { return TITLE; }
}
