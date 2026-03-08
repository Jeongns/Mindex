package com.jeongns.mindex.mindexGui.model.config;

import com.jeongns.mindex.mindexGui.model.display.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.model.layout.GuiModel;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GuiSettings {
    @NonNull
    private final GuiModel guiModel;
    @NonNull
    private final LockedEntryDisplay lockedEntryDisplay;
    @NonNull
    private final GuiSoundSettings guiSoundSettings;
    @NonNull
    private final GuiMessageSettings guiMessageSettings;

    public GuiSettings(
            @NonNull GuiModel guiModel,
            @NonNull LockedEntryDisplay lockedEntryDisplay,
            @NonNull GuiSoundSettings guiSoundSettings,
            @NonNull GuiMessageSettings guiMessageSettings
    ) {
        this.guiModel = guiModel;
        this.lockedEntryDisplay = lockedEntryDisplay;
        this.guiSoundSettings = guiSoundSettings;
        this.guiMessageSettings = guiMessageSettings;
    }

    public static GuiSettings defaultValue() {
        return new GuiSettings(
                GuiModel.empty(),
                LockedEntryDisplay.defaultValue(),
                GuiSoundSettings.defaultValue(),
                GuiMessageSettings.defaultValue()
        );
    }
}
