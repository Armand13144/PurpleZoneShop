package com.purplezone.shop.managers;

import com.purplezone.shop.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ShopManager {

    private final ShopPlugin plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        loadCategories();
    }

    private void loadCategories() {
        categories.clear();
        ConfigurationSection cats = plugin.getConfig().getConfigurationSection("categories");
        if (cats == null) return;

        for (String key : cats.getKeys(false)) {
            ConfigurationSection cat = cats.getConfigurationSection(key);
            if (cat == null) continue;

            String name = cat.getString("name", key);
            Material icon = parseMaterial(cat.getString("material", "CHEST"));
            int slot = cat.getInt("slot", 10);

            List<ShopItem> items = new ArrayList<>();
            ConfigurationSection itemsSec = cat.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String iKey : itemsSec.getKeys(false)) {
                    ConfigurationSection iSec = itemsSec.getConfigurationSection(iKey);
                    if (iSec == null) continue;
                    String iName = iSec.getString("name", iKey);
                    Material mat = parseMaterial(iSec.getString("material", "STONE"));
                    double buy = iSec.getDouble("buy", -1);
                    double sell = iSec.getDouble("sell", -1);
                    int iSlot = iSec.getInt("slot", items.size());
                    items.add(new ShopItem(iKey, iName, mat, buy, sell, iSlot));
                }
            }
            categories.put(key, new ShopCategory(key, name, icon, slot, items));
        }
        plugin.getLogger().info("Loaded " + categories.size() + " shop categories.");
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return Material.STONE;
        }
    }

    public Map<String, ShopCategory> getCategories() { return categories; }

    public ShopCategory getCategory(String key) { return categories.get(key); }

    public String getCurrencySymbol() {
        return plugin.getConfig().getString("currency-symbol", "$");
    }

    public void reload() {
        plugin.reloadConfig();
        loadCategories();
    }

    // ── Inner classes ──────────────────────────────────────────────────

    public static class ShopCategory {
        public final String key, name;
        public final Material icon;
        public final int slot;
        public final List<ShopItem> items;

        public ShopCategory(String key, String name, Material icon, int slot, List<ShopItem> items) {
            this.key = key; this.name = name; this.icon = icon;
            this.slot = slot; this.items = items;
        }
    }

    public static class ShopItem {
        public final String id, name;
        public final Material material;
        public final double buyPrice, sellPrice;
        public final int slot;

        public ShopItem(String id, String name, Material material, double buyPrice, double sellPrice, int slot) {
            this.id = id; this.name = name; this.material = material;
            this.buyPrice = buyPrice; this.sellPrice = sellPrice; this.slot = slot;
        }

        public boolean canBuy() { return buyPrice > 0; }
        public boolean canSell() { return sellPrice > 0; }
    }
}
