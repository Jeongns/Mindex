package com.jeongns.mindex.mindexGui.display.strategy;

import com.jeongns.mindex.mindexGui.display.LockedEntryDisplayMode;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

public final class LockedEntryDisplayStrategyFactory {
    private final Map<LockedEntryDisplayMode, LockedEntryDisplayStrategy> strategies;

    public LockedEntryDisplayStrategyFactory() {
        this.strategies = new EnumMap<>(LockedEntryDisplayMode.class);
        register(new FixedItemLockedEntryDisplayStrategy());
        register(new EntryItemCustomModelDataLockedEntryDisplayStrategy());
        register(new FixedItemEntryMaterialStringLockedEntryDisplayStrategy());
    }

    @NonNull
    public LockedEntryDisplayStrategy get(@NonNull LockedEntryDisplayMode mode) {
        LockedEntryDisplayStrategy strategy = strategies.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 잠금 엔트리 표시 모드: " + mode);
        }
        return strategy;
    }

    private void register(@NonNull LockedEntryDisplayStrategy strategy) {
        strategies.put(strategy.mode(), strategy);
    }
}
