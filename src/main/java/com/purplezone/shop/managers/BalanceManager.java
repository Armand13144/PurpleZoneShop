package com.purplezone.shop.managers;

import com.purplezone.shop.ShopPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BalanceManager {

    private final ShopPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();
    private final File dataFile;
    private YamlConfiguration data;
    private final double startingBalance;

    public BalanceManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.startingBalance = plugin.getConfig().getDouble("starting-balance", 500.0);
        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    private void loadAll() {
        balances.clear();
        playerNames.clear();
        if (data.getConfigurationSection("balances") == null) return;
        for (String key : data.getConfigurationSection("balances").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                balances.put(uuid, data.getDouble("balances." + key));
                String name = data.getString("names." + key, "Unknown");
                playerNames.put(uuid, name);
            } catch (Exception ignored) {}
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            String key = entry.getKey().toString();
            data.set("balances." + key, entry.getValue());
            String name = playerNames.getOrDefault(entry.getKey(), "Unknown");
            data.set("names." + key, name);
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public double getBalance(Player player) {
        playerNames.put(player.getUniqueId(), player.getName());
        return balances.getOrDefault(player.getUniqueId(), startingBalance);
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, startingBalance);
    }

    public void setBalance(Player player, double amount) {
        playerNames.put(player.getUniqueId(), player.getName());
        balances.put(player.getUniqueId(), Math.max(0, amount));
        saveAll();
    }

    public void setBalance(OfflinePlayer player, double amount) {
        if (player.getName() != null)
            playerNames.put(player.getUniqueId(), player.getName());
        balances.put(player.getUniqueId(), Math.max(0, amount));
        saveAll();
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

    public void deposit(OfflinePlayer player, double amount) {
        double current = balances.getOrDefault(player.getUniqueId(), startingBalance);
        if (player.getName() != null)
            playerNames.put(player.getUniqueId(), player.getName());
        balances.put(player.getUniqueId(), current + amount);
        saveAll();
    }

    public boolean take(OfflinePlayer player, double amount) {
        double current = balances.getOrDefault(player.getUniqueId(), startingBalance);
        if (current < amount) return false;
        if (player.getName() != null)
            playerNames.put(player.getUniqueId(), player.getName());
        balances.put(player.getUniqueId(), current - amount);
        saveAll();
        return true;
    }

    public String format(double amount) {
        String sym = plugin.getConfig().getString("currency-symbol", "$");
        return sym + String.format("%.2f", amount);
    }

    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(balances.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public String getPlayerName(UUID uuid) {
        if (playerNames.containsKey(uuid)) return playerNames.get(uuid);
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : "Unknown";
    }

    public double getStartingBalance() { return startingBalance; }
}
