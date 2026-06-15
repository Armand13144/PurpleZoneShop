package com.purplezone.shop;

import com.purplezone.shop.commands.*;
import com.purplezone.shop.listeners.NPCListener;
import com.purplezone.shop.listeners.ShopListener;
import com.purplezone.shop.managers.BalanceManager;
import com.purplezone.shop.managers.ShopManager;
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
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("givemoney").setExecutor(new MoneyAdminCommand(this, "give"));
        getCommand("setmoney").setExecutor(new MoneyAdminCommand(this, "set"));
        getCommand("takemoney").setExecutor(new MoneyAdminCommand(this, "take"));
        getCommand("topmoney").setExecutor(new TopMoneyCommand(this));

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(this), this);
            getLogger().info("Citizens detected! NPC shop support enabled.");
        }

        getLogger().info("PurpleZone Shop v2 loaded! Commands: /shop /balance /topmoney");
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
