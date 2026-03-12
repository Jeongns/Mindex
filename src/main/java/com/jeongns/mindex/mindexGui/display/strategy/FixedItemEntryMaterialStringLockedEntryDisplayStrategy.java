package com.jeongns.mindex.mindexGui.display.strategy;

import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.item.CustomModelDataComponentUtil;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplayMode;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;
import java.util.Locale;

public final class FixedItemEntryMaterialStringLockedEntryDisplayStrategy implements LockedEntryDisplayStrategy {
    @Override
    @NonNull
    public LockedEntryDisplayMode mode() {
        return LockedEntryDisplayMode.FIXED_ITEM_ENTRY_MATERIAL_STRING;
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
        String materialKey = entry.getItem().name().toLowerCase(Locale.ROOT);
        String prefix = lockedEntryDisplay.getStringPrefix() == null ? "" : lockedEntryDisplay.getStringPrefix();
        String modelKey = prefix + materialKey;
        return CustomModelDataComponentUtil.withStrings(
                lockedEntryDisplay.getCustomModelData(),
                List.of(modelKey)
        );
    }
}
