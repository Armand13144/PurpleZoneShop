package com.purplezone.shop;

import com.purplezone.shop.commands.*;
import com.purplezone.shop.listeners.NPCListener;
import com.purplezone.shop.listeners.ShopListener;
import com.purplezone.shop.managers.BalanceManager;
import com.purplezone.shop.managers.ShopManager;
import com.purplezone.shop.placeholders.PurpleZoneExpansion;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;
    private ShopManager shopManager;
    private BalanceManager balanceManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        shopManager = new ShopManager(this);
        balanceManager = new BalanceManager(this);

        // Commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("givemoney").setExecutor(new MoneyAdminCommand(this, "give"));
        getCommand("setmoney").setExecutor(new MoneyAdminCommand(this, "set"));
        getCommand("takemoney").setExecutor(new MoneyAdminCommand(this, "take"));
        getCommand("topmoney").setExecutor(new TopMoneyCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        // Citizens NPC support (optional)
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(this), this);
            getLogger().info("Citizens detected! NPC shop support enabled.");
        }

        // PlaceholderAPI support (optional)
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PurpleZoneExpansion(this).register();
            getLogger().info("PlaceholderAPI detected! Placeholders registered.");
            getLogger().info("  Available: %purplezoneshop_balance%");
            getLogger().info("  Available: %purplezoneshop_balance_raw%");
            getLogger().info("  Available: %purplezoneshop_rank%");
            getLogger().info("  Available: %purplezoneshop_top_name_1% ... _10%");
            getLogger().info("  Available: %purplezoneshop_top_balance_1% ... _10%");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
            getLogger().warning("Download it from: https://www.spigotmc.org/resources/placeholderapi.6245/");
        }

        getLogger().info("PurpleZone Shop v3 loaded!");
        getLogger().info("Commands: /shop /balance /pay /topmoney /givemoney /setmoney /takemoney");
    }

    @Override
    public void onDisable() {
        if (balanceManager != null) balanceManager.saveAll();
        getLogger().info("PurpleZone Shop disabled. Balances saved.");
    }

    public static ShopPlugin getInstance() { return instance; }
    public ShopManager getShopManager() { return shopManager; }
    public BalanceManager getBalanceManager() { return balanceManager; }
}
