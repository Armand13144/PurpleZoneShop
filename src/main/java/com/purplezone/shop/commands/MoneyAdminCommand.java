package com.purplezone.shop.commands;

import com.purplezone.shop.ShopPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyAdminCommand implements CommandExecutor {

    private final ShopPlugin plugin;
    private final String mode; // "give", "set", "take"

    public MoneyAdminCommand(ShopPlugin plugin, String mode) {
        this.plugin = plugin;
        this.mode = mode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("purplezone.shop.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount. Use a positive number.");
            return true;
        }

        String name = target.getName() != null ? target.getName() : args[0];
        String sym = plugin.getBalanceManager().format(amount);

        switch (mode) {
            case "give":
                plugin.getBalanceManager().deposit(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Gave " + sym + ChatColor.GREEN + " to " + name);
                if (target.isOnline() && target.getPlayer() != null)
                    target.getPlayer().sendMessage(ChatColor.GREEN + "You received " + sym + ChatColor.GREEN + " from an admin!");
                break;
            case "set":
                plugin.getBalanceManager().setBalance(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Set " + name + "'s balance to " + sym);
                if (target.isOnline() && target.getPlayer() != null)
                    target.getPlayer().sendMessage(ChatColor.YELLOW + "Your balance was set to " + sym);
                break;
            case "take":
                boolean success = plugin.getBalanceManager().take(target, amount);
                if (!success) {
                    sender.sendMessage(ChatColor.RED + name + " doesn't have enough money!");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Took " + sym + ChatColor.GREEN + " from " + name);
                if (target.isOnline() && target.getPlayer() != null)
                    target.getPlayer().sendMessage(ChatColor.RED + "An admin took " + sym + " from your balance.");
                break;
        }
        return true;
    }
}
