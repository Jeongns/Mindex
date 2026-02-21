package com.jeongns.mindex;

import com.jeongns.mindex.command.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MindexPlugin extends JavaPlugin {
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.commandManager = new CommandManager(this);
        this.commandManager.initialize();

        getLogger().info("Mindex 플러그인이 시작되었습니다.");
    }

    @Override
    public void onDisable() {
        commandManager.shutdown();

        getLogger().info("Mindex 플러그인이 종료되었습니다.");
    }
}
