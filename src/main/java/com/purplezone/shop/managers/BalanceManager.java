package com.purplezone.shop.managers;

import com.purplezone.shop.ShopPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceManager {

    private final ShopPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private final YamlConfiguration data;
    private final double startingBalance;

    public BalanceManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.startingBalance = plugin.getConfig().getDouble("starting-balance", 100.0);
        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    private void loadAll() {
        if (data.getConfigurationSection("balances") == null) return;
        for (String key : data.getConfigurationSection("balances").getKeys(false)) {
            try {
                balances.put(UUID.fromString(key), data.getDouble("balances." + key));
            } catch (Exception ignored) {}
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            data.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public double getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), startingBalance);
    }

    public void setBalance(Player player, double amount) {
        balances.put(player.getUniqueId(), Math.max(0, amount));
    }

    public boolean withdraw(Player player, double amount) {
        double bal = getBalance(player);
        if (bal < amount) return false;
        setBalance(player, bal - amount);
        return true;
    }

    public void deposit(Player player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public String format(double amount) {
        String sym = plugin.getConfig().getString("currency-symbol", "$");
        return sym + String.format("%.2f", amount);
    }
}
