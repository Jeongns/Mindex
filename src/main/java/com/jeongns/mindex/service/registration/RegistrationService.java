package com.jeongns.mindex.service.registration;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.item.CustomModelDataComponentUtil;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.service.reward.RewardExecutor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.Optional;

public class RegistrationService {
    @NonNull
    private final CatalogManager catalogManager;
    @NonNull
    private final PlayerStateManager playerStateManager;
    @NonNull
    private final RewardExecutor rewardExecutor;

    public RegistrationService(
            @NonNull CatalogManager catalogManager,
            @NonNull PlayerStateManager playerStateManager,
            @NonNull RewardExecutor rewardExecutor
    ) {
        this.catalogManager = catalogManager;
        this.playerStateManager = playerStateManager;
        this.rewardExecutor = rewardExecutor;
    }

    public RegistrationResult register(@NonNull Player player, @NonNull String entryId) {
        Optional<MindexEntry> entryOptional = catalogManager.findEntry(entryId);
        if (entryOptional.isEmpty()) {
            return RegistrationResult.entryNotFound(entryId);
        }

        MindexEntry entry = entryOptional.get();
        if (!canRegister(player, entry)) {
            return RegistrationResult.requirementNotMet(entry.getId(), entry.getName());
        }

        boolean unlocked = playerStateManager.unlock(player.getUniqueId(), entry.getId());
        if (!unlocked) {
            return RegistrationResult.alreadyRegistered(entry.getId(), entry.getName());
        }

        consumeItems(player, entry);
        rewardExecutor.execute(player, entry.getReward());
        return RegistrationResult.success(entry.getId(), entry.getName());
    }

    private boolean canRegister(@NonNull Player player, @NonNull MindexEntry entry) {
        int matchedAmount = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (!matchesEntry(itemStack, entry)) {
                continue;
            }

            matchedAmount += itemStack.getAmount();
            if (matchedAmount >= entry.getAmount()) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesEntry(ItemStack itemStack, @NonNull MindexEntry entry) {
        if (itemStack == null || itemStack.getType() != entry.getItem()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        CustomModelDataComponent requiredCustomModelData = entry.getCustomModelData();
        CustomModelDataComponent itemCustomModelData = extractCustomModelData(itemMeta);

        if (requiredCustomModelData == null) {
            return itemCustomModelData == null;
        }

        return hasSameCustomModelData(requiredCustomModelData, itemCustomModelData);
    }

    private boolean hasSameCustomModelData(
            @NonNull CustomModelDataComponent required,
            CustomModelDataComponent actual
    ) {
        if (actual == null) {
            return false;
        }

        return required.getFloats().equals(actual.getFloats())
                && required.getFlags().equals(actual.getFlags())
                && required.getStrings().equals(actual.getStrings());
    }

    private CustomModelDataComponent extractCustomModelData(ItemMeta itemMeta) {
        if (itemMeta == null) {
            return null;
        }

        CustomModelDataComponent customModelDataComponent = itemMeta.getCustomModelDataComponent();
        if (CustomModelDataComponentUtil.isEmpty(customModelDataComponent)) {
            return null;
        }
        return CustomModelDataComponentUtil.copy(customModelDataComponent);
    }

    private void consumeItems(@NonNull Player player, @NonNull MindexEntry entry) {
        int remainingAmount = entry.getAmount();
        ItemStack[] contents = player.getInventory().getContents();

        for (int slot = 0; slot < contents.length && remainingAmount > 0; slot++) {
            ItemStack itemStack = contents[slot];
            if (!matchesEntry(itemStack, entry)) {
                continue;
            }

            int consumedAmount = Math.min(itemStack.getAmount(), remainingAmount);
            int nextAmount = itemStack.getAmount() - consumedAmount;
            remainingAmount -= consumedAmount;

            if (nextAmount <= 0) {
                player.getInventory().setItem(slot, null);
                continue;
            }

            itemStack.setAmount(nextAmount);
            player.getInventory().setItem(slot, itemStack);
        }
    }
}
