package com.jeongns.mindex.mindexGui.model.display;

import lombok.NonNull;

import java.util.Locale;

public enum LockedEntryDisplayMode {
    FIXED_ITEM,
    ENTRY_ITEM_CUSTOM_MODEL_DATA;

    public static LockedEntryDisplayMode fromConfig(@NonNull String rawValue) {
        String normalized = rawValue.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("mindexGui.locked-entry-display.mode 값이 비어 있습니다.");
        }

        try {
            return LockedEntryDisplayMode.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 잠금 엔트리 표시 모드: " + rawValue, e);
        }
    }
}
