package com.jeongns.mindex.player.cache;

import com.jeongns.mindex.player.entity.PlayerMindexState;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateCache {
    @NonNull
    private final Map<UUID, PlayerMindexState> cache = new ConcurrentHashMap<>();
    @NonNull
    private final Map<UUID, List<PlayerStateChange>> pendingChanges = new ConcurrentHashMap<>();

    public PlayerMindexState get(@NonNull UUID playerId) {
        return cache.get(playerId);
    }

    public PlayerMindexState putIfAbsent(@NonNull UUID playerId, @NonNull PlayerMindexState playerState) {
        return cache.computeIfAbsent(playerId, ignored -> playerState);
    }

    public void enqueueCreate(@NonNull UUID playerId) {
        enqueue(playerId, new PlayerStateChange(PlayerStateChangeType.CREATE, null));
    }

    public void enqueueUnlock(@NonNull UUID playerId, @NonNull String entryId) {
        enqueue(playerId, new PlayerStateChange(PlayerStateChangeType.UNLOCK_ENTRY, entryId));
    }

    public void enqueueClaimCategoryReward(@NonNull UUID playerId, @NonNull String categoryId) {
        enqueue(playerId, new PlayerStateChange(PlayerStateChangeType.CLAIM_CATEGORY_REWARD, categoryId));
    }

    private void enqueue(@NonNull UUID playerId, @NonNull PlayerStateChange change) {
        pendingChanges.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(change);
    }

    public void remove(@NonNull UUID playerId) {
        cache.remove(playerId);
        pendingChanges.remove(playerId);
    }

    public void clear() {
        cache.clear();
        pendingChanges.clear();
    }

    public List<PendingPlayerStateChange> snapshotAllPendingChanges() {
        List<PendingPlayerStateChange> changes = new ArrayList<>();

        for (Map.Entry<UUID, List<PlayerStateChange>> entry : List.copyOf(pendingChanges.entrySet())) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            changes.add(new PendingPlayerStateChange(entry.getKey(), List.copyOf(entry.getValue())));
        }

        return changes;
    }

    public List<PlayerStateChange> snapshotPlayerPendingChanges(@NonNull UUID playerId) {
        List<PlayerStateChange> changes = pendingChanges.get(playerId);
        return changes == null ? List.of() : List.copyOf(changes);
    }

    public void clearPendingChanges(@NonNull Collection<UUID> playerIds) {
        playerIds.forEach(pendingChanges::remove);
    }

    public record PendingPlayerStateChange(UUID playerId, List<PlayerStateChange> changes) {
    }

    public record PlayerStateChange(PlayerStateChangeType type, String value) {
    }

    public enum PlayerStateChangeType {
        CREATE,
        UNLOCK_ENTRY,
        CLAIM_CATEGORY_REWARD
    }
}
