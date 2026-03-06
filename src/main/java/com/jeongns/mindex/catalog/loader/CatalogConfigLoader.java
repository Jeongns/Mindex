package com.jeongns.mindex.catalog.loader;

import com.jeongns.mindex.catalog.entity.CategoryRewardButton;
import com.jeongns.mindex.catalog.entity.MindexCategory;
import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.catalog.entity.UnlockType;
import com.jeongns.mindex.config.validation.ConfigValueValidator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CatalogConfigLoader {
    private static final String CATEGORY_DIRECTORY = "categories";
    private static final String ENTRY_LIST_PATH = "entries";
    private static final List<String> DEFAULT_CATEGORY_RESOURCES = List.of(
            "categories/mining.yml",
            "categories/fishing.yml"
    );

    @NonNull
    private final JavaPlugin plugin;

    public List<MindexCategory> loadCategories() {
        ensureCategoryDirectory();

        File[] categoryFiles = new File(plugin.getDataFolder(), CATEGORY_DIRECTORY)
                .listFiles((dir, name) -> name.endsWith(".yml"));
        if (categoryFiles == null) {
            return List.of();
        }

        List<MindexCategory> categories = Arrays.stream(categoryFiles)
                .sorted(Comparator.comparing(File::getName))
                .map(this::loadCategory)
                .collect(Collectors.toList());

        validateUniqueCategoryIds(categories);
        validateUniqueEntryIds(categories);
        return categories;
    }

    private MindexCategory loadCategory(@NonNull File categoryFile) {
        YamlConfiguration categoryConfig = YamlConfiguration.loadConfiguration(categoryFile);
        String categoryId = ConfigValueValidator.requireString(categoryConfig.getString("id"), categoryFile.getName() + ".id");
        String categoryName = ConfigValueValidator.requireString(categoryConfig.getString("name"), categoryFile.getName() + ".name");
        List<String> categoryReward = parseRewardCommands(categoryConfig.get("reward"), categoryFile.getName() + ".reward");
        CategoryRewardButton rewardButton = loadRewardButton(categoryConfig, categoryFile.getName());
        List<MindexEntry> entries = loadEntries(categoryId, categoryConfig);

        return new MindexCategory(categoryId, categoryName, categoryReward, rewardButton, entries);
    }

    private CategoryRewardButton loadRewardButton(@NonNull YamlConfiguration categoryConfig, @NonNull String fileName) {
        String path = "rewardButton";
        String materialName = ConfigValueValidator.requireString(
                categoryConfig.getString(path + ".material"),
                fileName + "." + path + ".material"
        );
        String name = ConfigValueValidator.requireString(
                categoryConfig.getString(path + ".name"),
                fileName + "." + path + ".name"
        );
        List<String> lore = categoryConfig.getStringList(path + ".lore");

        return new CategoryRewardButton(
                ConfigValueValidator.parseMaterial(materialName),
                name,
                lore
        );
    }

    private void ensureCategoryDirectory() {
        File directory = new File(plugin.getDataFolder(), CATEGORY_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File[] categoryFiles = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (categoryFiles != null && categoryFiles.length > 0) {
            return;
        }

        for (String resourcePath : DEFAULT_CATEGORY_RESOURCES) {
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false);
            }
        }
    }

    private List<MindexEntry> loadEntries(@NonNull String categoryId, @NonNull YamlConfiguration categoryConfig) {
        return categoryConfig.getMapList(ENTRY_LIST_PATH).stream()
                .map(row -> toEntry(categoryId, row))
                .collect(Collectors.toList());
    }

    private void validateUniqueCategoryIds(@NonNull List<MindexCategory> categories) {
        Set<String> seen = new HashSet<>();
        for (MindexCategory category : categories) {
            String normalized = category.getId().toLowerCase(Locale.ROOT);
            if (!seen.add(normalized)) {
                throw new IllegalArgumentException("중복 category.id 발견: " + category.getId());
            }
        }
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
                parseOptionalPositiveInt(row.get("customModelData"), "entries.customModelData"),
                parsePositiveAmount(row.get("amount"), "entries.amount", 1),
                parseRewardCommands(row.get("reward"), "entries.reward")
        );
    }

    private List<String> parseRewardCommands(Object rawValue, @NonNull String fieldName) {
        if (rawValue == null) {
            return List.of();
        }

        if (rawValue instanceof String stringValue) {
            String value = ConfigValueValidator.optionalString(stringValue, "");
            return value.isBlank() ? List.of() : List.of(value);
        }

        if (rawValue instanceof List<?> rawList) {
            return rawList.stream()
                    .map(value -> {
                        if (!(value instanceof String stringValue)) {
                            throw new IllegalArgumentException("reward는 문자열 또는 문자열 리스트여야 합니다: " + fieldName);
                        }
                        return ConfigValueValidator.optionalString(stringValue, "");
                    })
                    .filter(value -> !value.isBlank())
                    .toList();
        }

        throw new IllegalArgumentException("reward는 문자열 또는 문자열 리스트여야 합니다: " + fieldName);
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

    private Integer parseOptionalPositiveInt(Object rawValue, @NonNull String fieldName) {
        if (rawValue == null) {
            return null;
        }

        int value;
        if (rawValue instanceof Number numberValue) {
            value = numberValue.intValue();
        } else if (rawValue instanceof String stringValue) {
            String trimmed = stringValue.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                value = Integer.parseInt(trimmed);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("유효하지 않은 숫자: " + fieldName + "=" + rawValue, e);
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 숫자 타입: " + fieldName + "=" + rawValue);
        }

        if (value < 0) {
            throw new IllegalArgumentException("0 이상이어야 합니다: " + fieldName + "=" + value);
        }
        return value;
    }
}
