package com.jeongns.mindex.item;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public final class CustomModelDataComponentUtil {
    private CustomModelDataComponentUtil() {
    }

    public static CustomModelDataComponent create(
            @NonNull List<Float> floats,
            @NonNull List<Boolean> flags,
            @NonNull List<String> strings
    ) {
        CustomModelDataComponent component = emptyComponent();
        component.setFloats(List.copyOf(floats));
        component.setFlags(List.copyOf(flags));
        component.setStrings(List.copyOf(strings));
        return component;
    }

    public static CustomModelDataComponent copy(@NonNull CustomModelDataComponent source) {
        return create(source.getFloats(), source.getFlags(), source.getStrings());
    }

    public static CustomModelDataComponent withStrings(
            CustomModelDataComponent source,
            @NonNull List<String> strings
    ) {
        if (source == null) {
            return create(List.of(), List.of(), strings);
        }
        CustomModelDataComponent component = copy(source);
        component.setStrings(List.copyOf(strings));
        return component;
    }

    public static boolean isEmpty(CustomModelDataComponent component) {
        return component == null
                || (component.getFloats().isEmpty()
                && component.getFlags().isEmpty()
                && component.getStrings().isEmpty());
    }

    private static CustomModelDataComponent emptyComponent() {
        ItemMeta itemMeta = new ItemStack(Material.STONE).getItemMeta();
        return itemMeta.getCustomModelDataComponent();
    }
}
