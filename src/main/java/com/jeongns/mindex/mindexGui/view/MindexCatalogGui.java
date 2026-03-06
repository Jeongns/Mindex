package com.jeongns.mindex.mindexGui.view;

import com.jeongns.mindex.catalog.entity.MindexCatalog;
import com.jeongns.mindex.mindexGui.action.GuiAction;
import com.jeongns.mindex.mindexGui.interaction.MindexCatalogGuiInteractionHandler;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.model.LockedEntryDisplay;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.mindexGui.render.CatalogGuiRenderResult;
import com.jeongns.mindex.mindexGui.render.MindexCatalogGuiRenderer;
import com.jeongns.mindex.service.registration.RegistrationService;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MindexCatalogGui implements InventoryHolder {
    private final UUID ownerUuid;
    private final MindexCatalog catalog;
    private final GuiModel guiModel;
    private final LockedEntryDisplay lockedEntryDisplay;
    private final PlayerStateManager playerStateManager;
    private final MindexCatalogGuiRenderer renderer;
    private final MindexCatalogGuiInteractionHandler interactionHandler;
    private final Map<Integer, GuiAction> slotActions;
    private Inventory inventory;
    private int page;
    private int maxPage;
    private String categoryId;

    public MindexCatalogGui(
            @NonNull UUID ownerUuid,
            @NonNull MindexCatalog catalog,
            @NonNull GuiModel guiModel,
            @NonNull LockedEntryDisplay lockedEntryDisplay,
            @NonNull PlayerStateManager playerStateManager,
            @NonNull RegistrationService registrationService
    ) {
        this.ownerUuid = ownerUuid;
        this.catalog = catalog;
        this.guiModel = guiModel;
        this.lockedEntryDisplay = lockedEntryDisplay;
        this.playerStateManager = playerStateManager;
        this.renderer = new MindexCatalogGuiRenderer();
        this.interactionHandler = new MindexCatalogGuiInteractionHandler(registrationService);
        this.slotActions = new HashMap<>();
        this.page = 0;
        this.maxPage = 1;
        this.categoryId = "";
        render();
    }

    public void open(@NonNull Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(@NonNull Player player, int rawSlot, @NonNull ClickType clickType) {
        boolean changed = interactionHandler.handleClick(this, player, rawSlot, clickType);
        if (!changed) {
            return;
        }

        open(player);
    }

    public void setCategory(String categoryId) {
        changeCategory(categoryId == null ? "" : categoryId);
    }

    public void refresh() {
        render();
    }

    private void render() {
        CatalogGuiRenderResult result = renderer.render(
                this,
                catalog,
                guiModel,
                lockedEntryDisplay,
                playerStateManager,
                categoryId,
                page
        );
        slotActions.clear();
        slotActions.putAll(result.slotActions());
        inventory = result.inventory();
        page = result.page();
        maxPage = result.maxPage();
    }

    public boolean moveNextPage() {
        if (page + 1 >= maxPage) {
            return false;
        }
        page++;
        render();
        return true;
    }

    public boolean movePreviousPage() {
        if (page <= 0) {
            return false;
        }
        page--;
        render();
        return true;
    }

    public boolean openDefaultCategory() {
        return changeCategory("");
    }

    public boolean openCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return false;
        }
        return changeCategory(categoryId);
    }

    private boolean changeCategory(@NonNull String nextCategoryId) {
        if (nextCategoryId.equals(this.categoryId)) {
            return false;
        }
        this.categoryId = nextCategoryId;
        this.page = 0;
        render();
        return true;
    }

    public boolean isOwner(@NonNull Player player) {
        return ownerUuid.equals(player.getUniqueId());
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public GuiAction findAction(int rawSlot) {
        return slotActions.get(rawSlot);
    }
}
