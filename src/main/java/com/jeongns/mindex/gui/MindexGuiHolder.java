package com.jeongns.mindex.gui;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

@Getter
public final class MindexGuiHolder implements InventoryHolder {
    @NonNull
    private final UUID ownerUuid;
    private final Inventory inventory;

    public MindexGuiHolder(@NonNull UUID ownerUuid, int rows, @NonNull String title) {
        this.ownerUuid = ownerUuid;
        this.inventory = Bukkit.createInventory(this, rows * 9, Component.text(title));
    }
}
