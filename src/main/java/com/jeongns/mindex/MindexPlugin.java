package com.jeongns.mindex;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.catalog.loader.CatalogConfigLoader;
import com.jeongns.mindex.command.CommandManager;
import com.jeongns.mindex.mindexGui.MindexGuiManager;
import com.jeongns.mindex.listener.ListenerManager;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.player.repository.factory.PlayerStateRepositoryFactory;
import com.jeongns.mindex.service.registration.RegistrationService;
import com.jeongns.mindex.service.reward.CategoryRewardService;
import com.jeongns.mindex.service.reward.RewardExecutor;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class MindexPlugin extends JavaPlugin {
    private CatalogManager catalogManager;
    @Getter
    private PlayerStateManager playerStateManager;
    @Getter
    private MindexGuiManager mindexGuiManager;
    private ListenerManager listenerManager;
    @Getter
    private RegistrationService registrationService;
    @Getter
    private CategoryRewardService categoryRewardService;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.catalogManager = new CatalogManager(new CatalogConfigLoader(this));
        this.playerStateManager = new PlayerStateManager(new PlayerStateRepositoryFactory(this).create());
        RewardExecutor rewardExecutor = new RewardExecutor(this);
        this.registrationService = new RegistrationService(
                catalogManager,
                playerStateManager,
                rewardExecutor
        );
        this.categoryRewardService = new CategoryRewardService(catalogManager, playerStateManager, rewardExecutor);
        this.mindexGuiManager = new MindexGuiManager(
                this,
                catalogManager,
                playerStateManager,
                registrationService,
                categoryRewardService
        );
        this.listenerManager = new ListenerManager(this, mindexGuiManager, playerStateManager);
        this.commandManager = new CommandManager(this);

        this.catalogManager.initialize();
        this.playerStateManager.initialize();
        this.mindexGuiManager.initialize();
        this.listenerManager.initialize();
        this.commandManager.initialize();

        getLogger().info("Mindex 플러그인이 시작되었습니다.");
    }

    @Override
    public void onDisable() {
        catalogManager.shutdown();
        playerStateManager.shutdown();
        mindexGuiManager.shutdown();
        listenerManager.shutdown();
        commandManager.shutdown();

        getLogger().info("Mindex 플러그인이 종료되었습니다.");
    }

    public void reloadPlugin() {
        reloadConfig();

        catalogManager.reload();
        playerStateManager.reload();
        mindexGuiManager.reload();
    }
}
