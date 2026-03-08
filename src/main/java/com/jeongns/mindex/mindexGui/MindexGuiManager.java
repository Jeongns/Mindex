package com.jeongns.mindex.mindexGui;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.mindexGui.loader.GuiConfigLoader;
import com.jeongns.mindex.mindexGui.model.config.GuiMessageSettings;
import com.jeongns.mindex.mindexGui.model.config.GuiSettings;
import com.jeongns.mindex.mindexGui.model.layout.GuiModel;
import com.jeongns.mindex.mindexGui.model.config.GuiSoundSettings;
import com.jeongns.mindex.mindexGui.model.display.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.service.registration.RegistrationService;
import com.jeongns.mindex.service.reward.CategoryRewardService;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class MindexGuiManager implements Manager {
    @NonNull
    private final JavaPlugin plugin;
    @NonNull
    private final CatalogManager catalogManager;
    @NonNull
    private final PlayerStateManager playerStateManager;
    @NonNull
    private final RegistrationService registrationService;
    @NonNull
    private final CategoryRewardService categoryRewardService;
    @NonNull
    private final GuiConfigLoader configLoader;

    @Getter
    @NonNull
    private GuiModel guiModel;
    @Getter
    @NonNull
    private LockedEntryDisplay lockedEntryDisplay;
    @Getter
    @NonNull
    private GuiSoundSettings guiSoundSettings;
    @Getter
    @NonNull
    private GuiMessageSettings guiMessageSettings;

    public MindexGuiManager(
            @NonNull JavaPlugin plugin,
            @NonNull CatalogManager catalogManager,
            @NonNull PlayerStateManager playerStateManager,
            @NonNull RegistrationService registrationService,
            @NonNull CategoryRewardService categoryRewardService
    ) {
        this.plugin = plugin;
        this.catalogManager = catalogManager;
        this.playerStateManager = playerStateManager;
        this.registrationService = registrationService;
        this.categoryRewardService = categoryRewardService;
        this.configLoader = new GuiConfigLoader(plugin);
        this.guiModel = GuiModel.empty();
        this.lockedEntryDisplay = LockedEntryDisplay.defaultValue();
        this.guiSoundSettings = GuiSoundSettings.defaultValue();
        this.guiMessageSettings = GuiMessageSettings.defaultValue();
    }

    @Override
    public void initialize() {
        reload();
    }

    @Override
    public void reload() {
        applyGuiSettings(configLoader.load());
    }

    public void applyGuiModel(@NonNull GuiModel guiModel) {
        this.guiModel = guiModel;
    }

    public void applyLockedEntryDisplay(@NonNull LockedEntryDisplay lockedEntryDisplay) {
        this.lockedEntryDisplay = lockedEntryDisplay;
    }

    public void applyGuiSoundSettings(@NonNull GuiSoundSettings guiSoundSettings) {
        this.guiSoundSettings = guiSoundSettings;
    }

    public void applyGuiMessageSettings(@NonNull GuiMessageSettings guiMessageSettings) {
        this.guiMessageSettings = guiMessageSettings;
    }

    public void applyGuiSettings(@NonNull GuiSettings guiSettings) {
        validateCategorySymbols(guiSettings.getGuiModel());
        applyGuiModel(guiSettings.getGuiModel());
        applyLockedEntryDisplay(guiSettings.getLockedEntryDisplay());
        applyGuiSoundSettings(guiSettings.getGuiSoundSettings());
        applyGuiMessageSettings(guiSettings.getGuiMessageSettings());
    }

    public void openDefault(@NonNull Player player) {
        new MindexCatalogGui(
                player.getUniqueId(),
                catalogManager.getCatalog(),
                guiModel,
                lockedEntryDisplay,
                guiSoundSettings,
                guiMessageSettings,
                playerStateManager,
                plugin.getLogger(),
                registrationService,
                categoryRewardService
        ).open(player);
    }

    public void openCategory(@NonNull Player player, @NonNull String categoryId) {
        MindexCatalogGui gui = new MindexCatalogGui(
                player.getUniqueId(),
                catalogManager.getCatalog(),
                guiModel,
                lockedEntryDisplay,
                guiSoundSettings,
                guiMessageSettings,
                playerStateManager,
                plugin.getLogger(),
                registrationService,
                categoryRewardService
        );
        gui.setCategory(categoryId);
        gui.open(player);
    }

    public void handleOpen(@NonNull Player player, @NonNull MindexCatalogGui gui) {
        // Inventory open hook for GUI session lifecycle.
    }

    public void handleTopInventoryClick(
            @NonNull Player player,
            @NonNull MindexCatalogGui gui,
            int rawSlot,
            @NonNull ClickType clickType
    ) {
        gui.handleClick(player, rawSlot, clickType);
    }

    public void handleClose(@NonNull Player player, @NonNull MindexCatalogGui gui) {
        // Inventory close hook for GUI session lifecycle.
    }

    private void validateCategorySymbols(@NonNull GuiModel guiModel) {
        Set<String> categoryIds = new HashSet<>();
        catalogManager.getCategories().forEach(category -> categoryIds.add(category.getId().toLowerCase()));

        guiModel.getCategorySymbols().values().forEach(symbol -> {
            if (!categoryIds.contains(symbol.getCategoryId().toLowerCase())) {
                throw new IllegalArgumentException("존재하지 않는 categoryId를 참조하는 categorySymbol입니다: "
                        + symbol.getCategoryId());
            }
        });
    }

    @Override
    public void shutdown() {
        // no-op
    }
}
