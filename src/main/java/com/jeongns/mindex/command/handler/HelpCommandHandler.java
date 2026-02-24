package com.jeongns.mindex.command.handler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public final class HelpCommandHandler implements CommandHandler {
    @NonNull
    private final JavaPlugin plugin;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("help")
                .executes(ctx -> {
                    execute(ctx.getSource(), new String[0]);
                    return Command.SINGLE_SUCCESS;
                });
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        source.getSender().sendPlainMessage("=== Mindex ===");
        source.getSender().sendPlainMessage("마인크래프트 도감 플러그인입니다.");
        source.getSender().sendPlainMessage("/mindex help - 안내 다시 보기");
        source.getSender().sendPlainMessage("/mindex view - 내 도감 보기");

        plugin.getLogger().info("[Command] /mindex help shown to " + source.getSender().getName());
    }
}
