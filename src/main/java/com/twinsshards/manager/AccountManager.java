package com.twinsshards.manager;

import com.twinsshards.TwinsShards;
import com.twinsshards.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager {

    private final TwinsShards plugin;
    private final Map<UUID, Double> balanceCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> nameCache = new ConcurrentHashMap<>();

    public AccountManager(TwinsShards plugin) {
        this.plugin = plugin;
    }

    public double getDefaultBalance() {
        return plugin.getConfig().getDouble("default-balance", 0.0);
    }

    /**
     * Oyuncunun bakiyesini asenkron yükler ve önbelleğe alır.
     */
    public void loadPlayerAsync(UUID uuid, String name) {
        nameCache.put(uuid, name);
        TaskScheduler.runAsync(plugin, () -> {
            double bal = plugin.getDatabaseManager().loadBalance(uuid, name, getDefaultBalance());
            balanceCache.put(uuid, bal);
        });
    }

    /**
     * Oyuncunun bakiyesini önbellekten kaldırır ve veritabanına kaydeder.
     */
    public void unloadPlayer(UUID uuid) {
        Double bal = balanceCache.remove(uuid);
        String name = nameCache.remove(uuid);
        if (bal != null) {
            TaskScheduler.runAsync(plugin, () -> {
                plugin.getDatabaseManager().saveBalance(uuid, name, bal);
            });
        }
    }

    /**
     * Bakiye sorgulama (Çevrimiçi ise önbellekten, çevrimdışı ise veritabanından).
     */
    public double getBalance(UUID uuid) {
        if (balanceCache.containsKey(uuid)) {
            return balanceCache.get(uuid);
        }
        Player player = Bukkit.getPlayer(uuid);
        String name = player != null ? player.getName() : "Unknown";
        double bal = plugin.getDatabaseManager().loadBalance(uuid, name, getDefaultBalance());
        balanceCache.put(uuid, bal);
        return bal;
    }

    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public void setBalance(UUID uuid, String name, double amount) {
        double safeAmount = Math.max(0.0, amount);
        balanceCache.put(uuid, safeAmount);
        if (name != null) nameCache.put(uuid, name);
        saveAsync(uuid, name, safeAmount);
    }

    public void giveBalance(UUID uuid, String name, double amount) {
        if (amount <= 0) return;
        double current = getBalance(uuid);
        setBalance(uuid, name, current + amount);
    }

    public boolean takeBalance(UUID uuid, String name, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        setBalance(uuid, name, current - amount);
        return true;
    }

    public void resetBalance(UUID uuid, String name) {
        setBalance(uuid, name, getDefaultBalance());
    }

    public void saveAsync(UUID uuid, String name, double balance) {
        TaskScheduler.runAsync(plugin, () -> {
            plugin.getDatabaseManager().saveBalance(uuid, name, balance);
        });
    }

    public void saveAllSync() {
        for (Map.Entry<UUID, Double> entry : balanceCache.entrySet()) {
            UUID uuid = entry.getKey();
            double bal = entry.getValue();
            String name = nameCache.getOrDefault(uuid, "Unknown");
            plugin.getDatabaseManager().saveBalance(uuid, name, bal);
        }
    }

    public void saveAllAsync() {
        TaskScheduler.runAsync(plugin, this::saveAllSync);
    }

    public UUID getUuidByName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return player.getUniqueId();
        }
        return plugin.getDatabaseManager().getUuidByName(name);
    }
}
