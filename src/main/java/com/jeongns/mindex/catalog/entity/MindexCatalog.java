package com.jeongns.mindex.catalog.entity;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public final class MindexCatalog {
    @NonNull
    private final List<MindexCategory> categories;

    public MindexCatalog(@NonNull List<MindexCategory> categories) {
        this.categories = categories;
    }
}
