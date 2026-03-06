package com.jeongns.mindex.mindexGui.render;

import com.jeongns.mindex.catalog.entity.MindexCatalog;
import com.jeongns.mindex.catalog.entity.MindexCategory;
import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.mindexGui.action.GuiAction;
import com.jeongns.mindex.mindexGui.model.CategorySymbol;
import com.jeongns.mindex.mindexGui.model.DefaultSymbol;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.model.GuiView;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MindexCatalogGuiRenderer {
    public CatalogGuiRenderResult render(
            @NonNull MindexCatalogGui holder,
            @NonNull MindexCatalog catalog,
            @NonNull GuiModel guiModel,
            String categoryId,
            int requestedPage
    ) {
        Map<Integer, GuiAction> slotActions = new HashMap<>();
        boolean defaultView = categoryId == null || categoryId.isBlank();
        GuiView view = defaultView ? guiModel.getDefaultView() : guiModel.getEntryView();

        List<MindexEntry> entries = defaultView ? List.of() : findEntries(catalog, categoryId);
        List<Integer> entrySlots = collectEntrySlots(guiModel, view);
        int pageSize = entrySlots.size();
        int maxPage = computeMaxPage(entries.size(), pageSize);
        int page = normalizePage(requestedPage, maxPage);

        String resolvedTitle = resolveTitle(catalog, view.getTitle(), categoryId, page, maxPage);
        Inventory inventory = Bukkit.createInventory(holder, view.getRows() * 9, Component.text(resolvedTitle));

        renderBaseLayout(guiModel, view, inventory, slotActions);
        renderEntries(entries, entrySlots, page, pageSize, inventory, slotActions);

        return new CatalogGuiRenderResult(inventory, slotActions, page, maxPage);
    }

    private void renderBaseLayout(
            @NonNull GuiModel guiModel,
            @NonNull GuiView view,
            @NonNull Inventory inventory,
            @NonNull Map<Integer, GuiAction> slotActions
    ) {
        for (int row = 0; row < view.getLayout().size(); row++) {
            String line = view.getLayout().get(row);
            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                int slot = row * 9 + col;

                if (symbol == '.' || symbol == ' ') {
                    continue;
                }

                DefaultSymbol defaultSymbol = guiModel.getDefaultSymbols().get(symbol);
                if (defaultSymbol != null) {
                    String role = defaultSymbol.getRole();
                    if ("ENTRY_SLOT".equalsIgnoreCase(role)) {
                        continue;
                    }
                    registerDefaultAction(slot, role, slotActions);
                    inventory.setItem(slot, createItem(
                            defaultSymbol.getMaterial(),
                            defaultSymbol.getName(),
                            defaultSymbol.getLore(),
                            Material.PAPER
                    ));
                    continue;
                }

                CategorySymbol categorySymbol = guiModel.getCategorySymbols().get(symbol);
                if (categorySymbol != null) {
                    registerCategoryAction(slot, categorySymbol, slotActions);
                    inventory.setItem(slot, createItem(
                            categorySymbol.getMaterial(),
                            categorySymbol.getName(),
                            categorySymbol.getLore(),
                            Material.BOOK
                    ));
                }
            }
        }
    }

    private void renderEntries(
            @NonNull List<MindexEntry> entries,
            @NonNull List<Integer> slots,
            int page,
            int pageSize,
            @NonNull Inventory inventory,
            @NonNull Map<Integer, GuiAction> slotActions
    ) {
        if (pageSize <= 0 || entries.isEmpty()) {
            return;
        }

        int start = page * pageSize;
        int end = Math.min(start + pageSize, entries.size());
        int targetIndex = 0;

        for (int i = start; i < end; i++) {
            MindexEntry entry = entries.get(i);
            int slot = slots.get(targetIndex++);
            slotActions.put(slot, GuiAction.registerEntry(entry.getId()));
            inventory.setItem(slot, createItem(
                    entry.getItem(),
                    entry.getName(),
                    List.of(entry.getDescription()),
                    Material.PAPER
            ));
        }
    }

    private List<Integer> collectEntrySlots(@NonNull GuiModel guiModel, @NonNull GuiView view) {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < view.getLayout().size(); row++) {
            String line = view.getLayout().get(row);
            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                DefaultSymbol defaultSymbol = guiModel.getDefaultSymbols().get(symbol);
                if (defaultSymbol != null && "ENTRY_SLOT".equalsIgnoreCase(defaultSymbol.getRole())) {
                    slots.add(row * 9 + col);
                }
            }
        }
        return slots;
    }

    private List<MindexEntry> findEntries(@NonNull MindexCatalog catalog, String currentCategoryId) {
        if (currentCategoryId == null || currentCategoryId.isBlank()) {
            return Collections.emptyList();
        }
        for (MindexCategory category : catalog.getCategories()) {
            if (category.getId().equalsIgnoreCase(currentCategoryId)) {
                return category.getEntries();
            }
        }
        return Collections.emptyList();
    }

    private String resolveTitle(
            @NonNull MindexCatalog catalog,
            @NonNull String rawTitle,
            String currentCategoryId,
            int page,
            int maxPage
    ) {
        String categoryName = resolveCategoryName(catalog, currentCategoryId);
        String resolved = rawTitle
                .replace("%category_name%", categoryName)
                .replace("%page%", String.valueOf(page + 1))
                .replace("%max_page%", String.valueOf(maxPage));
        return colorize(resolved);
    }

    private String resolveCategoryName(@NonNull MindexCatalog catalog, String currentCategoryId) {
        if (currentCategoryId == null || currentCategoryId.isBlank()) {
            return "카테고리";
        }
        for (MindexCategory category : catalog.getCategories()) {
            if (category.getId().equalsIgnoreCase(currentCategoryId)) {
                return category.getCategoryName();
            }
        }
        return currentCategoryId.toLowerCase(Locale.ROOT);
    }

    private int computeMaxPage(int size, int pageSize) {
        if (pageSize <= 0 || size <= 0) {
            return 1;
        }
        return Math.max(1, (size + pageSize - 1) / pageSize);
    }

    private int normalizePage(int requestedPage, int maxPage) {
        if (requestedPage < 0) {
            return 0;
        }
        return Math.min(requestedPage, Math.max(0, maxPage - 1));
    }

    private void registerDefaultAction(int slot, @NonNull String role, @NonNull Map<Integer, GuiAction> slotActions) {
        if ("NEXT_PAGE".equalsIgnoreCase(role)) {
            slotActions.put(slot, GuiAction.nextPage());
            return;
        }
        if ("PREV_PAGE".equalsIgnoreCase(role)) {
            slotActions.put(slot, GuiAction.prevPage());
            return;
        }
        if ("OPEN_DEFAULT".equalsIgnoreCase(role)) {
            slotActions.put(slot, GuiAction.openDefault());
        }
    }

    private void registerCategoryAction(
            int slot,
            @NonNull CategorySymbol categorySymbol,
            @NonNull Map<Integer, GuiAction> slotActions
    ) {
        if (!"CATEGORY_BUTTON".equalsIgnoreCase(categorySymbol.getRole())) {
            return;
        }
        slotActions.put(slot, GuiAction.openCategory(categorySymbol.getCategoryId()));
    }

    private ItemStack createItem(Material material, String name, List<String> lore, Material fallback) {
        ItemStack itemStack = new ItemStack(material != null ? material : fallback);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (name != null && !name.isBlank()) {
                itemMeta.setDisplayName(colorize(name));
            }
            if (lore != null && !lore.isEmpty()) {
                itemMeta.setLore(lore.stream().map(this::colorize).toList());
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    private String colorize(@NonNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
