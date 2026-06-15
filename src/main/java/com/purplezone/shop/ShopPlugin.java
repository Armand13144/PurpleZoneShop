package com.purplezone.shop;

import com.purplezone.shop.commands.ShopCommand;
import com.purplezone.shop.commands.BalanceCommand;
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
        getCommand("shopbalance").setExecutor(new BalanceCommand(this));

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(this), this);
            getLogger().info("Citizens detected! NPC shop support enabled.");
        }

        getLogger().info("PurpleZone Shop loaded! Type /shop to open.");
    }

    @Override
    public void onDisable() {
        if (balanceManager != null) balanceManager.saveAll();
        getLogger().info("PurpleZone Shop disabled.");
    }

    public static ShopPlugin getInstance() { return instance; }
    public ShopManager getShopManager() { return shopManager; }
    public BalanceManager getBalanceManager() { return balanceManager; }
}
