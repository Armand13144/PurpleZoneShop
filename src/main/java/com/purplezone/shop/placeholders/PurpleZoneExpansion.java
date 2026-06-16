package com.purplezone.shop.placeholders;

import com.purplezone.shop.ShopPlugin;
import com.purplezone.shop.managers.BalanceManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registers the following placeholders for use in DecentHolograms and other plugins:
 *
 *  %purplezoneshop_balance%          → player's current balance (e.g. $500.00)
 *  %purplezoneshop_balance_raw%      → raw number, no symbol (e.g. 500.00)
 *  %purplezoneshop_rank%             → player's rank on the leaderboard (e.g. 3)
 *  %purplezoneshop_top_name_1%       → name of #1 richest player
 *  %purplezoneshop_top_name_2%       → name of #2 richest player  (up to 10)
 *  %purplezoneshop_top_balance_1%    → formatted balance of #1 player
 *  %purplezoneshop_top_balance_raw_1% → raw balance of #1 player
 */
public class PurpleZoneExpansion extends PlaceholderExpansion {

    private final ShopPlugin plugin;

    public PurpleZoneExpansion(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "purplezoneshop";
    }

    @Override
    public @NotNull String getAuthor() {
        return "PurpleZone";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Stay registered even after /papi reload
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        BalanceManager bm = plugin.getBalanceManager();

        // ── Player-specific placeholders ──────────────────────────────
        if (params.equals("balance")) {
            if (player == null) return "0";
            return bm.format(bm.getBalance(player.getUniqueId()));
        }

        if (params.equals("balance_raw")) {
            if (player == null) return "0";
            return String.format("%.2f", bm.getBalance(player.getUniqueId()));
        }

        if (params.equals("rank")) {
            if (player == null) return "-";
            List<Map.Entry<UUID, Double>> top = bm.getTopBalances(Integer.MAX_VALUE);
            for (int i = 0; i < top.size(); i++) {
                if (top.get(i).getKey().equals(player.getUniqueId())) {
                    return String.valueOf(i + 1);
                }
            }
            return "-";
        }

        // ── Leaderboard placeholders: top_name_1 … top_name_10 ───────
        if (params.startsWith("top_name_")) {
            int pos = parsePosition(params, "top_name_");
            if (pos < 1) return "";
            List<Map.Entry<UUID, Double>> top = bm.getTopBalances(10);
            if (pos > top.size()) return "---";
            return bm.getPlayerName(top.get(pos - 1).getKey());
        }

        // ── top_balance_1 … top_balance_10 ───────────────────────────
        if (params.startsWith("top_balance_raw_")) {
            int pos = parsePosition(params, "top_balance_raw_");
            if (pos < 1) return "";
            List<Map.Entry<UUID, Double>> top = bm.getTopBalances(10);
            if (pos > top.size()) return "0";
            return String.format("%.2f", top.get(pos - 1).getValue());
        }

        if (params.startsWith("top_balance_")) {
            int pos = parsePosition(params, "top_balance_");
            if (pos < 1) return "";
            List<Map.Entry<UUID, Double>> top = bm.getTopBalances(10);
            if (pos > top.size()) return "$0.00";
            return bm.format(top.get(pos - 1).getValue());
        }

        return null; // placeholder not found
    }

    private int parsePosition(String params, String prefix) {
        try {
            return Integer.parseInt(params.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
