package com.jeongns.mindex.catalog.loader;

import com.jeongns.mindex.catalog.entity.CategoryRewardButton;
import com.jeongns.mindex.catalog.entity.MindexCategory;
import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.config.CustomModelDataComponentParser;
import com.jeongns.mindex.config.validation.ConfigValueValidator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CatalogConfigLoader {
    private static final String CATEGORY_DIRECTORY = "categories";
    private static final String ENTRY_LIST_PATH = "entries";
    private static final List<String> DEFAULT_CATEGORY_RESOURCES = List.of(
            "categories/mining.yml",
            "categories/fishing.yml",
            "categories/farming.yml"
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
        CategoryRewardButton rewardButton = loadRequiredRewardButton(categoryConfig, categoryFile.getName());
        CategoryRewardButton claimedRewardButton = loadRequiredClaimedRewardButton(categoryConfig, categoryFile.getName());
        List<MindexEntry> entries = loadEntries(categoryId, categoryConfig);

        return new MindexCategory(categoryId, categoryName, categoryReward, rewardButton, claimedRewardButton, entries);
    }

    private CategoryRewardButton loadRequiredRewardButton(
            @NonNull YamlConfiguration categoryConfig,
            @NonNull String fileName
    ) {
        return loadRewardButtonAtPath(categoryConfig, fileName, "rewardButton", true);
    }

    private CategoryRewardButton loadRequiredClaimedRewardButton(
            @NonNull YamlConfiguration categoryConfig,
            @NonNull String fileName
    ) {
        return loadRewardButtonAtPath(categoryConfig, fileName, "claimedRewardButton", true);
    }

    private CategoryRewardButton loadRewardButtonAtPath(
            @NonNull YamlConfiguration categoryConfig,
            @NonNull String fileName,
            @NonNull String path,
            boolean required
    ) {
        if (!categoryConfig.contains(path)) {
            if (required) {
                throw new IllegalArgumentException("필수 설정이 누락되었습니다: " + fileName + "." + path);
            }
            return null;
        }

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
                CustomModelDataComponentParser.parseSection(categoryConfig, path + ".custom-model-data"),
                name,
                lore
        );
    }

    private void ensureCategoryDirectory() {
        File directory = new File(plugin.getDataFolder(), CATEGORY_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (String resourcePath : DEFAULT_CATEGORY_RESOURCES) {
            File targetFile = new File(plugin.getDataFolder(), resourcePath);
            if (!targetFile.exists() && plugin.getResource(resourcePath) != null) {
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
        String configuredId = ConfigValueValidator.requireString(valueAsString(row.get("id")), "entries.id");
        String entryId = buildEntryId(categoryId, configuredId);

        return new MindexEntry(
                entryId,
                ConfigValueValidator.requireString(valueAsString(row.get("name")), "entries.name"),
                ConfigValueValidator.requireString(valueAsString(row.get("description")), "entries.description"),
                material,
                CustomModelDataComponentParser.parseMap(row.get("custom-model-data"), "entries.custom-model-data"),
                parsePositiveAmount(row.get("amount"), "entries.amount", 1),
                parseRewardCommands(row.get("reward"), "entries.reward")
        );
    }

    private String buildEntryId(@NonNull String categoryId, @NonNull String configuredId) {
        return categoryId + "." + configuredId.toLowerCase(Locale.ROOT);
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
}
