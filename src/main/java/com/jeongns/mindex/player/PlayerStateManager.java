package com.jeongns.mindex.player;

import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.entity.PlayerMindexState;
import com.jeongns.mindex.player.repository.PlayerStateRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.HashSet;
import java.util.UUID;

@AllArgsConstructor
public class PlayerStateManager implements Manager {
    @NonNull
    private final PlayerStateRepository repository;

    @Override
    public void initialize() {
    }

    public void load(@NonNull UUID playerId) {
        repository.findByPlayerId(playerId);
    }

    public PlayerMindexState getOrCreate(@NonNull UUID playerId) {
        return repository.findByPlayerId(playerId).orElseGet(() -> {
            PlayerMindexState initialState = new PlayerMindexState(playerId, new HashSet<>());
            repository.save(initialState);
            return initialState;
        });
    }

    public boolean unlock(@NonNull UUID playerId, @NonNull String entryId) {
        PlayerMindexState playerState = getOrCreate(playerId);
        boolean unlocked = playerState.unlock(entryId);
        repository.save(playerState);
        return unlocked;
    }

    public boolean isUnlocked(@NonNull UUID playerId, @NonNull String entryId) {
        return getOrCreate(playerId).isUnlocked(entryId);
    }

    public void save(@NonNull UUID playerId) {
        repository.findByPlayerId(playerId).ifPresent(repository::save);
    }

    public void unload(@NonNull UUID playerId) {
    }
}
