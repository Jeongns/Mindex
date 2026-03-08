package com.jeongns.mindex.player.repository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerStateRepository {
    Optional<com.jeongns.mindex.player.entity.PlayerMindexState> findByPlayerId(UUID playerId);

    void create(UUID playerId);

    boolean unlock(UUID playerId, String entryId);

    boolean claimCategoryReward(UUID playerId, String categoryId);

    void reset(UUID playerId);
}
