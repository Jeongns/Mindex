package com.jeongns.mindex.mindexGui.model.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

@Getter
public final class GuiSoundSettings {
    @NonNull
    private final GuiSoundSetting menuSelect;
    @NonNull
    private final GuiSoundSetting registrationSuccess;
    @NonNull
    private final GuiSoundSetting registrationFail;

    public GuiSoundSettings(
            @NonNull GuiSoundSetting menuSelect,
            @NonNull GuiSoundSetting registrationSuccess,
            @NonNull GuiSoundSetting registrationFail
    ) {
        this.menuSelect = menuSelect;
        this.registrationSuccess = registrationSuccess;
        this.registrationFail = registrationFail;
    }

    public static GuiSoundSettings defaultValue() {
        return new GuiSoundSettings(
                GuiSoundSetting.of(defaultSound("minecraft:ui.button.click"), 1.0f, 1.0f),
                GuiSoundSetting.of(defaultSound("minecraft:entity.player.levelup"), 1.0f, 1.2f),
                GuiSoundSetting.of(defaultSound("minecraft:entity.villager.no"), 1.0f, 1.0f)
        );
    }

    private static Sound defaultSound(@NonNull String keyString) {
        NamespacedKey key = NamespacedKey.fromString(keyString);
        if (key == null) {
            throw new IllegalArgumentException("유효하지 않은 기본 사운드 키: " + keyString);
        }

        Sound sound = Registry.SOUND_EVENT.get(key);
        if (sound == null) {
            throw new IllegalArgumentException("등록되지 않은 기본 사운드 키: " + keyString);
        }
        return sound;
    }
}
