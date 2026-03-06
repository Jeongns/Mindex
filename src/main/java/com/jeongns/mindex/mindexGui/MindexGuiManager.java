package com.jeongns.mindex.mindexGui;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.mindexGui.loader.GuiConfigLoader;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.model.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.model.LockedEntryDisplayMode;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.service.registration.RegistrationService;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.java.JavaPlugin;

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
    private final GuiConfigLoader configLoader;

    @Getter
    @NonNull
    private GuiModel guiModel;
    @Getter
    @NonNull
    private LockedEntryDisplay lockedEntryDisplay;

    public MindexGuiManager(
            @NonNull JavaPlugin plugin,
            @NonNull CatalogManager catalogManager,
            @NonNull PlayerStateManager playerStateManager,
            @NonNull RegistrationService registrationService
    ) {
        this.plugin = plugin;
        this.catalogManager = catalogManager;
        this.playerStateManager = playerStateManager;
        this.registrationService = registrationService;
        this.configLoader = new GuiConfigLoader(plugin);
        this.guiModel = GuiModel.empty();
        this.lockedEntryDisplay = LockedEntryDisplay.defaultValue();
    }

    @Override
    public void initialize() {
        reload();
    }

    @Override
    public void reload() {
        applyGuiModel(configLoader.load());
        applyLockedEntryDisplay(loadLockedEntryDisplay());
    }

    public void applyGuiModel(@NonNull GuiModel guiModel) {
        this.guiModel = guiModel;
    }

    public void applyLockedEntryDisplay(@NonNull LockedEntryDisplay lockedEntryDisplay) {
        this.lockedEntryDisplay = lockedEntryDisplay;
    }

    public void openDefault(@NonNull Player player) {
        new MindexCatalogGui(
                player.getUniqueId(),
                catalogManager.getCatalog(),
                guiModel,
                lockedEntryDisplay,
                playerStateManager,
                registrationService
        ).open(player);
    }

    public void openCategory(@NonNull Player player, @NonNull String categoryId) {
        MindexCatalogGui gui = new MindexCatalogGui(
                player.getUniqueId(),
                catalogManager.getCatalog(),
                guiModel,
                lockedEntryDisplay,
                playerStateManager,
                registrationService
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

    @Override
    public void shutdown() {
        // no-op
    }

    private LockedEntryDisplay loadLockedEntryDisplay() {
        String modeValue = plugin.getConfig().getString(
                "mindexGui.locked-entry-display.mode",
                LockedEntryDisplayMode.FIXED_ITEM.name()
        );
        LockedEntryDisplayMode mode = LockedEntryDisplayMode.fromConfig(modeValue);

        String materialName = plugin.getConfig().getString("mindexGui.locked-entry-display.material", "GRAY_DYE");
        Material material = Material.matchMaterial(materialName == null ? "GRAY_DYE" : materialName);
        if (material == null) {
            throw new IllegalArgumentException("유효하지 않은 잠금 엔트리 material: " + materialName);
        }

        Integer customModelData = plugin.getConfig().contains("mindexGui.locked-entry-display.custom-model-data")
                ? plugin.getConfig().getInt("mindexGui.locked-entry-display.custom-model-data")
                : null;

        return new LockedEntryDisplay(mode, material, customModelData);
    }
}
