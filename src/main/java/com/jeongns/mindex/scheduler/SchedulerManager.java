package com.jeongns.mindex.scheduler;

import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.PlayerStateManager;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerManager implements Manager {
    @NonNull
    private final JavaPlugin plugin;
    @NonNull
    private final PlayerStateManager playerStateManager;
    private BukkitTask autoSaveTask;

    public SchedulerManager(@NonNull JavaPlugin plugin, @NonNull PlayerStateManager playerStateManager) {
        this.plugin = plugin;
        this.playerStateManager = playerStateManager;
    }

    @Override
    public void initialize() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        scheduleAutoSave();
    }

    @Override
    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }

    private void scheduleAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        int intervalMinutes = Math.max(0, plugin.getConfig().getInt("player-state.auto-save-interval-minutes", 1));
        if (intervalMinutes <= 0) {
            return;
        }

        long intervalTicks = intervalMinutes * 60L * 20L;
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                playerStateManager::flushDirty,
                intervalTicks,
                intervalTicks
        );
    }
}
