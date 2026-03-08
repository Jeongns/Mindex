package com.jeongns.mindex.player;

import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.cache.PlayerStateCache;
import com.jeongns.mindex.player.cache.PlayerStateCache.DirtyPlayerState;
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
        return initialState;
    }

    public boolean unlock(@NonNull UUID playerId, @NonNull String entryId) {
        PlayerMindexState playerState = find(playerId).orElseGet(() -> create(playerId));
        boolean unlocked = playerState.unlock(entryId);
        if (unlocked) {
            cache.markDirty(playerId);
        }
        return unlocked;
    }

    public boolean claimCategoryReward(@NonNull UUID playerId, @NonNull String categoryId) {
        PlayerMindexState playerState = find(playerId).orElseGet(() -> create(playerId));
        boolean claimed = playerState.claimCategoryReward(categoryId);
        if (claimed) {
            cache.markDirty(playerId);
        }
        return claimed;
    }

    public void save(@NonNull UUID playerId) {
        PlayerMindexState playerState = cache.get(playerId);
        if (playerState == null || !cache.isDirty(playerId)) {
            return;
        }
        repository.save(playerState.copy());
        cache.clearDirty(List.of(playerId));
    }

    public void reset(@NonNull UUID playerId) {
        cache.remove(playerId);
        repository.deleteByPlayerId(playerId);
    }

    public void unload(@NonNull UUID playerId) {
        save(playerId);
        cache.remove(playerId);
    }

    public void flushDirty() {
        List<DirtyPlayerState> dirtyEntries = cache.snapshotDirtyEntries();
        if (dirtyEntries.isEmpty()) {
            return;
        }
        repository.saveAll(dirtyEntries.stream().map(DirtyPlayerState::playerState).toList());
        cache.clearDirty(dirtyEntries.stream().map(DirtyPlayerState::playerId).toList());
    }

    @Override
    public void shutdown() {
        flushDirty();
        cache.clear();
    }
}
