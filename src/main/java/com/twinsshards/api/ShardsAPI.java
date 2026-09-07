package com.twinsshards.api;

import com.twinsshards.TwinsShards;
import com.twinsshards.manager.AccountManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ShardsAPI {

    private ShardsAPI() {}

    private static AccountManager getAccountManager() {
        return TwinsShards.getInstance().getAccountManager();
    }

    public static double getBalance(UUID uuid) {
        return getAccountManager().getBalance(uuid);
    }

    public static double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public static void giveBalance(UUID uuid, String name, double amount) {
        getAccountManager().giveBalance(uuid, name, amount);
    }

    public static void giveBalance(Player player, double amount) {
        giveBalance(player.getUniqueId(), player.getName(), amount);
    }

    public static boolean takeBalance(UUID uuid, String name, double amount) {
        return getAccountManager().takeBalance(uuid, name, amount);
    }

    public static boolean takeBalance(Player player, double amount) {
        return takeBalance(player.getUniqueId(), player.getName(), amount);
    }

    public static void setBalance(UUID uuid, String name, double amount) {
        getAccountManager().setBalance(uuid, name, amount);
    }

    public static void setBalance(Player player, double amount) {
        setBalance(player.getUniqueId(), player.getName(), amount);
    }

    public static boolean hasBalance(UUID uuid, double amount) {
        return getAccountManager().hasBalance(uuid, amount);
    }

    public static boolean hasBalance(Player player, double amount) {
        return hasBalance(player.getUniqueId(), amount);
    }

    public static void resetBalance(UUID uuid, String name) {
        getAccountManager().resetBalance(uuid, name);
    }

    public static void resetBalance(Player player) {
        resetBalance(player.getUniqueId(), player.getName());
    }
}
