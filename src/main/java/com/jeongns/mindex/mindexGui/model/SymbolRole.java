package com.jeongns.mindex.mindexGui.model;

import lombok.NonNull;

import java.util.Locale;

public enum SymbolRole {
    BORDER,
    ENTRY_SLOT,
    PREV_PAGE,
    NEXT_PAGE,
    OPEN_DEFAULT,
    CLAIM_CATEGORY_REWARD,
    CATEGORY_BUTTON;

    public static SymbolRole fromConfig(@NonNull String value) {
        try {
            return SymbolRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("유효하지 않은 GUI role: " + value, exception);
        }
    }
}
