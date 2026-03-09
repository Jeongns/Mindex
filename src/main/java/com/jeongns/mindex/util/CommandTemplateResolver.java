package com.jeongns.mindex.util;

import lombok.NonNull;

import java.util.Map;

public final class CommandTemplateResolver {
    private CommandTemplateResolver() {
    }

    public static String resolve(@NonNull String template, @NonNull Map<String, String> values) {
        String resolved = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            resolved = resolved.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return resolved;
    }
}
