package com.jeongns.mindex.listener.handler;

import com.jeongns.mindex.player.PlayerStateManager;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerStateListener implements Listener {
    @NonNull
    private final PlayerStateManager playerStateManager;

    public PlayerStateListener(@NonNull PlayerStateManager playerStateManager) {
        this.playerStateManager = playerStateManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerStateManager.unload(event.getPlayer().getUniqueId());
    }
}
