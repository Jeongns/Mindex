package com.jeongns.mindex.mindexGui.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

import java.util.List;

@Getter
@AllArgsConstructor
public final class CategorySymbol {
    private final char symbol;
    @NonNull
    private final SymbolRole role;
    @NonNull
    private final String categoryId;
    private final Material material;
    private final String name;
    @NonNull
    private final List<String> lore;
}
