package com.twinsshards.command;

import com.twinsshards.TwinsShards;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShardsTabCompleter implements TabCompleter {

    // Türkçe Alt Komutlar
    private static final List<String> TR_USER_SUBCOMMANDS = Arrays.asList("bak", "gonder", "top", "yardim");
    private static final List<String> TR_ADMIN_SUBCOMMANDS = Arrays.asList("ver", "al", "ayarla", "sifirla", "yenile");

    // İngilizce Alt Komutlar
    private static final List<String> EN_USER_SUBCOMMANDS = Arrays.asList("balance", "pay", "top", "help");
    private static final List<String> EN_ADMIN_SUBCOMMANDS = Arrays.asList("give", "take", "set", "reset", "reload");

    private static final List<String> AMOUNT_SUGGESTIONS = Arrays.asList("10", "100", "500", "1000", "5000", "10000");

    private static final Set<String> TR_ALIASES = new HashSet<>(Arrays.asList("kristal", "kristaller", "cr"));
    private static final Set<String> EN_ALIASES = new HashSet<>(Arrays.asList("shards", "shard", "crystal", "twinsshards", "ts"));

    public static boolean isTurkishAlias(String alias) {
        if (alias == null) return true;
        String lower = alias.toLowerCase();
        if (TR_ALIASES.contains(lower)) return true;
        if (EN_ALIASES.contains(lower)) return false;
        // Varsayılan config diline göre karar ver
        return "tr".equalsIgnoreCase(TwinsShards.getInstance().getConfigManager().getCurrentLanguage());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        boolean isTurkish = isTurkishAlias(alias);

        if (args.length == 1) {
            List<String> available = new ArrayList<>();
            boolean hasAdmin = sender.hasPermission("twinsshards.admin") || sender.hasPermission("magnetickristal.admin") || sender.isOp();

            if (isTurkish) {
                // Sadece Türkçe argümanlar
                available.addAll(TR_USER_SUBCOMMANDS);
                if (hasAdmin) {
                    available.addAll(TR_ADMIN_SUBCOMMANDS);
                } else {
                    if (hasPermission(sender, "give")) available.add("ver");
                    if (hasPermission(sender, "take")) available.add("al");
                    if (hasPermission(sender, "set")) available.add("ayarla");
                    if (hasPermission(sender, "reset")) available.add("sifirla");
                    if (hasPermission(sender, "reload")) available.add("yenile");
                }
            } else {
                // Sadece İngilizce argümanlar
                available.addAll(EN_USER_SUBCOMMANDS);
                if (hasAdmin) {
                    available.addAll(EN_ADMIN_SUBCOMMANDS);
                } else {
                    if (hasPermission(sender, "give")) available.add("give");
                    if (hasPermission(sender, "take")) available.add("take");
                    if (hasPermission(sender, "set")) available.add("set");
                    if (hasPermission(sender, "reset")) available.add("reset");
                    if (hasPermission(sender, "reload")) available.add("reload");
                }
            }
            return StringUtil.copyPartialMatches(args[0], available, completions);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            // Hem Türkçe hem İngilizce destekleyen oyuncu hedefli komutlar
            if (Arrays.asList("bak", "gonder", "ver", "al", "ayarla", "sifirla",
                    "balance", "pay", "give", "take", "set", "reset").contains(sub)) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                return StringUtil.copyPartialMatches(args[1], playerNames, completions);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            // Miktar gerektiren komutlar
            if (Arrays.asList("gonder", "ver", "al", "ayarla",
                    "pay", "give", "take", "set").contains(sub)) {
                return StringUtil.copyPartialMatches(args[2], AMOUNT_SUGGESTIONS, completions);
            }
        }

        return Collections.emptyList();
    }

    private boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission("twinsshards.admin." + node) || sender.hasPermission("magnetickristal.admin." + node);
    }
}
