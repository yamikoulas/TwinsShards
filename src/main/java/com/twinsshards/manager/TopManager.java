package com.twinsshards.manager;

import com.twinsshards.TwinsShards;
import com.twinsshards.database.DatabaseManager;
import com.twinsshards.scheduler.TaskScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopManager {

    private final TwinsShards plugin;
    private List<DatabaseManager.TopEntry> cachedTop = new ArrayList<>();
    private TaskScheduler.Task updateTask;

    public TopManager(TwinsShards plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        long intervalSeconds = plugin.getConfig().getLong("top.update-interval-seconds", 60L);
        intervalSeconds = Math.max(5L, intervalSeconds);

        // İlk güncellemeyi hemen asenkron yap
        updateAsync();

        // Periyodik güncelleme görevi (Folia & Spigot uyumlu)
        this.updateTask = TaskScheduler.runTimerAsync(plugin, this::updateSyncInternal, intervalSeconds, intervalSeconds);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public void updateAsync() {
        TaskScheduler.runAsync(plugin, this::updateSyncInternal);
    }

    private void updateSyncInternal() {
        // En güncel verilerin yansıması için önce çevrimiçi oyuncuların bakiye önbelleğini veritabanına flush et
        plugin.getAccountManager().saveAllSync();

        int limit = plugin.getConfig().getInt("top.limit", 10);
        List<DatabaseManager.TopEntry> topList = plugin.getDatabaseManager().getTop(limit);
        synchronized (this) {
            this.cachedTop = Collections.unmodifiableList(new ArrayList<>(topList));
        }
    }

    public synchronized List<DatabaseManager.TopEntry> getTopList() {
        return cachedTop;
    }

    public synchronized DatabaseManager.TopEntry getEntry(int rank) {
        if (rank <= 0 || rank > cachedTop.size()) {
            return null;
        }
        return cachedTop.get(rank - 1);
    }
}
