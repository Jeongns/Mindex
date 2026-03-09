package com.jeongns.mindex.mindexGui.interaction;

import com.jeongns.mindex.mindexGui.action.GuiAction;
import com.jeongns.mindex.mindexGui.model.config.GuiMessageSettings;
import com.jeongns.mindex.mindexGui.model.config.GuiSoundSetting;
import com.jeongns.mindex.mindexGui.model.config.GuiSoundSettings;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import com.jeongns.mindex.service.registration.RegistrationService;
import com.jeongns.mindex.service.registration.RegistrationStatus;
import com.jeongns.mindex.service.reward.CategoryRewardService;
import com.jeongns.mindex.service.reward.CategoryRewardStatus;
import com.jeongns.mindex.util.MiniMessageUtil;
import lombok.NonNull;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.logging.Logger;

public final class MindexCatalogGuiInteractionHandler {
    @NonNull
    private final RegistrationService registrationService;
    @NonNull
    private final CategoryRewardService categoryRewardService;
    @NonNull
    private final GuiSoundSettings guiSoundSettings;
    @NonNull
    private final GuiMessageSettings guiMessageSettings;
    @NonNull
    private final Logger logger;

    public MindexCatalogGuiInteractionHandler(
            @NonNull RegistrationService registrationService,
            @NonNull CategoryRewardService categoryRewardService,
            @NonNull GuiSoundSettings guiSoundSettings,
            @NonNull GuiMessageSettings guiMessageSettings,
            @NonNull Logger logger
    ) {
        this.registrationService = registrationService;
        this.categoryRewardService = categoryRewardService;
        this.guiSoundSettings = guiSoundSettings;
        this.guiMessageSettings = guiMessageSettings;
        this.logger = logger;
    }

    public boolean handleClick(
            @NonNull MindexCatalogGui gui,
            @NonNull Player player,
            int rawSlot,
            @NonNull ClickType clickType
    ) {
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return false;
        }
        if (!gui.isOwner(player)) {
            return false;
        }
        if (rawSlot < 0 || rawSlot >= gui.getInventory().getSize()) {
            return false;
        }

        GuiAction action = gui.findAction(rawSlot);
        if (action == null) {
            return false;
        }

        boolean changed = switch (action.type()) {
            case NEXT_PAGE -> gui.moveNextPage();
            case PREV_PAGE -> gui.movePreviousPage();
            case OPEN_DEFAULT -> gui.openDefaultCategory();
            case OPEN_CATEGORY -> gui.openCategory(action.categoryId());
            case REGISTER_ENTRY -> registerEntry(gui, player, action.entryId());
            case CLAIM_CATEGORY_REWARD -> claimCategoryReward(gui, player);
        };

        if (changed
                && action.type() != com.jeongns.mindex.mindexGui.action.ActionType.REGISTER_ENTRY
                && action.type() != com.jeongns.mindex.mindexGui.action.ActionType.CLAIM_CATEGORY_REWARD) {
            playSound(player, guiSoundSettings.getMenuSelect());
        }
        return changed;
    }

    private boolean registerEntry(@NonNull MindexCatalogGui gui, @NonNull Player player, String entryId) {
        if (entryId == null || entryId.isBlank()) {
            return false;
        }

        RegistrationStatus status = registrationService.register(player, entryId);
        return switch (status) {
            case SUCCESS -> {
                gui.refresh();
                playSound(player, guiSoundSettings.getRegistrationSuccess());
                player.sendMessage(MiniMessageUtil.parse(
                        guiMessageSettings.getRegistrationSuccess(),
                        Placeholder.unparsed("entry_id", entryId)
                ));
                yield true;
            }
            case ALREADY_REGISTERED -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(MiniMessageUtil.parse(guiMessageSettings.getRegistrationAlreadyRegistered()));
                yield false;
            }
            case REQUIREMENT_NOT_MET -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(MiniMessageUtil.parse(guiMessageSettings.getRegistrationRequirementNotMet()));
                yield false;
            }
            case ENTRY_NOT_FOUND -> {
                logger.severe("도감 엔트리를 찾을 수 없습니다: entryId=" + entryId);
                yield false;
            }
        };
    }

    private boolean claimCategoryReward(@NonNull MindexCatalogGui gui, @NonNull Player player) {
        String categoryId = gui.getCurrentCategoryId();
        if (categoryId == null || categoryId.isBlank()) {
            logger.severe("카테고리 화면이 아닌 상태에서 카테고리 보상 수령이 호출되었습니다: player="
                    + player.getUniqueId());
            return false;
        }

        CategoryRewardStatus status = categoryRewardService.claim(player, categoryId);
        return switch (status) {
            case SUCCESS -> {
                playSound(player, guiSoundSettings.getRegistrationSuccess());
                player.sendMessage(MiniMessageUtil.parse(guiMessageSettings.getCategoryRewardSuccess()));
                yield true;
            }
            case CATEGORY_NOT_FOUND -> {
                logger.severe("카테고리 보상 수령 대상 카테고리를 찾을 수 없습니다: categoryId=" + categoryId);
                yield false;
            }
            case CATEGORY_NOT_COMPLETE -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(MiniMessageUtil.parse(guiMessageSettings.getCategoryRewardNotComplete()));
                yield false;
            }
            case ALREADY_CLAIMED -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(MiniMessageUtil.parse(guiMessageSettings.getCategoryRewardAlreadyClaimed()));
                yield false;
            }
        };
    }

    private void playSound(@NonNull Player player, @NonNull GuiSoundSetting soundSetting) {
        if (!soundSetting.isEnabled() || soundSetting.getSound() == null) {
            return;
        }
        player.playSound(player.getLocation(), soundSetting.getSound(), soundSetting.getVolume(), soundSetting.getPitch());
    }
}
