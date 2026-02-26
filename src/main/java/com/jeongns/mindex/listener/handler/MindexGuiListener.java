package com.jeongns.mindex.gui.listener;

import com.jeongns.mindex.gui.GuiManager;
import com.jeongns.mindex.gui.view.MindexGui;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public final class MindexGuiListener implements Listener {
    @NonNull
    private final GuiManager guiManager;

    public MindexGuiListener(@NonNull GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof MindexGui mindexGui)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        guiManager.handleOpen(player, mindexGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MindexGui mindexGui)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);
        ClickType clickType = event.getClick();
        guiManager.handleTopInventoryClick(player, mindexGui, event.getRawSlot(), clickType);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MindexGui mindexGui)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        guiManager.handleClose(player, mindexGui);
    }
}
