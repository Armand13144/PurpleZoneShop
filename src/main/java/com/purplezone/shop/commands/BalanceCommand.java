package com.purplezone.shop.commands;

import com.purplezone.shop.ShopPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public BalanceCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        double bal = plugin.getBalanceManager().getBalance(player);
        player.sendMessage(ChatColor.GOLD + "Your balance: "
                + ChatColor.YELLOW + plugin.getBalanceManager().format(bal));
        return true;
    }
}
