package com.jeongns.mindex.repository;

import com.jeongns.mindex.player.entity.PlayerMindexState;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStateRepository {
    Optional<PlayerMindexState> findByPlayerId(UUID playerId);

    void save(PlayerMindexState playerState);

    void saveAll(Collection<PlayerMindexState> playerStates);

    void deleteByPlayerId(UUID playerId);
}
