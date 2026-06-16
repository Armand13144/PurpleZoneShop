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

import java.util.ArrayList;
import java.util.List;

public class CategoryShopGUI {

    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7 (slots 10-16, 19-25, 28-34, 37-43)
    private static final int[] ITEM_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    public static String getTitlePrefix() { return ChatColor.DARK_PURPLE + "Shop: "; }

    public static void open(Player player, ShopManager.ShopCategory category) {
        open(player, category, 0);
    }

    public static void open(Player player, ShopManager.ShopCategory category, int page) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        int totalPages = Math.max(1, (int) Math.ceil(category.items.size() / (double) ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String title = getTitlePrefix() + ChatColor.translateAlternateColorCodes('&', category.name)
                + ChatColor.DARK_GRAY + " [p" + page + "]";
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Border (top row, bottom row, side columns)
        ItemStack glass = makeItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);
        for (int row = 1; row < 5; row++) {
            inv.setItem(row * 9, glass);
            inv.setItem(row * 9 + 8, glass);
        }

        // Back button
        inv.setItem(45, makeItem(Material.ARROW, ChatColor.RED + "← Back"));

        // Balance
        inv.setItem(49, makeItem(Material.SUNFLOWER,
                ChatColor.GOLD + "Balance",
                ChatColor.YELLOW + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player))));

        // Sell hint
        inv.setItem(53, makeItem(Material.CHEST,
                ChatColor.YELLOW + "Want to sell?",
                ChatColor.GRAY + "Use /sell to open",
                ChatColor.GRAY + "the sell chest!"));

        // Pagination buttons
        if (totalPages > 1) {
            if (page > 0) {
                inv.setItem(46, makeItem(Material.SPECTRAL_ARROW,
                        ChatColor.YELLOW + "← Previous Page",
                        ChatColor.GRAY + "Page " + page + " / " + totalPages));
            }
            if (page < totalPages - 1) {
                inv.setItem(52, makeItem(Material.SPECTRAL_ARROW,
                        ChatColor.YELLOW + "Next Page →",
                        ChatColor.GRAY + "Page " + (page + 2) + " / " + totalPages));
            }
            inv.setItem(48, makeItem(Material.BOOK,
                    ChatColor.AQUA + "Page " + (page + 1) + " / " + totalPages,
                    ChatColor.GRAY + category.items.size() + " items total"));
        }

        // Items for this page
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, category.items.size());

        for (int i = start; i < end; i++) {
            ShopManager.ShopItem item = category.items.get(i);
            int slotIndex = i - start;
            if (slotIndex >= ITEM_SLOTS.length) break;
            int slot = ITEM_SLOTS[slotIndex];

            List<String> lore = new ArrayList<>();
            lore.add("");
            if (item.canBuy())
                lore.add(ChatColor.GREEN + "Buy: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.buyPrice) + ChatColor.GRAY + " each");
            if (item.canSell())
                lore.add(ChatColor.RED + "Sell: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.sellPrice) + ChatColor.GRAY + " each");
            lore.add("");
            if (item.canBuy())
                lore.add(ChatColor.YELLOW + "Click " + ChatColor.GRAY + "to choose quantity & buy");
            if (item.canSell())
                lore.add(ChatColor.YELLOW + "/sell " + ChatColor.GRAY + "to sell this item");

            ItemStack display = new ItemStack(item.material);
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', item.name));
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(slot, display);
        }

        player.openInventory(inv);
    }

    public static int getPageFromTitle(String title) {
        int idx = title.lastIndexOf("[p");
        if (idx == -1) return 0;
        try {
            String sub = title.substring(idx + 2, title.indexOf(']', idx));
            return Integer.parseInt(sub);
        } catch (Exception e) {
            return 0;
        }
    }

    private static ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore.length > 0) {
            List<String> l = new ArrayList<>();
            for (String s : lore) l.add(s);
            meta.setLore(l);
        }
        item.setItemMeta(meta);
        return item;
    }
}
