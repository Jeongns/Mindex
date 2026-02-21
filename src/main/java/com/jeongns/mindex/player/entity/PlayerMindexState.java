package com.jeongns.mindex.player.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
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
    private Map<String, Integer> progressByEntryId;
    @NonNull
    private Set<String> claimedCompletionKeys;

    public boolean isUnlocked(String entryId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean unlock(String entryId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int getProgress(String entryId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int addProgress(String entryId, int amount) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void setProgress(String entryId, int value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean hasClaimed(String completionKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean markClaimed(String completionKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
