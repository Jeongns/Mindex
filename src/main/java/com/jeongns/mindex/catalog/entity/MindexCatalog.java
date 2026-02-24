package com.jeongns.mindex.catalog.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@AllArgsConstructor
public final class MindexCatalog {
    @NonNull
    private final List<MindexCategory> categories;
}
