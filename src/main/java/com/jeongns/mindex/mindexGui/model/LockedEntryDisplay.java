package com.jeongns.mindex.mindexGui.model;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

@Getter
public final class LockedEntryDisplay {
    @NonNull
    private final LockedEntryDisplayMode mode;
    @NonNull
    private final Material material;
    private final Integer customModelData;

    public LockedEntryDisplay(
            @NonNull LockedEntryDisplayMode mode,
            @NonNull Material material,
            Integer customModelData
    ) {
        this.mode = mode;
        this.material = material;
        this.customModelData = customModelData;
    }

    public static LockedEntryDisplay defaultValue() {
        return new LockedEntryDisplay(LockedEntryDisplayMode.FIXED_ITEM, Material.GRAY_DYE, null);
    }
}
