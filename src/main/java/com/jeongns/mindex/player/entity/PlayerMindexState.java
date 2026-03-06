package com.jeongns.mindex.player.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public final class PlayerMindexState {
    @NonNull
    private UUID playerUuid;
    @NonNull
    private Set<String> unlockedEntryIds;
    @NonNull
    private Set<String> claimedCategoryRewardIds;

    public boolean isUnlocked(String entryId) {
        return unlockedEntryIds.contains(entryId);
    }

    public boolean unlock(String entryId) {
        return unlockedEntryIds.add(entryId);
    }

    public int getUnlockedCount() {
        return unlockedEntryIds.size();
    }

    public boolean hasClaimedCategoryReward(String categoryId) {
        return claimedCategoryRewardIds.contains(categoryId);
    }

    public boolean claimCategoryReward(String categoryId) {
        return claimedCategoryRewardIds.add(categoryId);
    }

    public PlayerMindexState copy() {
        return new PlayerMindexState(
                playerUuid,
                Set.copyOf(unlockedEntryIds),
                Set.copyOf(claimedCategoryRewardIds)
        );
    }
}
