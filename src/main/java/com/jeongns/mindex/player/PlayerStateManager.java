package com.jeongns.mindex.player;

import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.cache.PlayerStateCache;
import com.jeongns.mindex.player.entity.PlayerMindexState;
import com.jeongns.mindex.player.repository.PlayerStateRepository;
import lombok.NonNull;

import java.util.HashSet;
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

        repository.create(playerId);
        PlayerMindexState initialState = new PlayerMindexState(playerId, new HashSet<>(), new HashSet<>());
        cache.putIfAbsent(playerId, initialState);
        return initialState;
    }

    public boolean unlock(@NonNull UUID playerId, @NonNull String entryId) {
        PlayerMindexState playerState = find(playerId).orElseGet(() -> create(playerId));
        if (playerState.isUnlocked(entryId)) {
            return false;
        }

        repository.unlock(playerId, entryId);
        playerState.unlock(entryId);
        return true;
    }

    public boolean claimCategoryReward(@NonNull UUID playerId, @NonNull String categoryId) {
        PlayerMindexState playerState = find(playerId).orElseGet(() -> create(playerId));
        if (playerState.hasClaimedCategoryReward(categoryId)) {
            return false;
        }

        repository.claimCategoryReward(playerId, categoryId);
        playerState.claimCategoryReward(categoryId);
        return true;
    }

    public void reset(@NonNull UUID playerId) {
        cache.remove(playerId);
        repository.reset(playerId);
    }

    public void unload(@NonNull UUID playerId) {
        cache.remove(playerId);
    }

    @Override
    public void reload() {
        cache.clear();
    }

    @Override
    public void shutdown() {
        cache.clear();
        repository.close();
    }
}
