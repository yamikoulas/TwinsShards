package com.twinsshards.command;

import com.twinsshards.TwinsShards;
import com.twinsshards.config.ConfigManager;
import com.twinsshards.database.DatabaseManager;
import com.twinsshards.manager.AccountManager;
import com.twinsshards.util.ColorUtil;
import com.twinsshards.util.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ShardsCommand implements CommandExecutor {

    private final TwinsShards plugin;
    private final ConfigManager configManager;
    private final AccountManager accountManager;

    public ShardsCommand(TwinsShards plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.accountManager = plugin.getAccountManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isTr = ShardsTabCompleter.isTurkishAlias(label);
        String lang = isTr ? "tr" : "en";

        if (args.length == 0) {
            if (sender instanceof Player player) {
                sendBalance(sender, player.getUniqueId(), player.getName(), true, lang);
            } else {
                sendHelp(sender, lang);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "yardim", "help" -> {
                sendHelp(sender, lang);
                return true;
            }

            case "bak", "bakiye", "balance", "view" -> {
                handleBalance(sender, args, lang);
                return true;
            }

            case "top", "siralama", "leaderboard" -> {
                handleTop(sender, lang);
                return true;
            }

            case "pay", "gonder", "send" -> {
                handlePay(sender, args, lang, label);
                return true;
            }

            case "give", "ekle", "ver", "add" -> {
                handleGive(sender, args, lang, label);
                return true;
            }

            case "take", "al", "sil", "eksilt", "remove" -> {
                handleTake(sender, args, lang, label);
                return true;
            }

            case "set", "ayarla" -> {
                handleSet(sender, args, lang, label);
                return true;
            }

            case "reset", "sifirla" -> {
                handleReset(sender, args, lang, label);
                return true;
            }

            case "reload", "yenile" -> {
                handleReload(sender, lang);
                return true;
            }

            default -> {
                if (args.length == 1) {
                    handleBalance(sender, new String[]{"bak", args[0]}, lang);
                } else {
                    sendHelp(sender, lang);
                }
                return true;
            }
        }
    }

    private void handleBalance(CommandSender sender, String[] args, String lang) {
        if (args.length == 1) {
            if (sender instanceof Player player) {
                sendBalance(sender, player.getUniqueId(), player.getName(), true, lang);
            } else {
                sender.sendMessage(configManager.getMessage("player-only", lang));
            }
            return;
        }

        String targetName = args[1];
        UUID targetUuid = accountManager.getUuidByName(targetName);
        if (targetUuid == null) {
            sender.sendMessage(configManager.getFormattedForLang("player-not-found", lang, "%player%", targetName));
            return;
        }

        sendBalance(sender, targetUuid, targetName, false, lang);
    }

    private void sendBalance(CommandSender viewer, UUID uuid, String name, boolean self, String lang) {
        double balance = accountManager.getBalance(uuid);
        String formatted = NumberFormatter.formatCompact(balance);
        String commas = NumberFormatter.formatCommas(balance);

        String path = self ? "balance-self" : "balance-other";
        String msg = configManager.getFormattedForLang(path, lang,
                "%player%", name,
                "%balance%", NumberFormatter.formatRaw(balance),
                "%balance_formatted%", formatted,
                "%balance_commas%", commas
        );
        viewer.sendMessage(msg);
    }

    private void handlePay(CommandSender sender, String[] args, String lang, String label) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("player-only", lang));
            return;
        }

        if (!player.hasPermission("twinsshards.pay") && !player.hasPermission("magnetickristal.pay")) {
            player.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        if (args.length < 3) {
            String subCmd = lang.equals("tr") ? "gonder" : "pay";
            player.sendMessage(ColorUtil.color("&c" + (lang.equals("tr") ? "Kullanım" : "Usage") + ": &f/" + label + " " + subCmd + " <" + (lang.equals("tr") ? "oyuncu" : "player") + "> <" + (lang.equals("tr") ? "miktar" : "amount") + ">"));
            return;
        }

        String targetName = args[1];
        if (player.getName().equalsIgnoreCase(targetName)) {
            player.sendMessage(configManager.getMessage("pay-self-error", lang));
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : accountManager.getUuidByName(targetName);

        if (targetUuid == null) {
            player.sendMessage(configManager.getFormattedForLang("player-not-found", lang, "%player%", targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(configManager.getMessage("invalid-amount", lang));
            return;
        }

        if (!accountManager.hasBalance(player.getUniqueId(), amount)) {
            player.sendMessage(configManager.getFormattedForLang("pay-insufficient", lang,
                    "%balance_formatted%", NumberFormatter.formatCompact(accountManager.getBalance(player.getUniqueId()))
            ));
            return;
        }

        accountManager.takeBalance(player.getUniqueId(), player.getName(), amount);
        accountManager.giveBalance(targetUuid, target != null ? target.getName() : targetName, amount);

        String amountFormatted = NumberFormatter.formatCompact(amount);
        player.sendMessage(configManager.getFormattedForLang("pay-sent", lang,
                "%player%", targetName,
                "%amount_formatted%", amountFormatted
        ));

        if (target != null && target.isOnline()) {
            target.sendMessage(configManager.getFormattedForLang("pay-received", lang,
                    "%player%", player.getName(),
                    "%amount_formatted%", amountFormatted
            ));
        }
    }

    private void handleTop(CommandSender sender, String lang) {
        if (!sender.hasPermission("twinsshards.top") && !sender.hasPermission("twinsshards.use")
                && !sender.hasPermission("magnetickristal.top") && !sender.hasPermission("magnetickristal.use")) {
            sender.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        List<DatabaseManager.TopEntry> topList = plugin.getTopManager().getTopList();

        for (String line : configManager.getMessageList("top-header", lang)) {
            sender.sendMessage(line);
        }

        if (topList.isEmpty()) {
            sender.sendMessage(configManager.getMessage("top-empty", lang));
        } else {
            for (int i = 0; i < topList.size(); i++) {
                DatabaseManager.TopEntry entry = topList.get(i);
                String line = configManager.getFormattedForLang("top-line", lang,
                        "%pos%", String.valueOf(i + 1),
                        "%player%", entry.name(),
                        "%balance%", NumberFormatter.formatRaw(entry.balance()),
                        "%balance_formatted%", NumberFormatter.formatCompact(entry.balance()),
                        "%balance_commas%", NumberFormatter.formatCommas(entry.balance())
                );
                sender.sendMessage(line);
            }
        }

        for (String line : configManager.getMessageList("top-footer", lang)) {
            sender.sendMessage(line);
        }
    }

    private void handleGive(CommandSender sender, String[] args, String lang, String label) {
        if (!hasAdminPermission(sender, "give")) {
            sender.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        if (args.length < 3) {
            String subCmd = lang.equals("tr") ? "ver" : "give";
            sender.sendMessage(ColorUtil.color("&c" + (lang.equals("tr") ? "Kullanım" : "Usage") + ": &f/" + label + " " + subCmd + " <" + (lang.equals("tr") ? "oyuncu" : "player") + "> <" + (lang.equals("tr") ? "miktar" : "amount") + ">"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : accountManager.getUuidByName(targetName);

        if (targetUuid == null) {
            sender.sendMessage(configManager.getFormattedForLang("player-not-found", lang, "%player%", targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount", lang));
            return;
        }

        accountManager.giveBalance(targetUuid, target != null ? target.getName() : targetName, amount);
        double newBal = accountManager.getBalance(targetUuid);

        sender.sendMessage(configManager.getFormattedForLang("admin-give-success", lang,
                "%player%", targetName,
                "%amount_formatted%", NumberFormatter.formatCompact(amount),
                "%new_balance_formatted%", NumberFormatter.formatCompact(newBal)
        ));

        if (target != null && target.isOnline()) {
            target.sendMessage(configManager.getFormattedForLang("admin-give-received", lang,
                    "%amount_formatted%", NumberFormatter.formatCompact(amount)
            ));
        }
    }

    private void handleTake(CommandSender sender, String[] args, String lang, String label) {
        if (!hasAdminPermission(sender, "take")) {
            sender.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        if (args.length < 3) {
            String subCmd = lang.equals("tr") ? "al" : "take";
            sender.sendMessage(ColorUtil.color("&c" + (lang.equals("tr") ? "Kullanım" : "Usage") + ": &f/" + label + " " + subCmd + " <" + (lang.equals("tr") ? "oyuncu" : "player") + "> <" + (lang.equals("tr") ? "miktar" : "amount") + ">"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : accountManager.getUuidByName(targetName);

        if (targetUuid == null) {
            sender.sendMessage(configManager.getFormattedForLang("player-not-found", lang, "%player%", targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount", lang));
            return;
        }

        if (!accountManager.hasBalance(targetUuid, amount)) {
            sender.sendMessage(configManager.getFormattedForLang("admin-take-insufficient", lang,
                    "%balance_formatted%", NumberFormatter.formatCompact(accountManager.getBalance(targetUuid))
            ));
            return;
        }

        accountManager.takeBalance(targetUuid, target != null ? target.getName() : targetName, amount);
        double newBal = accountManager.getBalance(targetUuid);

        sender.sendMessage(configManager.getFormattedForLang("admin-take-success", lang,
                "%player%", targetName,
                "%amount_formatted%", NumberFormatter.formatCompact(amount),
                "%new_balance_formatted%", NumberFormatter.formatCompact(newBal)
        ));

        if (target != null && target.isOnline()) {
            target.sendMessage(configManager.getFormattedForLang("admin-take-deducted", lang,
                    "%amount_formatted%", NumberFormatter.formatCompact(amount)
            ));
        }
    }

    private void handleSet(CommandSender sender, String[] args, String lang, String label) {
        if (!hasAdminPermission(sender, "set")) {
            sender.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        if (args.length < 3) {
            String subCmd = lang.equals("tr") ? "ayarla" : "set";
            sender.sendMessage(ColorUtil.color("&c" + (lang.equals("tr") ? "Kullanım" : "Usage") + ": &f/" + label + " " + subCmd + " <" + (lang.equals("tr") ? "oyuncu" : "player") + "> <" + (lang.equals("tr") ? "miktar" : "amount") + ">"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : accountManager.getUuidByName(targetName);

        if (targetUuid == null) {
            sender.sendMessage(configManager.getFormattedForLang("player-not-found", lang, "%player%", targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount", lang));
            return;
        }

        accountManager.setBalance(targetUuid, target != null ? target.getName() : targetName, amount);

        sender.sendMessage(configManager.getFormattedForLang("admin-set-success", lang,
                "%player%", targetName,
                "%amount_formatted%", NumberFormatter.formatCompact(amount)
        ));

        if (target != null && target.isOnline()) {
            target.sendMessage(configManager.getFormattedForLang("admin-set-updated", lang,
                    "%amount_formatted%", NumberFormatter.formatCompact(amount)
            ));
        }
    }

    private void handleReset(CommandSender sender, String[] args, String lang, String label) {
        if (!hasAdminPermission(sender, "reset")) {
            sender.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        if (args.length < 2) {
            String subCmd = lang.equals("tr") ? "sifirla" : "reset";
            sender.sendMessage(ColorUtil.color("&c" + (lang.equals("tr") ? "Kullanım" : "Usage") + ": &f/" + label + " " + subCmd + " <" + (lang.equals("tr") ? "oyuncu" : "player") + ">"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : accountManager.getUuidByName(targetName);

        if (targetUuid == null) {
            sender.sendMessage(configManager.getFormattedForLang("player-not-found", lang, "%player%", targetName));
            return;
        }

        accountManager.resetBalance(targetUuid, target != null ? target.getName() : targetName);

        sender.sendMessage(configManager.getFormattedForLang("admin-reset-success", lang,
                "%player%", targetName
        ));

        if (target != null && target.isOnline()) {
            target.sendMessage(configManager.getMessage("admin-reset-notified", lang));
        }
    }

    private void handleReload(CommandSender sender, String lang) {
        if (!hasAdminPermission(sender, "reload")) {
            sender.sendMessage(configManager.getMessage("no-permission", lang));
            return;
        }

        plugin.reloadPlugin();
        sender.sendMessage(configManager.getMessage("reload-success", lang));
    }

    private void sendHelp(CommandSender sender, String lang) {
        for (String line : configManager.getMessageList("help.header", lang)) {
            sender.sendMessage(line);
        }

        for (String line : configManager.getMessageList("help.user-commands", lang)) {
            sender.sendMessage(line);
        }

        if (hasAnyAdminPermission(sender)) {
            for (String line : configManager.getMessageList("help.admin-commands", lang)) {
                sender.sendMessage(line);
            }
        }

        for (String line : configManager.getMessageList("help.footer", lang)) {
            sender.sendMessage(line);
        }
    }

    private boolean hasAdminPermission(CommandSender sender, String node) {
        return sender.hasPermission("twinsshards.admin") ||
                sender.hasPermission("twinsshards.admin." + node) ||
                sender.hasPermission("magnetickristal.admin") ||
                sender.hasPermission("magnetickristal.admin." + node) ||
                sender.isOp();
    }

    private boolean hasAnyAdminPermission(CommandSender sender) {
        return hasAdminPermission(sender, "give") ||
                hasAdminPermission(sender, "take") ||
                hasAdminPermission(sender, "set") ||
                hasAdminPermission(sender, "reset") ||
                hasAdminPermission(sender, "reload");
    }
}
