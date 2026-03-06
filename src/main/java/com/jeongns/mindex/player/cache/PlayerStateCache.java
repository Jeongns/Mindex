package com.jeongns.mindex.player.cache;

import com.jeongns.mindex.player.entity.PlayerMindexState;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateCache {
    @NonNull
    private final Map<UUID, PlayerMindexState> cache = new ConcurrentHashMap<>();
    @NonNull
    private final Set<UUID> dirtyPlayerIds = ConcurrentHashMap.newKeySet();

    public PlayerMindexState get(@NonNull UUID playerId) {
        return cache.get(playerId);
    }

    public PlayerMindexState putIfAbsent(@NonNull UUID playerId, @NonNull PlayerMindexState playerState) {
        return cache.computeIfAbsent(playerId, ignored -> playerState);
    }

    public void markDirty(@NonNull UUID playerId) {
        dirtyPlayerIds.add(playerId);
    }

    public boolean isDirty(@NonNull UUID playerId) {
        return dirtyPlayerIds.contains(playerId);
    }

    public void remove(@NonNull UUID playerId) {
        cache.remove(playerId);
        dirtyPlayerIds.remove(playerId);
    }

    public void clear() {
        cache.clear();
        dirtyPlayerIds.clear();
    }

    public List<PlayerMindexState> snapshotDirtyStates() {
        return snapshotDirtyEntries().stream()
                .map(DirtyPlayerState::playerState)
                .toList();
    }

    public List<DirtyPlayerState> snapshotDirtyEntries() {
        List<DirtyPlayerState> entries = new ArrayList<>();

        for (UUID playerId : Set.copyOf(dirtyPlayerIds)) {
            PlayerMindexState playerState = cache.get(playerId);
            if (playerState == null) {
                dirtyPlayerIds.remove(playerId);
                continue;
            }
            PlayerMindexState snapshot = playerState.copy();
            entries.add(new DirtyPlayerState(playerId, snapshot));
        }

        return entries;
    }

    public void clearDirty(@NonNull Collection<UUID> playerIds) {
        dirtyPlayerIds.removeAll(playerIds);
    }

    public record DirtyPlayerState(UUID playerId, PlayerMindexState playerState) {
    }
}
