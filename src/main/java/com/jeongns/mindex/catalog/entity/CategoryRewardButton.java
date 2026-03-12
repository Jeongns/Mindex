package com.jeongns.mindex.catalog.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

@Getter
@AllArgsConstructor
public final class CategoryRewardButton {
    @NonNull
    private final Material material;
    private final CustomModelDataComponent customModelData;
    @NonNull
    private final String name;
    @NonNull
    private final List<String> lore;
}
