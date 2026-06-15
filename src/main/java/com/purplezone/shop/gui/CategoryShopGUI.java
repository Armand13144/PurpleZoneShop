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

        // Fill top and bottom rows
        ItemStack glass = MainShopGUI.makeItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass);

        // Back button
        inv.setItem(size - 9, MainShopGUI.makeItem(Material.ARROW,
                ChatColor.RED + "← Back", ChatColor.GRAY + "Return to main shop"));

        // Balance
        inv.setItem(size - 5, MainShopGUI.makeItem(Material.SUNFLOWER,
                ChatColor.GOLD + "Balance",
                ChatColor.YELLOW + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player))));

        // Items start at slot 9
        for (ShopManager.ShopItem item : category.items) {
            int slot = 9 + item.slot;
            if (slot >= size - 9) continue;

            List<String> lore = new ArrayList<>();
            lore.add("");
            if (item.canBuy())
                lore.add(ChatColor.GREEN + "Buy: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.buyPrice));
            if (item.canSell())
                lore.add(ChatColor.RED + "Sell: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.sellPrice));
            lore.add("");
            if (item.canBuy()) lore.add(ChatColor.YELLOW + "Left-click " + ChatColor.GRAY + "to buy x1");
            if (item.canBuy()) lore.add(ChatColor.YELLOW + "Shift+Left " + ChatColor.GRAY + "to buy x64");
            if (item.canSell()) lore.add(ChatColor.YELLOW + "Right-click " + ChatColor.GRAY + "to sell x1");
            if (item.canSell()) lore.add(ChatColor.YELLOW + "Shift+Right " + ChatColor.GRAY + "to sell all");

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
}
