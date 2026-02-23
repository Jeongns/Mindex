package com.jeongns.mindex.catalog;

import com.jeongns.mindex.catalog.entity.MindexCatalog;
import com.jeongns.mindex.catalog.entity.MindexCategory;
import com.jeongns.mindex.manager.Manager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class CatalogManager implements Manager {
    @Getter
    @Setter
    private MindexCatalog catalog = new MindexCatalog(List.of());

    @Override
    public void initialize() {
    }

    @Override
    public void reload() {
    }

    public List<MindexCategory> getCategories() {
        return catalog.getCategories();
    }
}
