package com.purplezone.shop.gui;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.managers.BalanceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Money Leaderboard ✦";

    public static void open(Player player) {
        ShopPlugin plugin = ShopPlugin.getInstance();
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Fill border
        ItemStack glass = makeItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);
        for (int i = 0; i < 6; i++) { inv.setItem(i * 9, glass); inv.setItem(i * 9 + 8, glass); }

        // Title item
        inv.setItem(4, makeItem(Material.GOLD_INGOT,
                ChatColor.GOLD + "✦ Money Leaderboard ✦",
                ChatColor.YELLOW + "Top richest players"));

        // Your balance
        double myBal = plugin.getBalanceManager().getBalance(player);
        inv.setItem(49, makeItem(Material.SUNFLOWER,
                ChatColor.GOLD + "Your Balance",
                ChatColor.YELLOW + plugin.getBalanceManager().format(myBal)));

        // Back button
        inv.setItem(45, makeItem(Material.ARROW,
                ChatColor.RED + "← Back to Shop"));

        // Top 10 players
        List<Map.Entry<UUID, Double>> top = plugin.getBalanceManager().getTopBalances(10);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        String[] medals = {"§6#1", "§7#2", "§c#3", "§f#4", "§f#5", "§f#6", "§f#7", "§f#8", "§f#9", "§f#10"};

        for (int i = 0; i < top.size(); i++) {
            Map.Entry<UUID, Double> entry = top.get(i);
            String name = plugin.getBalanceManager().getPlayerName(entry.getKey());
            double bal = entry.getValue();

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getKey()));
                meta.setDisplayName(medals[i] + " " + ChatColor.WHITE + name);
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Balance: " + ChatColor.WHITE + plugin.getBalanceManager().format(bal));
                lore.add(ChatColor.GRAY + "Rank: " + (i + 1));
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            if (i < slots.length) inv.setItem(slots[i], skull);
        }

        // Fill empty leaderboard slots with gray glass
        for (int slot : slots) {
            if (inv.getItem(slot) == null)
                inv.setItem(slot, makeItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + "Empty"));
        }

        player.openInventory(inv);
    }

    private static ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String l : lore) loreList.add(l);
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static String getTitle() { return TITLE; }
}
