package com.purplezone.shop.commands;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.gui.LeaderboardGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopMoneyCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public TopMoneyCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Console: print text leaderboard
            sender.sendMessage(ChatColor.GOLD + "=== Top Balances ===");
            var top = plugin.getBalanceManager().getTopBalances(10);
            for (int i = 0; i < top.size(); i++) {
                var entry = top.get(i);
                String name = plugin.getBalanceManager().getPlayerName(entry.getKey());
                sender.sendMessage((i + 1) + ". " + name + " - " + plugin.getBalanceManager().format(entry.getValue()));
            }
            return true;
        }
        LeaderboardGUI.open((Player) sender);
        return true;
    }
}
