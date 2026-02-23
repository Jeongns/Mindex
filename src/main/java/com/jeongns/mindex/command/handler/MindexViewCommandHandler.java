package com.jeongns.mindex.command.handler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

public final class MindexViewCommandHandler implements CommandHandler {
    private final JavaPlugin plugin;

    public MindexViewCommandHandler(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

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
        source.getSender().sendPlainMessage("=== 내 도감 ===");
        source.getSender().sendPlainMessage("진행도: 0/0 (초기 구현)");
        source.getSender().sendPlainMessage("상세 도감 UI는 다음 단계에서 연결됩니다.");
        plugin.getLogger().info("[Command] /mindex view executed by " + source.getSender().getName());
    }
}
