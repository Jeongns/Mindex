package com.jeongns.mindex.mindexGui.gui;

import com.jeongns.mindex.catalog.entity.MindexCatalog;
import com.jeongns.mindex.catalog.entity.MindexCategory;
import com.jeongns.mindex.catalog.entity.MindexEntry;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.model.CategorySymbol;
import com.jeongns.mindex.mindexGui.model.DefaultSymbol;
import com.jeongns.mindex.mindexGui.model.GuiView;
import com.jeongns.mindex.service.registration.RegistrationService;
import com.jeongns.mindex.service.registration.RegistrationStatus;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class MindexCatalogGui implements InventoryHolder {
    private final UUID ownerUuid;
    private final MindexCatalog catalog;
    private final GuiModel guiModel;
    private final RegistrationService registrationService;
    private final Map<Integer, GuiAction> slotActions;
    private Inventory inventory;
    private int page;
    private int maxPage;
    private String categoryId;

    public MindexCatalogGui(
            @NonNull UUID ownerUuid,
            @NonNull MindexCatalog catalog,
            @NonNull GuiModel guiModel,
            @NonNull RegistrationService registrationService
    ) {
        this.ownerUuid = ownerUuid;
        this.catalog = catalog;
        this.guiModel = guiModel;
        this.registrationService = registrationService;
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
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return;
        }
        if (!ownerUuid.equals(player.getUniqueId())) {
            return;
        }
        if (rawSlot < 0 || rawSlot >= inventory.getSize()) {
            return;
        }

        GuiAction action = slotActions.get(rawSlot);
        if (action == null) {
            return;
        }

        boolean changed = switch (action.type()) {
            case NEXT_PAGE -> moveNextPage();
            case PREV_PAGE -> movePreviousPage();
            case OPEN_DEFAULT -> openDefaultCategory();
            case OPEN_CATEGORY -> openCategory(action.categoryId());
            case REGISTER_ENTRY -> registerEntry(player, action.entryId());
        };

        if (!changed) {
            return;
        }

        open(player);
    }

    public void setCategory(String categoryId) {
        changeCategory(categoryId == null ? "" : categoryId);
    }

    private void render() {
        slotActions.clear();
        boolean defaultView = categoryId == null || categoryId.isBlank();
        GuiView view = defaultView ? guiModel.getDefaultView() : guiModel.getEntryView();

        List<MindexEntry> entries = defaultView ? List.of() : findEntries(categoryId);
        List<Integer> entrySlots = collectEntrySlots(view);
        int pageSize = entrySlots.size();

        this.maxPage = computeMaxPage(entries.size(), pageSize);
        this.page = normalizePage(page, maxPage);

        String resolvedTitle = resolveTitle(view.getTitle(), categoryId, page, maxPage);
        this.inventory = Bukkit.createInventory(this, view.getRows() * 9, Component.text(resolvedTitle));
        renderBaseLayout(view);
        renderEntries(entries, entrySlots, page, pageSize);
    }

    private void renderBaseLayout(@NonNull GuiView view) {
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
                    registerDefaultAction(slot, role);
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
                    registerCategoryAction(slot, categorySymbol);
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
            int pageSize
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

    private List<Integer> collectEntrySlots(@NonNull GuiView view) {
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

    private List<MindexEntry> findEntries(String currentCategoryId) {
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
            @NonNull String rawTitle,
            String currentCategoryId,
            int page,
            int maxPage
    ) {
        String categoryName = resolveCategoryName(currentCategoryId);
        String resolved = rawTitle
                .replace("%category_name%", categoryName)
                .replace("%page%", String.valueOf(page + 1))
                .replace("%max_page%", String.valueOf(maxPage));
        return colorize(resolved);
    }

    private String resolveCategoryName(String currentCategoryId) {
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

    private void registerDefaultAction(int slot, @NonNull String role) {
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

    private void registerCategoryAction(int slot, @NonNull CategorySymbol categorySymbol) {
        if (!"CATEGORY_BUTTON".equalsIgnoreCase(categorySymbol.getRole())) {
            return;
        }
        slotActions.put(slot, GuiAction.openCategory(categorySymbol.getCategoryId()));
    }

    private boolean moveNextPage() {
        if (page + 1 >= maxPage) {
            return false;
        }
        page++;
        render();
        return true;
    }

    private boolean movePreviousPage() {
        if (page <= 0) {
            return false;
        }
        page--;
        render();
        return true;
    }

    private boolean openDefaultCategory() {
        return changeCategory("");
    }

    private boolean openCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return false;
        }
        return changeCategory(categoryId);
    }

    private boolean registerEntry(@NonNull Player player, String entryId) {
        if (entryId == null || entryId.isBlank()) {
            return false;
        }

        RegistrationStatus status = registrationService.register(player, entryId);
        return switch (status) {
            case SUCCESS -> {
                player.sendMessage(colorize("&a도감이 등록되었습니다: " + entryId));
                yield true;
            }
            case ALREADY_REGISTERED -> {
                player.sendMessage(colorize("&e이미 등록된 도감입니다."));
                yield false;
            }
            case REQUIREMENT_NOT_MET -> {
                player.sendMessage(colorize("&c등록 조건을 만족하지 못했습니다."));
                yield false;
            }
            case ENTRY_NOT_FOUND -> {
                player.sendMessage(colorize("&c존재하지 않는 도감 엔트리입니다."));
                yield false;
            }
            case UNSUPPORTED_UNLOCK_TYPE -> {
                player.sendMessage(colorize("&c지원하지 않는 등록 타입입니다."));
                yield false;
            }
        };
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

    private ItemStack createItem(Material material, String name, List<String> lore, Material fallback) {
        ItemStack itemStack = new ItemStack(material != null ? material : fallback);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (name != null && !name.isBlank()) {
                itemMeta.setDisplayName(colorize(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<String> convertedLore = lore.stream().map(this::colorize).toList();
                itemMeta.setLore(convertedLore);
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    private String colorize(@NonNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
