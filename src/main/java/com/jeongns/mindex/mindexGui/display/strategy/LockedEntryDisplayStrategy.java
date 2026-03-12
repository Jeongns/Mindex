package com.jeongns.mindex.mindexGui.display.strategy;

import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplayMode;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public interface LockedEntryDisplayStrategy {
    @NonNull
    LockedEntryDisplayMode mode();

    @NonNull
    Material resolveMaterial(@NonNull MindexEntry entry, @NonNull LockedEntryDisplay lockedEntryDisplay);

    @NonNull
    String resolveName(@NonNull MindexEntry entry, @NonNull LockedEntryDisplay lockedEntryDisplay);

    CustomModelDataComponent resolveCustomModelData(
            @NonNull MindexEntry entry,
            @NonNull LockedEntryDisplay lockedEntryDisplay
    );
}
