package com.jeongns.mindex.catalog.loader;

import com.jeongns.mindex.catalog.entity.MindexCategory;
import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.catalog.entity.UnlockType;
import com.jeongns.mindex.config.validation.ConfigValueValidator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CatalogConfigLoader {
    private static final String CATEGORY_LIST_PATH = "catalog.categories";
    private static final String ENTRY_LIST_PATH = "entries";

    @NonNull
    private final JavaPlugin plugin;

    public List<MindexCategory> loadCategories() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        List<MindexCategory> categories = plugin.getConfig().getMapList(CATEGORY_LIST_PATH).stream()
                .map(this::toCategory)
                .collect(Collectors.toList());

        validateUniqueEntryIds(categories);
        return categories;
    }

    private MindexCategory toCategory(@NonNull Map<?, ?> row) {
        String categoryId = ConfigValueValidator.requireString(valueAsString(row.get("id")), "catalog.categories.id");
        String categoryName = ConfigValueValidator.requireString(valueAsString(row.get("name")), "catalog.categories.name");
        String categoryFilePath = ConfigValueValidator.requireString(valueAsString(row.get("file")), "catalog.categories.file");
        String categoryReward = ConfigValueValidator.optionalString(valueAsString(row.get("reward")), "");

        ensureCategoryFile(categoryFilePath);
        List<MindexEntry> entries = loadEntries(categoryId, categoryFilePath);

        return new MindexCategory(categoryId, categoryName, categoryReward, entries);
    }

    private void ensureCategoryFile(@NonNull String categoryFilePath) {
        File categoryFile = new File(plugin.getDataFolder(), categoryFilePath);
        if (!categoryFile.exists()) {
            plugin.saveResource(categoryFilePath, false);
        }
    }

    private List<MindexEntry> loadEntries(@NonNull String categoryId, @NonNull String categoryFilePath) {
        File categoryFile = new File(plugin.getDataFolder(), categoryFilePath);
        YamlConfiguration categoryConfig = YamlConfiguration.loadConfiguration(categoryFile);

        return categoryConfig.getMapList(ENTRY_LIST_PATH).stream()
                .map(row -> toEntry(categoryId, row))
                .collect(Collectors.toList());
    }

    private MindexEntry toEntry(@NonNull String categoryId, @NonNull Map<?, ?> row) {
        String materialName = ConfigValueValidator.requireString(valueAsString(row.get("material")), "entries.material");
        var material = ConfigValueValidator.parseMaterial(materialName);
        String configuredId = ConfigValueValidator.optionalString(valueAsString(row.get("id")), "");
        String entryId = configuredId.isEmpty() ? categoryId + "." + material.name() : configuredId;

        return new MindexEntry(
                entryId,
                UnlockType.fromConfig(
                        ConfigValueValidator.requireString(
                                valueAsString(row.get("unlockType")),
                                "entries.unlockType"
                        )
                ),
                ConfigValueValidator.requireString(valueAsString(row.get("name")), "entries.name"),
                ConfigValueValidator.requireString(valueAsString(row.get("description")), "entries.description"),
                material,
                parsePositiveAmount(row.get("amount"), "entries.amount", 1),
                ConfigValueValidator.optionalString(valueAsString(row.get("reward")), "")
        );
    }

    private void validateUniqueEntryIds(@NonNull List<MindexCategory> categories) {
        Set<String> seen = new HashSet<>();
        for (MindexCategory category : categories) {
            for (MindexEntry entry : category.getEntries()) {
                String normalized = entry.getId().toLowerCase(Locale.ROOT);
                if (!seen.add(normalized)) {
                    throw new IllegalArgumentException("중복 entries.id 발견: " + entry.getId());
                }
            }
        }
    }

    private String valueAsString(Object value) {
        return value instanceof String stringValue ? stringValue : null;
    }

    private int parsePositiveAmount(Object rawValue, @NonNull String fieldName, int defaultValue) {
        if (rawValue == null) {
            return defaultValue;
        }

        int amount;
        if (rawValue instanceof Number numberValue) {
            amount = numberValue.intValue();
        } else if (rawValue instanceof String stringValue) {
            String trimmed = stringValue.trim();
            if (trimmed.isEmpty()) {
                return defaultValue;
            }
            try {
                amount = Integer.parseInt(trimmed);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("유효하지 않은 숫자: " + fieldName + "=" + rawValue, e);
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 숫자 타입: " + fieldName + "=" + rawValue);
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("amount는 1 이상이어야 합니다: " + fieldName + "=" + amount);
        }
        return amount;
    }
}
