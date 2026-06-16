package com.purplezone.shop.commands;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.gui.SellChestGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public SellCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /sell.");
            return true;
        }
        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Place items in the chest and close it to sell!");
        SellChestGUI.open(player);
        return true;
    }
}
