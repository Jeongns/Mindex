package com.jeongns.mindex;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.catalog.loader.CatalogConfigLoader;
import com.jeongns.mindex.command.CommandManager;
import com.jeongns.mindex.mindexGui.MindexGuiManager;
import com.jeongns.mindex.listener.ListenerManager;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.player.repository.InMemoryPlayerStateRepository;
import com.jeongns.mindex.scheduler.SchedulerManager;
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
    private SchedulerManager schedulerManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.catalogManager = new CatalogManager(new CatalogConfigLoader(this));
        this.playerStateManager = new PlayerStateManager(new InMemoryPlayerStateRepository());
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
        this.schedulerManager = new SchedulerManager(this, playerStateManager);
        this.commandManager = new CommandManager(this);

        this.catalogManager.initialize();
        this.playerStateManager.initialize();
        this.mindexGuiManager.initialize();
        this.listenerManager.initialize();
        this.schedulerManager.initialize();
        this.commandManager.initialize();

        getLogger().info("Mindex 플러그인이 시작되었습니다.");
    }

    @Override
    public void onDisable() {
        if (catalogManager != null) {
            catalogManager.shutdown();
        }
        if (playerStateManager != null) {
            playerStateManager.shutdown();
        }
        if (mindexGuiManager != null) {
            mindexGuiManager.shutdown();
        }
        if (listenerManager != null) {
            listenerManager.shutdown();
        }
        if (schedulerManager != null) {
            schedulerManager.shutdown();
        }
        commandManager.shutdown();

        getLogger().info("Mindex 플러그인이 종료되었습니다.");
    }
}
