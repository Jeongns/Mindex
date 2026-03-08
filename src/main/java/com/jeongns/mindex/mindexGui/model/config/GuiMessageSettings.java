package com.jeongns.mindex.mindexGui.model.config;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GuiMessageSettings {
    @NonNull
    private final String registrationSuccess;
    @NonNull
    private final String registrationAlreadyRegistered;
    @NonNull
    private final String registrationRequirementNotMet;
    @NonNull
    private final String categoryRewardSuccess;
    @NonNull
    private final String categoryRewardNotComplete;
    @NonNull
    private final String categoryRewardAlreadyClaimed;

    public GuiMessageSettings(
            @NonNull String registrationSuccess,
            @NonNull String registrationAlreadyRegistered,
            @NonNull String registrationRequirementNotMet,
            @NonNull String categoryRewardSuccess,
            @NonNull String categoryRewardNotComplete,
            @NonNull String categoryRewardAlreadyClaimed
    ) {
        this.registrationSuccess = registrationSuccess;
        this.registrationAlreadyRegistered = registrationAlreadyRegistered;
        this.registrationRequirementNotMet = registrationRequirementNotMet;
        this.categoryRewardSuccess = categoryRewardSuccess;
        this.categoryRewardNotComplete = categoryRewardNotComplete;
        this.categoryRewardAlreadyClaimed = categoryRewardAlreadyClaimed;
    }

    public static GuiMessageSettings defaultValue() {
        return new GuiMessageSettings(
                "&a도감이 등록되었습니다: %entry_id%",
                "&e이미 등록된 도감입니다.",
                "&c등록 조건을 만족하지 못했습니다.",
                "&a카테고리 완료 보상을 수령했습니다.",
                "&c아직 이 카테고리를 모두 완성하지 못했습니다.",
                "&e이미 이 카테고리 보상을 수령했습니다."
        );
    }
}
