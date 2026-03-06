package com.jeongns.mindex.mindexGui;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.loader.GuiConfigLoader;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.service.registration.RegistrationService;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.java.JavaPlugin;

public class MindexGuiManager implements Manager {
    @NonNull
    private final JavaPlugin plugin;
    @NonNull
    private final CatalogManager catalogManager;
    @NonNull
    private final RegistrationService registrationService;
    @NonNull
    private final GuiConfigLoader configLoader;

    @Getter
    @NonNull
    private GuiModel guiModel;

    public MindexGuiManager(
            @NonNull JavaPlugin plugin,
            @NonNull CatalogManager catalogManager,
            @NonNull RegistrationService registrationService
    ) {
        this.plugin = plugin;
        this.catalogManager = catalogManager;
        this.registrationService = registrationService;
        this.configLoader = new GuiConfigLoader(plugin);
        this.guiModel = GuiModel.empty();
    }

    @Override
    public void initialize() {
        reload();
    }

    @Override
    public void reload() {
        applyGuiModel(configLoader.load());
    }

    public void applyGuiModel(@NonNull GuiModel guiModel) {
        this.guiModel = guiModel;
    }

    public void openDefault(@NonNull Player player) {
        new MindexCatalogGui(player.getUniqueId(), catalogManager.getCatalog(), guiModel, registrationService).open(player);
    }

    public void openCategory(@NonNull Player player, @NonNull String categoryId) {
        MindexCatalogGui gui = new MindexCatalogGui(player.getUniqueId(), catalogManager.getCatalog(), guiModel, registrationService);
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
}
