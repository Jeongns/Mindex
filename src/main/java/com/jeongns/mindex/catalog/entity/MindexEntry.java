package com.jeongns.mindex.catalog.entity;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

@Getter
public final class MindexEntry {
    @NonNull
    private String id;
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private Material item;
    @NonNull
    private String reward;

    public MindexEntry(@NonNull String id,
                       @NonNull String name,
                       @NonNull String description,
                       @NonNull Material item,
                       @NonNull String reward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.item = item;
        this.reward = reward;
    }
}
