package com.jeongns.mindex.mindexGui.interaction;

import com.jeongns.mindex.mindexGui.action.GuiAction;
import com.jeongns.mindex.mindexGui.model.GuiSoundSetting;
import com.jeongns.mindex.mindexGui.model.GuiSoundSettings;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import com.jeongns.mindex.service.registration.RegistrationService;
import com.jeongns.mindex.service.registration.RegistrationStatus;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class MindexCatalogGuiInteractionHandler {
    @NonNull
    private final RegistrationService registrationService;
    @NonNull
    private final GuiSoundSettings guiSoundSettings;

    public MindexCatalogGuiInteractionHandler(
            @NonNull RegistrationService registrationService,
            @NonNull GuiSoundSettings guiSoundSettings
    ) {
        this.registrationService = registrationService;
        this.guiSoundSettings = guiSoundSettings;
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
        };

        if (changed && action.type() != com.jeongns.mindex.mindexGui.action.ActionType.REGISTER_ENTRY) {
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
                player.sendMessage(colorize("&a도감이 등록되었습니다: " + entryId));
                yield true;
            }
            case ALREADY_REGISTERED -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(colorize("&e이미 등록된 도감입니다."));
                yield false;
            }
            case REQUIREMENT_NOT_MET -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(colorize("&c등록 조건을 만족하지 못했습니다."));
                yield false;
            }
            case ENTRY_NOT_FOUND -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(colorize("&c존재하지 않는 도감 엔트리입니다."));
                yield false;
            }
            case UNSUPPORTED_UNLOCK_TYPE -> {
                playSound(player, guiSoundSettings.getRegistrationFail());
                player.sendMessage(colorize("&c지원하지 않는 등록 타입입니다."));
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

    private String colorize(@NonNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
