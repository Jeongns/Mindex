package com.jeongns.mindex.command.handler;

import com.jeongns.mindex.MindexPlugin;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public final class MindexViewCommandHandler implements CommandHandler {
    @NonNull
    private final JavaPlugin plugin;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("view")
                .executes(ctx -> {
                    execute(ctx.getSource(), new String[0]);
                    return Command.SINGLE_SUCCESS;
                });
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendPlainMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return;
        }

        if (!(plugin instanceof MindexPlugin mindexPlugin) || mindexPlugin.getGuiManager() == null) {
            source.getSender().sendPlainMessage("GUI 매니저가 초기화되지 않았습니다.");
            plugin.getLogger().warning("[Command] /mindex view failed: GuiManager not initialized");
            return;
        }

        mindexPlugin.getGuiManager().openDefault(player);
        plugin.getLogger().info("[Command] /mindex view opened for " + player.getName());
    }
}
