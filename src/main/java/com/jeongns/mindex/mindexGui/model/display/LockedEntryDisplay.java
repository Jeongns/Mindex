package com.jeongns.mindex.mindexGui.model.display;

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

    public LockedEntryDisplay(
            @NonNull LockedEntryDisplayMode mode,
            @NonNull Material material,
            CustomModelDataComponent customModelData,
            String name
    ) {
        this.mode = mode;
        this.material = material;
        this.customModelData = customModelData;
        this.name = name;
    }

    public static LockedEntryDisplay defaultValue() {
        return new LockedEntryDisplay(LockedEntryDisplayMode.FIXED_ITEM, Material.GRAY_DYE, null, null);
    }
}
