package com.jeongns.mindex.catalog.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public final class MindexEntry {
    @NonNull
    private final String id;
    @NonNull
    private final UnlockType unlockType;
    @NonNull
    private final String name;
    @NonNull
    private final String description;
    @NonNull
    private final Material item;
    private final int amount;
    @NonNull
    private final String reward;
}
