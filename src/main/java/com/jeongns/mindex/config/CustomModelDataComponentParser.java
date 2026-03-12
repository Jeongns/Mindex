package com.jeongns.mindex.config;

import com.jeongns.mindex.item.CustomModelDataComponentUtil;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CustomModelDataComponentParser {
    private CustomModelDataComponentParser() {
    }

    public static CustomModelDataComponent parseSection(ConfigurationSection root, @NonNull String path) {
        if (!root.contains(path)) {
            return null;
        }

        ConfigurationSection section = root.getConfigurationSection(path);
        if (section == null) {
            throw new IllegalArgumentException("custom-model-data는 섹션이어야 합니다: " + path);
        }

        return buildComponent(
                section.getList("floats"),
                section.getList("flags"),
                section.getList("strings"),
                path
        );
    }

    public static CustomModelDataComponent parseMap(Object rawValue, @NonNull String fieldName) {
        if (rawValue == null) {
            return null;
        }
        if (!(rawValue instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("custom-model-data는 객체여야 합니다: " + fieldName);
        }

        return buildComponent(
                listValue(map.get("floats"), fieldName + ".floats"),
                listValue(map.get("flags"), fieldName + ".flags"),
                listValue(map.get("strings"), fieldName + ".strings"),
                fieldName
        );
    }

    private static CustomModelDataComponent buildComponent(
            List<?> rawFloats,
            List<?> rawFlags,
            List<?> rawStrings,
            @NonNull String fieldName
    ) {
        CustomModelDataComponent component = CustomModelDataComponentUtil.create(
                parseFloats(rawFloats, fieldName + ".floats"),
                parseFlags(rawFlags, fieldName + ".flags"),
                parseStrings(rawStrings, fieldName + ".strings")
        );
        return CustomModelDataComponentUtil.isEmpty(component) ? null : component;
    }

    private static List<?> listValue(Object rawValue, @NonNull String fieldName) {
        if (rawValue == null) {
            return List.of();
        }
        if (rawValue instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException("리스트여야 합니다: " + fieldName);
    }

    private static List<Float> parseFloats(List<?> rawValues, @NonNull String fieldName) {
        List<Float> values = new ArrayList<>();
        for (Object rawValue : rawValues) {
            if (rawValue instanceof Number numberValue) {
                values.add(numberValue.floatValue());
                continue;
            }
            throw new IllegalArgumentException("float 리스트여야 합니다: " + fieldName);
        }
        return List.copyOf(values);
    }

    private static List<Boolean> parseFlags(List<?> rawValues, @NonNull String fieldName) {
        List<Boolean> values = new ArrayList<>();
        for (Object rawValue : rawValues) {
            if (rawValue instanceof Boolean booleanValue) {
                values.add(booleanValue);
                continue;
            }
            throw new IllegalArgumentException("boolean 리스트여야 합니다: " + fieldName);
        }
        return List.copyOf(values);
    }

    private static List<String> parseStrings(List<?> rawValues, @NonNull String fieldName) {
        List<String> values = new ArrayList<>();
        for (Object rawValue : rawValues) {
            if (rawValue instanceof String stringValue) {
                values.add(stringValue);
                continue;
            }
            throw new IllegalArgumentException("문자열 리스트여야 합니다: " + fieldName);
        }
        return List.copyOf(values);
    }
}
