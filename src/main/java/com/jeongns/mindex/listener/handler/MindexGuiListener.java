package com.jeongns.mindex.listener.handler;

import com.jeongns.mindex.mindexGui.MindexGuiManager;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
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
    private final MindexGuiManager mindexGuiManager;

    public MindexGuiListener(@NonNull MindexGuiManager mindexGuiManager) {
        this.mindexGuiManager = mindexGuiManager;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof MindexCatalogGui mindexGui)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        mindexGuiManager.handleOpen(player, mindexGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MindexCatalogGui mindexGui)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);
        ClickType clickType = event.getClick();
        mindexGuiManager.handleTopInventoryClick(player, mindexGui, event.getRawSlot(), clickType);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MindexCatalogGui mindexGui)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        mindexGuiManager.handleClose(player, mindexGui);
    }
}
