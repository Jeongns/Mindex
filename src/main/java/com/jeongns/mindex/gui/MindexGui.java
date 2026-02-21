package com.jeongns.mindex.gui;

import com.jeongns.mindex.catalog.entity.MindexCatalog;
import com.jeongns.mindex.player.entity.PlayerMindexState;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MindexGui {
    private static final int DEFAULT_ROWS = 6;
    private static final String DEFAULT_TITLE = "Mindex";

    private final MindexGuiHolder holder;
    private int page;
    private String category;

    public MindexGui(UUID ownerUuid, MindexCatalog catalog, PlayerMindexState playerState) {
        this.holder = new MindexGuiHolder(ownerUuid, DEFAULT_ROWS, DEFAULT_TITLE);
        this.page = 0;
        this.category = "";
        render();
    }

    public void open(Player player) {
        player.openInventory(holder.getInventory());
    }

    public void nextPage() {

    }

    public void previousPage() {
    }

    public void setCategory(String category) {

    }

    private void render() {
    }
}
