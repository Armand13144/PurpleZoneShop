package com.purplezone.shop.commands;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.gui.MainShopGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("purplezone.shop.admin")) {
                player.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            plugin.getShopManager().reload();
            player.sendMessage(ChatColor.GREEN + "Shop reloaded!");
            return true;
        }

        MainShopGUI.open(player);
        return true;
    }
}
