package com.jeongns.mindex.listener;

import com.jeongns.mindex.mindexGui.MindexGuiManager;
import com.jeongns.mindex.listener.handler.MindexGuiListener;
import com.jeongns.mindex.listener.handler.PlayerStateListener;
import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.PlayerStateManager;
import lombok.NonNull;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ListenerManager implements Manager {
    @NonNull
    private final JavaPlugin plugin;
    @NonNull
    private final List<Listener> listeners;

    public ListenerManager(
            @NonNull JavaPlugin plugin,
            @NonNull MindexGuiManager mindexGuiManager,
            @NonNull PlayerStateManager playerStateManager
    ) {
        this.plugin = plugin;
        this.listeners = List.of(
                new MindexGuiListener(mindexGuiManager),
                new PlayerStateListener(playerStateManager)
        );
    }

    @Override
    public void initialize() {
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void shutdown() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }
}
