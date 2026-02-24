package com.jeongns.mindex;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.catalog.loader.CatalogConfigLoader;
import com.jeongns.mindex.command.CommandManager;
import com.jeongns.mindex.gui.GuiManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MindexPlugin extends JavaPlugin {
    private CatalogManager catalogManager;
    private GuiManager guiManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.catalogManager = new CatalogManager(new CatalogConfigLoader(this));
        this.catalogManager.initialize();

        this.guiManager = new GuiManager(this, catalogManager);
        this.guiManager.initialize();

        this.commandManager = new CommandManager(this);
        this.commandManager.initialize();

        getLogger().info("Mindex 플러그인이 시작되었습니다.");
    }

    @Override
    public void onDisable() {
        if (catalogManager != null) {
            catalogManager.shutdown();
        }
        if (guiManager != null) {
            guiManager.shutdown();
        }
        commandManager.shutdown();

        getLogger().info("Mindex 플러그인이 종료되었습니다.");
    }
}
