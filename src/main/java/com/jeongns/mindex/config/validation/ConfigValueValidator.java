package com.jeongns.mindex.config.validation;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Locale;

public final class ConfigValueValidator {
    private ConfigValueValidator() {
    }

    public static String requireString(String value, @NonNull String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("필수 문자열 누락: " + fieldName);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("필수 문자열 누락: " + fieldName);
        }
        return trimmed;
    }

    public static String optionalString(String value, @NonNull String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    public static Material parseMaterial(@NonNull String materialName) {
        String normalized = requireString(materialName, "material").toUpperCase(Locale.ROOT);
        Material material = Material.getMaterial(normalized);
        if (material == null) {
            throw new IllegalArgumentException("유효하지 않은 material: " + materialName);
        }
        return material;
    }

    public static Material optionalMaterial(String value, @NonNull String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return parseMaterial(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 material: " + fieldName + "=" + value, e);
        }
    }
}
