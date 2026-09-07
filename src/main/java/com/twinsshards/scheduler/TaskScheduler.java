package com.twinsshards.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskScheduler {

    private static final boolean IS_FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public interface Task {
        void cancel();
    }

    /**
     * Asenkron tek seferlik görev çalıştırır.
     */
    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    /**
     * Asenkron periyodik görev başlatır.
     */
    public static Task runTimerAsync(Plugin plugin, Runnable runnable, long delaySeconds, long periodSeconds) {
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> runnable.run(),
                    delaySeconds,
                    periodSeconds,
                    TimeUnit.SECONDS
            );
            return task::cancel;
        } else {
            long delayTicks = Math.max(1L, delaySeconds * 20L);
            long periodTicks = Math.max(1L, periodSeconds * 20L);
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
            return task::cancel;
        }
    }
}
