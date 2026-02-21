package com.jeongns.mindex.catalog.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public final class MindexEntry {
    @NonNull
    private String id;
    @NonNull
    private String category;
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private Material item;
    private int sortOrder;
}
