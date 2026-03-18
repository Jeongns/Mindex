package com.jeongns.mindex.player.cache;

import com.jeongns.mindex.player.entity.PlayerMindexState;
import lombok.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateCache {
    @NonNull
    private final Map<UUID, PlayerMindexState> cache = new ConcurrentHashMap<>();

    public PlayerMindexState get(@NonNull UUID playerId) {
        return cache.get(playerId);
    }

    public PlayerMindexState putIfAbsent(@NonNull UUID playerId, @NonNull PlayerMindexState playerState) {
        return cache.computeIfAbsent(playerId, ignored -> playerState);
    }

    public void remove(@NonNull UUID playerId) {
        cache.remove(playerId);
    }

    public void clear() {
        cache.clear();
    }
}
