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

    public boolean isUnlocked(String entryId) {
        return unlockedEntryIds.contains(entryId);
    }

    public boolean unlock(String entryId) {
        return unlockedEntryIds.add(entryId);
    }

    public int getUnlockedCount() {
        return unlockedEntryIds.size();
    }
}
