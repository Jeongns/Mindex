package com.jeongns.mindex.config;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Set;

public final class YamlNodeReader {
    @NonNull
    private final ConfigurationSection section;
    @NonNull
    private final String path;

    private YamlNodeReader(@NonNull ConfigurationSection section, @NonNull String path) {
        this.section = section;
        this.path = path;
    }

    public static YamlNodeReader root(@NonNull YamlConfiguration config, @NonNull String rootKey) {
        ConfigurationSection rootSection = config.getConfigurationSection(rootKey);
        if (rootSection == null) {
            throw new IllegalArgumentException("필수 섹션 누락: " + rootKey);
        }
        return new YamlNodeReader(rootSection, rootKey);
    }

    public YamlNodeReader child(@NonNull String key) {
        ConfigurationSection child = section.getConfigurationSection(key);
        if (child == null) {
            throw new IllegalArgumentException("필수 섹션 누락: " + pathFor(key));
        }
        return new YamlNodeReader(child, pathFor(key));
    }

    public String getString(@NonNull String key) {
        return section.getString(key);
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return section.getInt(key, defaultValue);
    }

    public List<String> getStringList(@NonNull String key) {
        return List.copyOf(section.getStringList(key));
    }

    public Set<String> keys() {
        return section.getKeys(false);
    }

    public String getPath() {
        return path;
    }

    public String pathForKey(@NonNull String key) {
        return pathFor(key);
    }

    private String pathFor(@NonNull String key) {
        return path + "." + key;
    }
}
