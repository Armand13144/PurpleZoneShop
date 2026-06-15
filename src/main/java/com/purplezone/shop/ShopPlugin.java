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

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("givemoney").setExecutor(new MoneyAdminCommand(this, "give"));
        getCommand("setmoney").setExecutor(new MoneyAdminCommand(this, "set"));
        getCommand("takemoney").setExecutor(new MoneyAdminCommand(this, "take"));
        getCommand("topmoney").setExecutor(new TopMoneyCommand(this));

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(this), this);
            getLogger().info("Citizens detected! NPC shop support enabled.");
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PurpleZoneExpansion(this).register();
            getLogger().info("PlaceholderAPI hooked!");
        }

        getLogger().info("PurpleZone Shop v4 loaded!");
    }

    @Override
    public void onDisable() {
        if (balanceManager != null) balanceManager.saveAll();
        getLogger().info("Balances saved.");
    }

    public static ShopPlugin getInstance() { return instance; }
    public ShopManager getShopManager() { return shopManager; }
    public BalanceManager getBalanceManager() { return balanceManager; }
}
