package com.purplezone.shop.listeners;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.gui.MainShopGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCListener implements Listener {

    private final ShopPlugin plugin;

    public NPCListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent e) {
        Player player = e.getClicker();
        String npcName = ChatColor.stripColor(e.getNPC().getName());

        // Open shop for any NPC with "shop" in the name (case-insensitive)
        if (npcName.toLowerCase().contains("shop")) {
            MainShopGUI.open(player);
        }
    }
}
