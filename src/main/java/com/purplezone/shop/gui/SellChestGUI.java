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

public class SellChestGUI {

    public static final String TITLE = ChatColor.DARK_RED + "✦ Sell Items ✦ " + ChatColor.GRAY + "(Close to sell)";

    public static void open(Player player) {
        // 4 rows: 3 rows for items + 1 row info bar at bottom
        Inventory inv = Bukkit.createInventory(null, 36, TITLE);

        // Bottom row: info items (slots 27-35)
        ShopPlugin plugin = ShopPlugin.getInstance();
        ItemStack info = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Place items above to sell");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Close the chest when done");
            lore.add(ChatColor.GRAY + "Only sellable items will be sold");
            lore.add("");
            lore.add(ChatColor.GOLD + "Balance: " + ChatColor.WHITE + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player)));
            meta.setLore(lore);
            info.setItemMeta(meta);
        }
        for (int i = 27; i < 36; i++) inv.setItem(i, info);

        player.openInventory(inv);
    }

    public static String getTitle() { return TITLE; }

    /**
     * Called when the player closes the sell chest.
     * Returns the total amount earned, or -1 if nothing was sold.
     */
    public static double processSell(Player player, Inventory inv) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        double totalEarned = 0;
        int totalItems = 0;
        List<String> soldLines = new ArrayList<>();

        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType() == Material.AIR) continue;

            // Find this item in any shop category
            ShopManager.ShopItem shopItem = findSellableItem(stack.getType());
            if (shopItem == null) {
                // Not sellable — return to player
                player.getInventory().addItem(stack.clone());
                continue;
            }

            double earned = shopItem.sellPrice * stack.getAmount();
            totalEarned += earned;
            totalItems += stack.getAmount();
            soldLines.add(ChatColor.GRAY + "  " + stack.getAmount() + "x "
                    + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', shopItem.name)
                    + ChatColor.GRAY + " → " + ChatColor.GREEN + plugin.getBalanceManager().format(earned));
        }

        if (totalItems == 0) return -1;

        plugin.getBalanceManager().deposit(player, totalEarned);

        // Send results as title + chat
        sendSellResults(player, totalEarned, totalItems, soldLines, plugin);

        return totalEarned;
    }

    private static ShopManager.ShopItem findSellableItem(Material mat) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        for (ShopManager.ShopCategory cat : plugin.getShopManager().getCategories().values()) {
            for (ShopManager.ShopItem item : cat.items) {
                if (item.material == mat && item.canSell()) return item;
            }
        }
        return null;
    }

    private static void sendSellResults(Player player, double total, int count,
                                         List<String> lines, ShopPlugin plugin) {
        // Title on screen
        player.sendTitle(
                ChatColor.GREEN + "+" + plugin.getBalanceManager().format(total),
                ChatColor.GRAY + "Sold " + count + " items",
                10, 50, 20
        );

        // Chat breakdown
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "✦ " + ChatColor.BOLD + "Sell Summary");
        player.sendMessage(ChatColor.GRAY + "─────────────────────");
        for (String line : lines) player.sendMessage(line);
        player.sendMessage(ChatColor.GRAY + "─────────────────────");
        player.sendMessage(ChatColor.GREEN + "Total earned: " + ChatColor.WHITE + plugin.getBalanceManager().format(total));
        player.sendMessage(ChatColor.GOLD + "New balance: " + ChatColor.WHITE + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player)));
        player.sendMessage("");
    }
}
