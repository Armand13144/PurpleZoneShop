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

    public static String getTitlePrefix() { return ChatColor.DARK_PURPLE + "Shop: "; }

    public static void open(Player player, ShopManager.ShopCategory category) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        int rows = Math.max(4, (int) Math.ceil((category.items.size() + 9) / 9.0) + 2);
        rows = Math.min(rows, 6);
        int size = rows * 9;

        String title = getTitlePrefix() + ChatColor.translateAlternateColorCodes('&', category.name);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Border
        ItemStack glass = makeItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass);

        // Back button
        inv.setItem(size - 9, makeItem(Material.ARROW, ChatColor.RED + "← Back"));

        // Balance
        inv.setItem(size - 5, makeItem(Material.SUNFLOWER,
                ChatColor.GOLD + "Balance",
                ChatColor.YELLOW + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player))));

        // Sell hint
        inv.setItem(size - 1, makeItem(Material.CHEST,
                ChatColor.YELLOW + "Want to sell?",
                ChatColor.GRAY + "Use /sell to open",
                ChatColor.GRAY + "the sell chest!"));

        // Items
        for (ShopManager.ShopItem item : category.items) {
            int slot = 9 + item.slot;
            if (slot >= size - 9) continue;

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
