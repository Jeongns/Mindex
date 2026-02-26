package com.jeongns.mindex.mindexGui.gui;

public record GuiAction(ActionType type, String categoryId, String entryId) {
    public static GuiAction nextPage() {
        return new GuiAction(ActionType.NEXT_PAGE, null, null);
    }

    public static GuiAction prevPage() {
        return new GuiAction(ActionType.PREV_PAGE, null, null);
    }

    public static GuiAction openDefault() {
        return new GuiAction(ActionType.OPEN_DEFAULT, null, null);
    }

    public static GuiAction openCategory(String categoryId) {
        return new GuiAction(ActionType.OPEN_CATEGORY, categoryId, null);
    }

    public static GuiAction registerEntry(String entryId) {
        return new GuiAction(ActionType.REGISTER_ENTRY, null, entryId);
    }
}
