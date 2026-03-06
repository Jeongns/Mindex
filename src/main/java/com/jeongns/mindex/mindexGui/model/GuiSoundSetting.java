package com.jeongns.mindex.mindexGui.model;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;
import org.jspecify.annotations.Nullable;

@Getter
public final class GuiSoundSetting {
    private final boolean enabled;
    @Nullable
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public GuiSoundSetting(boolean enabled, @Nullable Sound sound, float volume, float pitch) {
        this.enabled = enabled;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static GuiSoundSetting disabled() {
        return new GuiSoundSetting(false, null, 1.0f, 1.0f);
    }

    public static GuiSoundSetting of(@NonNull Sound sound, float volume, float pitch) {
        return new GuiSoundSetting(true, sound, volume, pitch);
    }
}
