package com.jeongns.mindex.player;

import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.cache.PlayerStateCache;
import com.jeongns.mindex.player.cache.PlayerStateCache.PendingPlayerStateChange;
import com.jeongns.mindex.player.cache.PlayerStateCache.PlayerStateChange;
import com.jeongns.mindex.player.cache.PlayerStateCache.PlayerStateChangeType;
import com.jeongns.mindex.player.entity.PlayerMindexState;
import com.jeongns.mindex.player.repository.PlayerStateRepository;
import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerStateManager implements Manager {
    @NonNull
    private final PlayerStateRepository repository;
    @NonNull
    private final PlayerStateCache cache;

    public PlayerStateManager(@NonNull PlayerStateRepository repository) {
        this.repository = repository;
        this.cache = new PlayerStateCache();
    }

    public Optional<PlayerMindexState> find(@NonNull UUID playerId) {
        PlayerMindexState cachedState = cache.get(playerId);
        if (cachedState != null) {
            return Optional.of(cachedState);
        }

        Optional<PlayerMindexState> repositoryState = repository.findByPlayerId(playerId);
        repositoryState.ifPresent(state -> cache.putIfAbsent(playerId, state));
        return repositoryState;
    }

    public PlayerMindexState create(@NonNull UUID playerId) {
        if (find(playerId).isPresent()) {
            throw new IllegalStateException("이미 플레이어 상태가 존재합니다: " + playerId);
        }

        PlayerMindexState initialState = new PlayerMindexState(playerId, new HashSet<>(), new HashSet<>());
        cache.putIfAbsent(playerId, initialState);
        cache.enqueueCreate(playerId);
        return initialState;
    }

    public boolean unlock(@NonNull UUID playerId, @NonNull String entryId) {
        PlayerMindexState playerState = find(playerId).orElseGet(() -> create(playerId));
        boolean unlocked = playerState.unlock(entryId);
        if (unlocked) {
            cache.enqueueUnlock(playerId, entryId);
        }
        return unlocked;
    }

    public boolean claimCategoryReward(@NonNull UUID playerId, @NonNull String categoryId) {
        PlayerMindexState playerState = find(playerId).orElseGet(() -> create(playerId));
        boolean claimed = playerState.claimCategoryReward(categoryId);
        if (claimed) {
            cache.enqueueClaimCategoryReward(playerId, categoryId);
        }
        return claimed;
    }

    public void save(@NonNull UUID playerId) {
        List<PlayerStateChange> changes = cache.snapshotPlayerPendingChanges(playerId);
        if (changes.isEmpty()) {
            return;
        }
        applyChanges(playerId, changes);
        cache.clearPendingChanges(List.of(playerId));
    }

    public void reset(@NonNull UUID playerId) {
        cache.remove(playerId);
        repository.reset(playerId);
    }

    public void unload(@NonNull UUID playerId) {
        save(playerId);
        cache.remove(playerId);
    }

    public void flushDirty() {
        List<PendingPlayerStateChange> pendingChanges = cache.snapshotAllPendingChanges();
        if (pendingChanges.isEmpty()) {
            return;
        }
        for (PendingPlayerStateChange pendingPlayerStateChange : pendingChanges) {
            applyChanges(pendingPlayerStateChange.playerId(), pendingPlayerStateChange.changes());
        }
        cache.clearPendingChanges(pendingChanges.stream().map(PendingPlayerStateChange::playerId).toList());
    }

    @Override
    public void shutdown() {
        flushDirty();
        cache.clear();
    }

    private void applyChanges(@NonNull UUID playerId, @NonNull List<PlayerStateChange> changes) {
        for (PlayerStateChange change : changes) {
            if (change.type() == PlayerStateChangeType.CREATE) {
                repository.create(playerId);
                continue;
            }
            if (change.type() == PlayerStateChangeType.UNLOCK_ENTRY) {
                repository.unlock(playerId, change.value());
                continue;
            }
            if (change.type() == PlayerStateChangeType.CLAIM_CATEGORY_REWARD) {
                repository.claimCategoryReward(playerId, change.value());
            }
        }
    }
}
