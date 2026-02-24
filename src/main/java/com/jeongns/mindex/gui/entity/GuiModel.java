package com.jeongns.mindex.gui.entity;

import com.jeongns.mindex.gui.entity.model.CategorySymbol;
import com.jeongns.mindex.gui.entity.model.DefaultSymbol;
import com.jeongns.mindex.gui.entity.model.GuiView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public final class GuiModel {
    @NonNull
    private final Map<Character, DefaultSymbol> defaultSymbols;
    @NonNull
    private final Map<Character, CategorySymbol> categorySymbols;
    @NonNull
    private final GuiView defaultView;
    @NonNull
    private final GuiView entryView;

    public static GuiModel empty() {
        GuiView emptyView = new GuiView("", 0, List.of());
        return new GuiModel(Map.of(), Map.of(), emptyView, emptyView);
    }
}
