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
        this.listener = new MindexGuiListener();
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
        new MindexGui(player.getUniqueId(), catalogManager.getCatalog()).open(player);
    }

    public void openCategory(@NonNull Player player, @NonNull String categoryId) {
        MindexGui gui = new MindexGui(player.getUniqueId(), catalogManager.getCatalog());
        gui.setCategory(categoryId);
        gui.open(player);
    }

    @Override
    public void shutdown() {
        // Bukkit Listener unregistration is not required yet because listener handlers are not implemented.
    }
}
