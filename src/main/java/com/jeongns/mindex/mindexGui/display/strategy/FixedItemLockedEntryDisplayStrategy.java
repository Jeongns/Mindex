package com.jeongns.mindex.mindexGui.display.strategy;

import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplayMode;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public final class FixedItemLockedEntryDisplayStrategy implements LockedEntryDisplayStrategy {
    @Override
    @NonNull
    public LockedEntryDisplayMode mode() {
        return LockedEntryDisplayMode.FIXED_ITEM;
    }

    @Override
    @NonNull
    public Material resolveMaterial(@NonNull MindexEntry entry, @NonNull LockedEntryDisplay lockedEntryDisplay) {
        return lockedEntryDisplay.getMaterial();
    }

    @Override
    @NonNull
    public String resolveName(@NonNull MindexEntry entry, @NonNull LockedEntryDisplay lockedEntryDisplay) {
        if (lockedEntryDisplay.getName() == null || lockedEntryDisplay.getName().isBlank()) {
            return entry.getName();
        }
        return lockedEntryDisplay.getName();
    }

    @Override
    public CustomModelDataComponent resolveCustomModelData(
            @NonNull MindexEntry entry,
            @NonNull LockedEntryDisplay lockedEntryDisplay
    ) {
        return lockedEntryDisplay.getCustomModelData();
    }
}
