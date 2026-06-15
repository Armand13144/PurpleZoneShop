package com.purplezone.shop.commands;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.managers.BalanceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public PayCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /pay.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        Player from = (Player) sender;
        Player to = Bukkit.getPlayerExact(args[0]);

        if (to == null) {
            from.sendMessage(ChatColor.RED + "Player '" + args[0] + "' is not online.");
            return true;
        }

        if (to.equals(from)) {
            from.sendMessage(ChatColor.RED + "You can't pay yourself!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            from.sendMessage(ChatColor.RED + "Invalid amount. Use a number like: /pay Steve 100");
            return true;
        }

        if (amount <= 0) {
            from.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
            return true;
        }

        // Minimum pay amount to prevent spam
        if (amount < 1.0) {
            from.sendMessage(ChatColor.RED + "Minimum payment is $1.00.");
            return true;
        }

        BalanceManager bm = plugin.getBalanceManager();

        if (!bm.withdraw(from, amount)) {
            from.sendMessage(ChatColor.RED + "You don't have enough money!"
                    + ChatColor.GRAY + " (Balance: " + bm.format(bm.getBalance(from)) + ChatColor.GRAY + ")");
            return true;
        }

        bm.deposit(to, amount);

        String formatted = bm.format(amount);

        // Notify sender
        from.sendMessage(ChatColor.GREEN + "✦ " + ChatColor.WHITE + "You sent "
                + ChatColor.GOLD + formatted
                + ChatColor.WHITE + " to "
                + ChatColor.AQUA + to.getName()
                + ChatColor.GRAY + " | Your balance: " + bm.format(bm.getBalance(from)));

        // Notify receiver
        to.sendMessage(ChatColor.GREEN + "✦ " + ChatColor.AQUA + from.getName()
                + ChatColor.WHITE + " sent you "
                + ChatColor.GOLD + formatted
                + ChatColor.GRAY + " | Your balance: " + bm.format(bm.getBalance(to)));

        // Log to console
        plugin.getLogger().info("[Pay] " + from.getName() + " → " + to.getName() + ": " + formatted);

        return true;
    }
}
