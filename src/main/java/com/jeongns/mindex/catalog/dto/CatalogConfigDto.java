package com.jeongns.mindex.catalog.dto;

import java.util.List;

public class CatalogConfigDto {
    public int schemaVersion;
    public CatalogNode catalog;

    public static class CatalogNode {
        public Boolean validateOnLoad;
        public List<CategoryRef> categories;
    }

    public static class CategoryRef {
        public String id;
        public String name;
        public String file;
        public String reward;
    }

    public static class CategoryEntries {
        public List<Entry> entries;
    }

    public static class Entry {
        public String id;
        public String name;
        public String description;
        public String material;
        public String reward;
    }
}
