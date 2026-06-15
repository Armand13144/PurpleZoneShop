package com.purplezone.shop.listeners;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.gui.CategoryShopGUI;
import com.purplezone.shop.gui.LeaderboardGUI;
import com.purplezone.shop.gui.MainShopGUI;
import com.purplezone.shop.managers.BalanceManager;
import com.purplezone.shop.managers.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals(MainShopGUI.getTitle())) {
            e.setCancelled(true);
            handleMainMenu(player, e.getCurrentItem());
            return;
        }

        if (title.startsWith(CategoryShopGUI.getTitlePrefix())) {
            e.setCancelled(true);
            handleCategoryMenu(player, e.getCurrentItem(), e.isRightClick(), e.isShiftClick(), title);
            return;
        }

        if (title.equals(LeaderboardGUI.getTitle())) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                MainShopGUI.open(player);
            }
        }
    }

    private void handleMainMenu(Player player, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        // Leaderboard button
        if (clicked.getType() == Material.GOLD_INGOT) {
            LeaderboardGUI.open(player);
            return;
        }

        if (clicked.getItemMeta() == null) return;
        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        for (ShopManager.ShopCategory cat : plugin.getShopManager().getCategories().values()) {
            String catName = ChatColor.stripColor(
                    ChatColor.translateAlternateColorCodes('&', cat.name));
            if (catName.equals(displayName)) {
                CategoryShopGUI.open(player, cat);
                return;
            }
        }
    }

    private void handleCategoryMenu(Player player, ItemStack clicked, boolean rightClick,
                                    boolean shift, String title) {
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        if (clicked.getType() == Material.ARROW) { MainShopGUI.open(player); return; }
        if (clicked.getType() == Material.SUNFLOWER) return;

        String catTitle = ChatColor.stripColor(title.replace(
                ChatColor.stripColor(CategoryShopGUI.getTitlePrefix()), ""));

        ShopManager.ShopCategory category = null;
        for (ShopManager.ShopCategory cat : plugin.getShopManager().getCategories().values()) {
            String catName = ChatColor.stripColor(
                    ChatColor.translateAlternateColorCodes('&', cat.name));
            if (catName.equals(catTitle)) { category = cat; break; }
        }
        if (category == null) return;

        ShopManager.ShopItem shopItem = null;
        for (ShopManager.ShopItem item : category.items) {
            if (item.material == clicked.getType()) { shopItem = item; break; }
        }
        if (shopItem == null) return;

        BalanceManager bm = plugin.getBalanceManager();

        if (!rightClick) {
            if (!shopItem.canBuy()) { player.sendMessage(ChatColor.RED + "This item cannot be bought."); return; }
            int amount = shift ? 64 : 1;
            double cost = shopItem.buyPrice * amount;
            if (!bm.withdraw(player, cost)) {
                player.sendMessage(ChatColor.RED + "Not enough money! Need " + bm.format(cost)
                        + ChatColor.RED + ", you have " + bm.format(bm.getBalance(player)));
                return;
            }
            player.getInventory().addItem(new ItemStack(shopItem.material, amount));
            player.sendMessage(ChatColor.GREEN + "Bought " + amount + "x "
                    + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', shopItem.name)
                    + ChatColor.GREEN + " for " + bm.format(cost)
                    + ChatColor.GREEN + " | Balance: " + bm.format(bm.getBalance(player)));
        } else {
            if (!shopItem.canSell()) { player.sendMessage(ChatColor.RED + "This item cannot be sold."); return; }
            int amount = shift ? countItems(player, shopItem.material) : 1;
            if (amount <= 0 || !removeItems(player, shopItem.material, amount)) {
                player.sendMessage(ChatColor.RED + "You don't have any " + ChatColor.translateAlternateColorCodes('&', shopItem.name) + ChatColor.RED + " to sell!");
                return;
            }
            double earned = shopItem.sellPrice * amount;
            bm.deposit(player, earned);
            player.sendMessage(ChatColor.YELLOW + "Sold " + amount + "x "
                    + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', shopItem.name)
                    + ChatColor.YELLOW + " for " + bm.format(earned)
                    + ChatColor.YELLOW + " | Balance: " + bm.format(bm.getBalance(player)));
        }

        final ShopManager.ShopCategory finalCat = category;
        plugin.getServer().getScheduler().runTask(plugin, () -> CategoryShopGUI.open(player, finalCat));
    }

    private int countItems(Player player, Material mat) {
        int count = 0;
        for (ItemStack s : player.getInventory().getContents())
            if (s != null && s.getType() == mat) count += s.getAmount();
        return count;
    }

    private boolean removeItems(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack s = contents[i];
            if (s != null && s.getType() == mat) {
                int remove = Math.min(s.getAmount(), remaining);
                s.setAmount(s.getAmount() - remove);
                remaining -= remove;
            }
        }
        player.getInventory().setContents(contents);
        return remaining == 0;
    }
}
