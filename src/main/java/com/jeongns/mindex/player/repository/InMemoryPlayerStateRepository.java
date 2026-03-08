package com.jeongns.mindex.player.repository;

import com.jeongns.mindex.player.entity.PlayerMindexState;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPlayerStateRepository implements PlayerStateRepository {
    private final Map<UUID, PlayerMindexState> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<PlayerMindexState> findByPlayerId(@NonNull UUID playerId) {
        return Optional.ofNullable(storage.get(playerId));
    }

    @Override
    public void create(@NonNull UUID playerId) {
        storage.putIfAbsent(playerId, new PlayerMindexState(playerId, new HashSet<>(), new HashSet<>()));
    }

    @Override
    public boolean unlock(@NonNull UUID playerId, @NonNull String entryId) {
        create(playerId);
        PlayerMindexState playerState = storage.get(playerId);
        return playerState != null && playerState.unlock(entryId);
    }

    @Override
    public boolean claimCategoryReward(@NonNull UUID playerId, @NonNull String categoryId) {
        create(playerId);
        PlayerMindexState playerState = storage.get(playerId);
        return playerState != null && playerState.claimCategoryReward(categoryId);
    }

    @Override
    public void reset(@NonNull UUID playerId) {
        storage.remove(playerId);
    }
}
