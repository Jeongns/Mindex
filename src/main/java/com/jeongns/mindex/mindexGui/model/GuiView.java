package com.jeongns.mindex.mindexGui.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@AllArgsConstructor
public final class GuiView {
    @NonNull
    private final String title;
    private final int rows;
    @NonNull
    private final List<String> layout;
}
