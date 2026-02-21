package com.jeongns.mindex.player;

import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.player.entity.PlayerMindexState;
import com.jeongns.mindex.repository.PlayerStateRepository;

import java.util.UUID;

public class PlayerStateManager implements Manager {
    private final PlayerStateRepository repository;

    public PlayerStateManager(PlayerStateRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initialize() {
    }

    public void load(UUID playerId) {
    }

    public PlayerMindexState getOrCreate(UUID playerId) {
        return null;
    }

    public void save(UUID playerId) {
    }

    public void saveAll() {
    }

    public void unload(UUID playerId) {
    }
}
