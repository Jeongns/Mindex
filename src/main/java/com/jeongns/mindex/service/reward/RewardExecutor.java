package com.jeongns.mindex.service.reward;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RewardExecutor {
    @NonNull
    private final JavaPlugin plugin;

    public RewardExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean execute(@NonNull Player player, @NonNull List<String> rewardCommands) {
        boolean allExecuted = true;
        for (String rewardCommand : rewardCommands) {
            if (!execute(player, rewardCommand)) {
                allExecuted = false;
            }
        }
        return allExecuted;
    }

    public boolean execute(@NonNull Player player, String rewardCommand) {
        if (rewardCommand == null || rewardCommand.isBlank()) {
            return true;
        }
        String command = rewardCommand.replace("%player%", player.getName());
        boolean executed = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        if (!executed) {
            plugin.getLogger().warning("[Reward] command failed: " + command);
        }
        return executed;
    }
}
