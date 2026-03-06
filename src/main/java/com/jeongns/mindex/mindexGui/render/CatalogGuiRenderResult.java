package com.jeongns.mindex.mindexGui.render;

import com.jeongns.mindex.mindexGui.action.GuiAction;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public record CatalogGuiRenderResult(
        Inventory inventory,
        Map<Integer, GuiAction> slotActions,
        int page,
        int maxPage
) {
}
