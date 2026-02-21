package com.jeongns.mindex.command;

import com.jeongns.mindex.command.handler.CommandHandler;
import com.jeongns.mindex.command.handler.HelpCommandHandler;
import com.jeongns.mindex.command.handler.MindexViewCommandHandler;
import com.jeongns.mindex.command.handler.RootCommandHandler;
import com.jeongns.mindex.manager.Manager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public final class CommandManager implements Manager {
    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        CommandHandler rootCommand = new RootCommandHandler(plugin);
        List<CommandHandler> handlers = List.of(
                new HelpCommandHandler(plugin),
                new MindexViewCommandHandler(plugin)
        );

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var root = rootCommand.createCommand();
            for (CommandHandler handler : handlers) {
                root.then(handler.createCommand());
            }
            commands.registrar().register(root.build(), "Mindex root command");
        });
    }
}
