package com.jeongns.mindex.command.handler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

public final class RootCommandHandler implements CommandHandler {
    private final JavaPlugin plugin;

    public RootCommandHandler(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("mindex")
                .executes(ctx -> {
                    execute(ctx.getSource(), new String[0]);
                    return Command.SINGLE_SUCCESS;
                });
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        source.getSender().sendPlainMessage("마인크래프트 도감 플러그인 Mindex");
    }
}
