package com.jeongns.mindex.domain;

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
}

