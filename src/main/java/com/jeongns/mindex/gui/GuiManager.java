package com.jeongns.mindex.gui;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.gui.entity.GuiModel;
import com.jeongns.mindex.gui.listener.MindexGuiListener;
import com.jeongns.mindex.gui.loader.GuiConfigLoader;
import com.jeongns.mindex.gui.view.MindexGui;
import com.jeongns.mindex.manager.Manager;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiManager implements Manager {
    @NonNull
    private final JavaPlugin plugin;
    @NonNull
    private final CatalogManager catalogManager;
    @NonNull
    private final GuiConfigLoader configLoader;
    @NonNull
    private final MindexGuiListener listener;

    @Getter
    @NonNull
    private GuiModel guiModel;

    public GuiManager(@NonNull JavaPlugin plugin, @NonNull CatalogManager catalogManager) {
        this.plugin = plugin;
        this.catalogManager = catalogManager;
        this.configLoader = new GuiConfigLoader(plugin);
        this.listener = new MindexGuiListener(this);
        this.guiModel = GuiModel.empty();
    }

    @Override
    public void initialize() {
        reload();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public void reload() {
        applyGuiModel(configLoader.load());
    }

    public void applyGuiModel(@NonNull GuiModel guiModel) {
        this.guiModel = guiModel;
    }

    public void openDefault(@NonNull Player player) {
        new MindexGui(player.getUniqueId(), catalogManager.getCatalog(), guiModel).open(player);
    }

    public void openCategory(@NonNull Player player, @NonNull String categoryId) {
        MindexGui gui = new MindexGui(player.getUniqueId(), catalogManager.getCatalog(), guiModel);
        gui.setCategory(categoryId);
        gui.open(player);
    }

    public void handleOpen(@NonNull Player player, @NonNull MindexGui gui) {
        // Inventory open hook for GUI session lifecycle.
    }

    public void handleTopInventoryClick(
            @NonNull Player player,
            @NonNull MindexGui gui,
            int rawSlot,
            @NonNull ClickType clickType
    ) {
        gui.handleClick(player, rawSlot, clickType);
    }

    public void handleClose(@NonNull Player player, @NonNull MindexGui gui) {
        // Inventory close hook for GUI session lifecycle.
    }

    @Override
    public void shutdown() {
        // Bukkit Listener unregistration is not required yet because listener handlers are not implemented.
    }
}
