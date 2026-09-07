package com.twinsshards.papi;

import com.twinsshards.TwinsShards;
import com.twinsshards.database.DatabaseManager;
import com.twinsshards.util.ColorUtil;
import com.twinsshards.util.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShardsExpansion extends PlaceholderExpansion {

    private final TwinsShards plugin;
    private final String identifier;

    private static final Pattern TOP_PATTERN = Pattern.compile("^top_(\\d+)$");
    private static final Pattern TOP_NAME_PATTERN = Pattern.compile("^top_(?:name|player)_(\\d+)$");
    private static final Pattern TOP_BAL_PATTERN = Pattern.compile("^top_balance_(\\d+)$");
    private static final Pattern TOP_BAL_FORMATTED_PATTERN = Pattern.compile("^top_balance_format(?:t)?ed_(\\d+)$");
    private static final Pattern TOP_BAL_COMMAS_PATTERN = Pattern.compile("^top_balance_commas_(\\d+)$");

    public ShardsExpansion(TwinsShards plugin, String identifier) {
        this.plugin = plugin;
        this.identifier = identifier;
    }

    public ShardsExpansion(TwinsShards plugin) {
        this(plugin, "twinsshards");
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return "TwinsShards Team";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // 1. Oyuncu Bakiyesi Placeholder'ları
        if (player != null) {
            double balance = plugin.getAccountManager().getBalance(player.getUniqueId());

            if (params.equalsIgnoreCase("balance")) {
                return NumberFormatter.formatRaw(balance);
            }
            if (params.equalsIgnoreCase("balance_formatted") || params.equalsIgnoreCase("balance_formated")) {
                return NumberFormatter.formatCompact(balance);
            }
            if (params.equalsIgnoreCase("balance_commas")) {
                return NumberFormatter.formatCommas(balance);
            }
        }

        // 2. Sıralama (Top) Placeholder'ları
        // %twinsshards_top_1%, %twinsshards_top_2% ...
        Matcher topMatcher = TOP_PATTERN.matcher(params);
        if (topMatcher.matches()) {
            int rank = Integer.parseInt(topMatcher.group(1));
            DatabaseManager.TopEntry entry = plugin.getTopManager().getEntry(rank);
            if (entry == null) {
                return ColorUtil.color(plugin.getConfig().getString("top.empty-slot", "&7---"));
            }
            String format = plugin.getConfig().getString("top.placeholder-format", "&e%pos%. &f%player% &8» &b%balance_formatted% &7Shards");
            format = format.replace("%pos%", String.valueOf(rank))
                    .replace("%player%", entry.name())
                    .replace("%balance%", NumberFormatter.formatRaw(entry.balance()))
                    .replace("%balance_formatted%", NumberFormatter.formatCompact(entry.balance()))
                    .replace("%balance_commas%", NumberFormatter.formatCommas(entry.balance()));
            return ColorUtil.color(format);
        }

        // %twinsshards_top_name_1% / %twinsshards_top_player_1%
        Matcher topNameMatcher = TOP_NAME_PATTERN.matcher(params);
        if (topNameMatcher.matches()) {
            int rank = Integer.parseInt(topNameMatcher.group(1));
            DatabaseManager.TopEntry entry = plugin.getTopManager().getEntry(rank);
            return entry != null ? entry.name() : ColorUtil.color(plugin.getConfig().getString("top.empty-slot", "&7---"));
        }

        // %twinsshards_top_balance_1%
        Matcher topBalMatcher = TOP_BAL_PATTERN.matcher(params);
        if (topBalMatcher.matches()) {
            int rank = Integer.parseInt(topBalMatcher.group(1));
            DatabaseManager.TopEntry entry = plugin.getTopManager().getEntry(rank);
            return entry != null ? NumberFormatter.formatRaw(entry.balance()) : "0";
        }

        // %twinsshards_top_balance_formatted_1%
        Matcher topBalFormattedMatcher = TOP_BAL_FORMATTED_PATTERN.matcher(params);
        if (topBalFormattedMatcher.matches()) {
            int rank = Integer.parseInt(topBalFormattedMatcher.group(1));
            DatabaseManager.TopEntry entry = plugin.getTopManager().getEntry(rank);
            return entry != null ? NumberFormatter.formatCompact(entry.balance()) : "0";
        }

        // %twinsshards_top_balance_commas_1%
        Matcher topBalCommasMatcher = TOP_BAL_COMMAS_PATTERN.matcher(params);
        if (topBalCommasMatcher.matches()) {
            int rank = Integer.parseInt(topBalCommasMatcher.group(1));
            DatabaseManager.TopEntry entry = plugin.getTopManager().getEntry(rank);
            return entry != null ? NumberFormatter.formatCommas(entry.balance()) : "0";
        }

        return null;
    }
}
