package com.jeongns.mindex.util;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

public final class MiniMessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MiniMessageUtil() {
    }

    public static Component parse(@NonNull String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    public static Component parse(@NonNull String text, @NonNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(text, resolvers);
    }

    public static List<Component> parse(@NonNull List<String> texts) {
        return texts.stream()
                .map(MiniMessageUtil::parse)
                .toList();
    }

    public static List<Component> parse(@NonNull List<String> texts, @NonNull TagResolver... resolvers) {
        return texts.stream()
                .map(text -> parse(text, resolvers))
                .toList();
    }
}
