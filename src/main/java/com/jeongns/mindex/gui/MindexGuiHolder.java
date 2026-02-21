package com.jeongns.mindex.gui;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class MindexGuiHolder implements InventoryHolder {
    private final UUID ownerUuid;
    private final Inventory inventory;

    public MindexGuiHolder(UUID ownerUuid, int rows, String title) {
        this.ownerUuid = Objects.requireNonNull(ownerUuid, "ownerUuid");
        this.inventory = Bukkit.createInventory(this, rows * 9, Component.text(title));
    }
}
