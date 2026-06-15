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

public class BuyGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_GREEN + "Buy: ";

    public static void open(Player player, ShopManager.ShopCategory category, ShopManager.ShopItem item) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_PREFIX + item.id + ":" + category.key);

        // Fill border with green glass
        ItemStack border = make(Material.GREEN_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Item preview in center-left
        ItemStack preview = new ItemStack(item.material);
        ItemMeta pm = preview.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(ChatColor.translateAlternateColorCodes('&', item.name));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Buy price: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.buyPrice) + ChatColor.GRAY + " each");
            lore.add(ChatColor.RED + "Sell price: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.sellPrice) + ChatColor.GRAY + " each");
            lore.add("");
            lore.add(ChatColor.GOLD + "Balance: " + ChatColor.WHITE + plugin.getBalanceManager().format(plugin.getBalanceManager().getBalance(player)));
            pm.setLore(lore);
            preview.setItemMeta(pm);
        }
        inv.setItem(13, preview);

        // Quantity buttons
        inv.setItem(1,  quantityBtn(Material.WHITE_WOOL,   "&fx1",  "1",  item, plugin, player));
        inv.setItem(2,  quantityBtn(Material.YELLOW_WOOL,  "&ex8",  "8",  item, plugin, player));
        inv.setItem(3,  quantityBtn(Material.ORANGE_WOOL,  "&6x16", "16", item, plugin, player));
        inv.setItem(4,  quantityBtn(Material.LIME_WOOL,    "&ax32", "32", item, plugin, player));
        inv.setItem(5,  quantityBtn(Material.GREEN_WOOL,   "&2x64", "64", item, plugin, player));

        // Custom amount button
        inv.setItem(7, makeCustom(item, plugin, player));

        // Back button
        inv.setItem(18, make(Material.ARROW, ChatColor.RED + "← Back to Shop"));

        player.openInventory(inv);
    }

    private static ItemStack quantityBtn(Material mat, String label, String qty,
                                          ShopManager.ShopItem item, ShopPlugin plugin, Player player) {
        int amount = Integer.parseInt(qty);
        double cost = item.buyPrice * amount;
        boolean canAfford = plugin.getBalanceManager().getBalance(player) >= cost;

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Total cost: " + (canAfford ? ChatColor.GREEN : ChatColor.RED) + plugin.getBalanceManager().format(cost));
        lore.add(canAfford ? ChatColor.GREEN + "Click to buy!" : ChatColor.RED + "Not enough money!");

        ItemStack btn = new ItemStack(mat, amount > 64 ? 1 : amount);
        ItemMeta meta = btn.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', label) + ChatColor.GRAY + " (" + qty + "x)");
            meta.setLore(lore);
            btn.setItemMeta(meta);
        }
        return btn;
    }

    private static ItemStack makeCustom(ShopManager.ShopItem item, ShopPlugin plugin, Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Type a number in chat");
        lore.add(ChatColor.GRAY + "to buy a custom amount");
        lore.add("");
        lore.add(ChatColor.GOLD + "Price per item: " + ChatColor.WHITE + plugin.getBalanceManager().format(item.buyPrice));

        ItemStack btn = new ItemStack(Material.PAPER);
        ItemMeta meta = btn.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "✎ Custom Amount");
            meta.setLore(lore);
            btn.setItemMeta(meta);
        }
        return btn;
    }

    public static ItemStack make(Material mat, String name, String... lore) {
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

    public static String getTitlePrefix() { return TITLE_PREFIX; }
}
