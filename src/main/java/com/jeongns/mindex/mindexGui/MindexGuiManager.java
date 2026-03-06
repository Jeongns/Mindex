package com.jeongns.mindex.mindexGui;

import com.jeongns.mindex.catalog.CatalogManager;
import com.jeongns.mindex.manager.Manager;
import com.jeongns.mindex.mindexGui.loader.GuiConfigLoader;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.model.GuiSoundSetting;
import com.jeongns.mindex.mindexGui.model.GuiSoundSettings;
import com.jeongns.mindex.mindexGui.model.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.model.LockedEntryDisplayMode;
import com.jeongns.mindex.mindexGui.view.MindexCatalogGui;
import com.jeongns.mindex.player.PlayerStateManager;
import com.jeongns.mindex.service.registration.RegistrationService;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.java.JavaPlugin;

public class MindexGuiManager implements Manager {
    @NonNull
    private final JavaPlugin plugin;
    @NonNull
    private final CatalogManager catalogManager;
    @NonNull
    private final PlayerStateManager playerStateManager;
    @NonNull
    private final RegistrationService registrationService;
    @NonNull
    private final GuiConfigLoader configLoader;

    @Getter
    @NonNull
    private GuiModel guiModel;
    @Getter
    @NonNull
    private LockedEntryDisplay lockedEntryDisplay;
    @Getter
    @NonNull
    private GuiSoundSettings guiSoundSettings;

    public MindexGuiManager(
            @NonNull JavaPlugin plugin,
            @NonNull CatalogManager catalogManager,
            @NonNull PlayerStateManager playerStateManager,
            @NonNull RegistrationService registrationService
    ) {
        this.plugin = plugin;
        this.catalogManager = catalogManager;
        this.playerStateManager = playerStateManager;
        this.registrationService = registrationService;
        this.configLoader = new GuiConfigLoader(plugin);
        this.guiModel = GuiModel.empty();
        this.lockedEntryDisplay = LockedEntryDisplay.defaultValue();
        this.guiSoundSettings = GuiSoundSettings.defaultValue();
    }

    @Override
    public void initialize() {
        reload();
    }

    @Override
    public void reload() {
        applyGuiModel(configLoader.load());
        applyLockedEntryDisplay(loadLockedEntryDisplay());
        applyGuiSoundSettings(loadGuiSoundSettings());
    }

    public void applyGuiModel(@NonNull GuiModel guiModel) {
        this.guiModel = guiModel;
    }

    public void applyLockedEntryDisplay(@NonNull LockedEntryDisplay lockedEntryDisplay) {
        this.lockedEntryDisplay = lockedEntryDisplay;
    }

    public void applyGuiSoundSettings(@NonNull GuiSoundSettings guiSoundSettings) {
        this.guiSoundSettings = guiSoundSettings;
    }

    public void openDefault(@NonNull Player player) {
        new MindexCatalogGui(
                player.getUniqueId(),
                catalogManager.getCatalog(),
                guiModel,
                lockedEntryDisplay,
                guiSoundSettings,
                playerStateManager,
                registrationService
        ).open(player);
    }

    public void openCategory(@NonNull Player player, @NonNull String categoryId) {
        MindexCatalogGui gui = new MindexCatalogGui(
                player.getUniqueId(),
                catalogManager.getCatalog(),
                guiModel,
                lockedEntryDisplay,
                guiSoundSettings,
                playerStateManager,
                registrationService
        );
        gui.setCategory(categoryId);
        gui.open(player);
    }

    public void handleOpen(@NonNull Player player, @NonNull MindexCatalogGui gui) {
        // Inventory open hook for GUI session lifecycle.
    }

    public void handleTopInventoryClick(
            @NonNull Player player,
            @NonNull MindexCatalogGui gui,
            int rawSlot,
            @NonNull ClickType clickType
    ) {
        gui.handleClick(player, rawSlot, clickType);
    }

    public void handleClose(@NonNull Player player, @NonNull MindexCatalogGui gui) {
        // Inventory close hook for GUI session lifecycle.
    }

    @Override
    public void shutdown() {
        // no-op
    }

    private LockedEntryDisplay loadLockedEntryDisplay() {
        String modeValue = plugin.getConfig().getString(
                "mindexGui.locked-entry-display.mode",
                LockedEntryDisplayMode.FIXED_ITEM.name()
        );
        LockedEntryDisplayMode mode = LockedEntryDisplayMode.fromConfig(modeValue);

        String materialName = plugin.getConfig().getString("mindexGui.locked-entry-display.material", "GRAY_DYE");
        Material material = Material.matchMaterial(materialName == null ? "GRAY_DYE" : materialName);
        if (material == null) {
            throw new IllegalArgumentException("유효하지 않은 잠금 엔트리 material: " + materialName);
        }

        Integer customModelData = plugin.getConfig().contains("mindexGui.locked-entry-display.custom-model-data")
                ? plugin.getConfig().getInt("mindexGui.locked-entry-display.custom-model-data")
                : null;

        return new LockedEntryDisplay(mode, material, customModelData);
    }

    private GuiSoundSettings loadGuiSoundSettings() {
        return new GuiSoundSettings(
                loadGuiSoundSetting("mindexGui.sounds.menu-select", GuiSoundSettings.defaultValue().getMenuSelect()),
                loadGuiSoundSetting("mindexGui.sounds.registration-success", GuiSoundSettings.defaultValue().getRegistrationSuccess()),
                loadGuiSoundSetting("mindexGui.sounds.registration-fail", GuiSoundSettings.defaultValue().getRegistrationFail())
        );
    }

    private GuiSoundSetting loadGuiSoundSetting(@NonNull String path, @NonNull GuiSoundSetting defaultValue) {
        boolean enabled = plugin.getConfig().getBoolean(path + ".enabled", defaultValue.isEnabled());
        String defaultSoundKey = defaultValue.getSound() == null
                ? null
                : Registry.SOUND_EVENT.getKey(defaultValue.getSound()).asString();
        String soundKey = plugin.getConfig().getString(path + ".sound", defaultSoundKey);
        Sound sound = resolveSound(soundKey, path + ".sound");
        float volume = (float) plugin.getConfig().getDouble(path + ".volume", defaultValue.getVolume());
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", defaultValue.getPitch());
        return new GuiSoundSetting(enabled, sound, volume, pitch);
    }

    private Sound resolveSound(String soundKey, @NonNull String path) {
        if (soundKey == null || soundKey.isBlank()) {
            return null;
        }

        NamespacedKey key = NamespacedKey.fromString(soundKey.trim());
        if (key == null) {
            throw new IllegalArgumentException("유효하지 않은 사운드 키 형식: " + path + "=" + soundKey);
        }

        Sound sound = Registry.SOUND_EVENT.get(key);
        if (sound == null) {
            throw new IllegalArgumentException("등록되지 않은 사운드 키: " + path + "=" + soundKey);
        }
        return sound;
    }
}
