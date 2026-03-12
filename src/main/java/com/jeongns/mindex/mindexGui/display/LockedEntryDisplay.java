package com.jeongns.mindex.mindexGui.display;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

@Getter
public final class LockedEntryDisplay {
    @NonNull
    private final LockedEntryDisplayMode mode;
    @NonNull
    private final Material material;
    private final CustomModelDataComponent customModelData;
    private final String name;
    private final String stringPrefix;

    public LockedEntryDisplay(
            @NonNull LockedEntryDisplayMode mode,
            @NonNull Material material,
            CustomModelDataComponent customModelData,
            String name,
            String stringPrefix
    ) {
        this.mode = mode;
        this.material = material;
        this.customModelData = customModelData;
        this.name = name;
        this.stringPrefix = stringPrefix;
    }

    public static LockedEntryDisplay defaultValue() {
        return new LockedEntryDisplay(LockedEntryDisplayMode.FIXED_ITEM, Material.GRAY_DYE, null, null, null);
    }
}
