package com.twinsshards;

import com.twinsshards.command.ShardsCommand;
import com.twinsshards.command.ShardsTabCompleter;
import com.twinsshards.config.ConfigManager;
import com.twinsshards.database.DatabaseManager;
import com.twinsshards.database.MySQLDatabase;
import com.twinsshards.database.SQLiteDatabase;
import com.twinsshards.listener.PlayerListener;
import com.twinsshards.manager.AccountManager;
import com.twinsshards.manager.TopManager;
import com.twinsshards.papi.ShardsExpansion;
import com.twinsshards.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class TwinsShards extends JavaPlugin {

    private static TwinsShards instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private AccountManager accountManager;
    private TopManager topManager;
    private TaskScheduler.Task autoSaveTask;

    public static TwinsShards getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // 1. Konfigürasyon ve Dil Dosyalarını Yükle
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // 2. Veritabanı Başlat
        setupDatabase();

        // 3. Yöneticileri Başlat
        this.accountManager = new AccountManager(this);
        this.topManager = new TopManager(this);

        // Sunucu yeniden başlatıldıysa/reload atıldıysa mevcut oyuncuları yükle
        for (Player p : Bukkit.getOnlinePlayers()) {
            accountManager.loadPlayerAsync(p.getUniqueId(), p.getName());
        }

        this.topManager.start();

        // 4. Komutlar ve Dinleyiciler
        PluginCommand cmd = getCommand("kristal");
        if (cmd != null) {
            ShardsCommand executor = new ShardsCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(new ShardsTabCompleter());
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // 5. PlaceholderAPI Entegrasyonu (Hem twinsshards hem magnetickristal tanımlayıcıları)
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ShardsExpansion(this, "twinsshards").register();
            new ShardsExpansion(this, "magnetickristal").register();
            getLogger().info("PlaceholderAPI hooks registered for 'twinsshards' and 'magnetickristal'!");
        } else {
            getLogger().warning("PlaceholderAPI not found! PAPI placeholders disabled.");
        }

        // 6. Otomatik Kayıt Görevi (Folia & Spigot uyumlu)
        startAutoSaveTask();

        // Başlangıç Logu
        getLogger().info("=========================================");
        getLogger().info("   TwinsShards v" + getDescription().getVersion() + " by TwinsShards Team");
        getLogger().info("   Folia Support: " + (TaskScheduler.isFolia() ? "ENABLED (Folia Detected)" : "ENABLED (Standard Scheduler)"));
        getLogger().info("   Selected Language: " + configManager.getCurrentLanguage().toUpperCase());
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }

        if (topManager != null) {
            topManager.stop();
        }

        if (accountManager != null && databaseManager != null) {
            getLogger().info("Saving all balances to database...");
            accountManager.saveAllSync();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("TwinsShards disabled successfully.");
        instance = null;
    }

    private void setupDatabase() {
        String dbType = getConfig().getString("database.type", "sqlite").toLowerCase();
        if ("mysql".equals(dbType)) {
            this.databaseManager = new MySQLDatabase(this);
            getLogger().info("Database engine: MySQL");
        } else {
            this.databaseManager = new SQLiteDatabase(this);
            getLogger().info("Database engine: SQLite");
        }

        try {
            databaseManager.init();
            getLogger().info("Database initialized successfully.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
        }
    }

    private void startAutoSaveTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        long seconds = getConfig().getLong("auto-save-interval-seconds", 300L);
        seconds = Math.max(10L, seconds);

        autoSaveTask = TaskScheduler.runTimerAsync(this, () -> {
            if (accountManager != null) {
                accountManager.saveAllSync();
            }
        }, seconds, seconds);
    }

    public void reloadPlugin() {
        configManager.reload();
        topManager.start();
        startAutoSaveTask();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public TopManager getTopManager() {
        return topManager;
    }
}
