package com.jeongns.mindex.catalog.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@AllArgsConstructor
public final class MindexCategory {
    @NonNull
    private final String id;
    @NonNull
    private final String categoryName;
    @NonNull
    private final List<String> reward;
    @NonNull
    private final CategoryRewardButton rewardButton;
    @NonNull
    private final List<MindexEntry> entries;
}
