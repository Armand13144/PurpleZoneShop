package com.purplezone.shop.listeners;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.gui.*;
import com.purplezone.shop.managers.BalanceManager;
import com.purplezone.shop.managers.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;

    // Players waiting to type a custom buy amount in chat
    // UUID -> "categoryKey:itemId"
    private final Map<UUID, String> awaitingCustomAmount = new HashMap<>();

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Inventory Click ──────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        // Main shop menu
        if (title.equals(MainShopGUI.getTitle())) {
            e.setCancelled(true);
            handleMainMenu(player, e.getCurrentItem());
            return;
        }

        // Category menu — now opens BuyGUI instead of direct buy, supports pagination
        if (title.startsWith(CategoryShopGUI.getTitlePrefix())) {
            e.setCancelled(true);
            handleCategoryMenu(player, e.getCurrentItem(), title);
            return;
        }

        // Buy quantity GUI
        if (title.startsWith(BuyGUI.getTitlePrefix())) {
            e.setCancelled(true);
            handleBuyGUI(player, e.getCurrentItem(), title);
            return;
        }

        // Leaderboard
        if (title.equals(LeaderboardGUI.getTitle())) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                MainShopGUI.open(player);
            }
            return;
        }

        // Sell chest — allow placing/taking items freely (don't cancel)
        if (title.equals(SellChestGUI.getTitle())) {
            // Block bottom row (info bar slots 27-35)
            if (e.getRawSlot() >= 27 && e.getRawSlot() < 36) {
                e.setCancelled(true);
            }
        }
    }

    // ── Inventory Close (Sell chest) ─────────────────────────────────

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        String title = e.getView().getTitle();

        if (!title.equals(SellChestGUI.getTitle())) return;

        Inventory inv = e.getInventory();
        // Run one tick later so inventory is fully closed
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            double earned = SellChestGUI.processSell(player, inv);
            if (earned <= 0) {
                player.sendMessage(ChatColor.RED + "No sellable items found!");
            }
        }, 1L);
    }

    // ── Chat listener for custom buy amount ──────────────────────────

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!awaitingCustomAmount.containsKey(player.getUniqueId())) return;

        e.setCancelled(true);
        String data = awaitingCustomAmount.remove(player.getUniqueId());

        String[] parts = data.split(":");
        if (parts.length < 2) return;
        String catKey = parts[0];
        String itemId = parts[1];

        int amount;
        try {
            amount = Integer.parseInt(e.getMessage().trim());
            if (amount <= 0 || amount > 2304) {
                player.sendMessage(ChatColor.RED + "Amount must be between 1 and 2304.");
                return;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Invalid number. Purchase cancelled.");
            return;
        }

        ShopManager.ShopCategory cat = plugin.getShopManager().getCategory(catKey);
        if (cat == null) return;
        ShopManager.ShopItem shopItem = cat.items.stream()
                .filter(i -> i.id.equals(itemId)).findFirst().orElse(null);
        if (shopItem == null) return;

        plugin.getServer().getScheduler().runTask(plugin, () ->
                executeBuy(player, shopItem, cat, amount));
    }

    // ── Handlers ─────────────────────────────────────────────────────

    private void handleMainMenu(Player player, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        if (clicked.getType() == Material.GOLD_INGOT) {
            LeaderboardGUI.open(player);
            return;
        }
        if (clicked.getItemMeta() == null) return;
        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        for (ShopManager.ShopCategory cat : plugin.getShopManager().getCategories().values()) {
            String catName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', cat.name));
            if (catName.equals(displayName)) {
                CategoryShopGUI.open(player, cat);
                return;
            }
        }
    }

    private void handleCategoryMenu(Player player, ItemStack clicked, String title) {
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        int currentPage = CategoryShopGUI.getPageFromTitle(title);

        // Strip the "[pN]" suffix to get the clean category name
        String stripped = ChatColor.stripColor(title);
        int bracketIdx = stripped.lastIndexOf(" [p");
        String catTitle = bracketIdx > -1 ? stripped.substring(0, bracketIdx) : stripped;
        catTitle = catTitle.replace(ChatColor.stripColor(CategoryShopGUI.getTitlePrefix()), "");

        ShopManager.ShopCategory category = null;
        for (ShopManager.ShopCategory cat : plugin.getShopManager().getCategories().values()) {
            String catName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', cat.name));
            if (catName.equals(catTitle)) { category = cat; break; }
        }
        if (category == null) return;

        if (clicked.getType() == Material.ARROW) { MainShopGUI.open(player); return; }
        if (clicked.getType() == Material.SUNFLOWER || clicked.getType() == Material.BOOK) return;

        // Pagination buttons
        if (clicked.getType() == Material.SPECTRAL_ARROW) {
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (name.contains("Previous")) {
                CategoryShopGUI.open(player, category, currentPage - 1);
            } else if (name.contains("Next")) {
                CategoryShopGUI.open(player, category, currentPage + 1);
            }
            return;
        }

        // Find clicked shop item and open BuyGUI
        for (ShopManager.ShopItem item : category.items) {
            if (item.material == clicked.getType()) {
                if (!item.canBuy()) {
                    player.sendMessage(ChatColor.RED + "This item cannot be bought.");
                    return;
                }
                BuyGUI.open(player, category, item);
                return;
            }
        }
    }

    private void handleBuyGUI(Player player, ItemStack clicked, String title) {
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.GREEN_STAINED_GLASS_PANE) return;

        // Parse category and item from title: "Buy: itemId:catKey"
        String data = ChatColor.stripColor(title).replace(
                ChatColor.stripColor(BuyGUI.getTitlePrefix()), "");
        String[] parts = data.split(":");
        if (parts.length < 2) return;
        String itemId = parts[0];
        String catKey = parts[1];

        ShopManager.ShopCategory cat = plugin.getShopManager().getCategory(catKey);
        if (cat == null) return;
        ShopManager.ShopItem shopItem = cat.items.stream()
                .filter(i -> i.id.equals(itemId)).findFirst().orElse(null);
        if (shopItem == null) return;

        // Back button
        if (clicked.getType() == Material.ARROW) {
            CategoryShopGUI.open(player, cat);
            return;
        }

        // Custom amount (paper)
        if (clicked.getType() == Material.PAPER) {
            player.closeInventory();
            player.sendMessage(ChatColor.AQUA + "Type the amount you want to buy in chat:");
            player.sendMessage(ChatColor.GRAY + "Price per item: " + plugin.getBalanceManager().format(shopItem.buyPrice));
            awaitingCustomAmount.put(player.getUniqueId(), catKey + ":" + itemId);
            return;
        }

        // Quantity wool buttons
        int amount = clicked.getAmount();
        if (amount <= 0) amount = 1;
        executeBuy(player, shopItem, cat, amount);
    }

    private void executeBuy(Player player, ShopManager.ShopItem shopItem,
                             ShopManager.ShopCategory cat, int amount) {
        BalanceManager bm = plugin.getBalanceManager();
        double cost = shopItem.buyPrice * amount;

        if (!bm.withdraw(player, cost)) {
            player.sendMessage(ChatColor.RED + "Not enough money! Need "
                    + bm.format(cost) + ChatColor.RED + ", you have " + bm.format(bm.getBalance(player)));
            BuyGUI.open(player, cat, shopItem);
            return;
        }

        // Give items (handle stacks > 64)
        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            player.getInventory().addItem(new ItemStack(shopItem.material, stackSize));
            remaining -= stackSize;
        }

        // Title on screen
        player.sendTitle(
                ChatColor.GREEN + "Purchased!",
                ChatColor.WHITE + "" + amount + "x " + ChatColor.translateAlternateColorCodes('&', shopItem.name)
                        + ChatColor.GRAY + " for " + bm.format(cost),
                10, 50, 20
        );

        player.sendMessage(ChatColor.GREEN + "✦ Bought " + amount + "x "
                + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', shopItem.name)
                + ChatColor.GREEN + " for " + bm.format(cost)
                + ChatColor.GRAY + " | Balance: " + bm.format(bm.getBalance(player)));

        // Reopen buy GUI with updated balance
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                BuyGUI.open(player, cat, shopItem), 2L);
    }
}
