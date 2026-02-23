package com.jeongns.mindex.catalog.entity;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public final class MindexCategory {
    @NonNull
    private final String id;
    @NonNull
    private final String categoryName;
    @NonNull
    private final String reward;
    @NonNull
    private final List<MindexEntry> entries;

    public MindexCategory(
            @NonNull String id,
            @NonNull String categoryName,
            @NonNull String reward,
            @NonNull List<MindexEntry> entries
    ) {
        this.id = id;
        this.categoryName = categoryName;
        this.reward = reward;
        this.entries = entries;
    }
}
