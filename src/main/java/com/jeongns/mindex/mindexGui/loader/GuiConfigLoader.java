package com.jeongns.mindex.mindexGui.loader;

import com.jeongns.mindex.config.YamlNodeReader;
import com.jeongns.mindex.config.CustomModelDataComponentParser;
import com.jeongns.mindex.config.validation.ConfigValueValidator;
import com.jeongns.mindex.mindexGui.model.config.GuiMessageSettings;
import com.jeongns.mindex.mindexGui.model.config.GuiSettings;
import com.jeongns.mindex.mindexGui.model.config.GuiSoundSetting;
import com.jeongns.mindex.mindexGui.model.config.GuiSoundSettings;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplay;
import com.jeongns.mindex.mindexGui.display.LockedEntryDisplayMode;
import com.jeongns.mindex.mindexGui.model.layout.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class GuiConfigLoader {
    private static final String GUI_FILE_NAME = "gui.yml";

    @NonNull
    private final JavaPlugin plugin;

    public GuiSettings load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        GuiModel guiModel = loadGuiModel();
        LockedEntryDisplay lockedEntryDisplay = loadLockedEntryDisplay();
        GuiSoundSettings guiSoundSettings = loadGuiSoundSettings();
        GuiMessageSettings guiMessageSettings = loadGuiMessageSettings();

        return new GuiSettings(guiModel, lockedEntryDisplay, guiSoundSettings, guiMessageSettings);
    }

    private GuiModel loadGuiModel() {
        YamlConfiguration config = loadConfig();
        Map<Character, DefaultSymbol> defaultSymbols = loadDefaultSymbols(YamlNodeReader.root(config, "defaultSymbols"));
        Map<Character, CategorySymbol> categorySymbols = loadCategorySymbols(YamlNodeReader.root(config, "categorySymbols"));
        validateDuplicateSymbols(defaultSymbols, categorySymbols);

        YamlNodeReader defaultViewNode = YamlNodeReader.root(config, "defaultView");
        YamlNodeReader entryViewNode = YamlNodeReader.root(config, "entryView");
        GuiView defaultView = loadView(defaultViewNode);
        GuiView entryView = loadView(entryViewNode);

        validateLayout(defaultView, defaultViewNode.getPath(), defaultSymbols, categorySymbols);
        validateLayout(entryView, entryViewNode.getPath(), defaultSymbols, categorySymbols);
        validateDefaultView(defaultView, defaultViewNode.getPath(), defaultSymbols);

        return new GuiModel(
                Map.copyOf(defaultSymbols),
                Map.copyOf(categorySymbols),
                defaultView,
                entryView
        );
    }

    private LockedEntryDisplay loadLockedEntryDisplay() {
        String modeValue = plugin.getConfig().getString(
                "locked-entry-display.mode",
                LockedEntryDisplayMode.FIXED_ITEM.name()
        );
        LockedEntryDisplayMode mode = LockedEntryDisplayMode.fromConfig(modeValue);

        String materialName = plugin.getConfig().getString("locked-entry-display.material", "GRAY_DYE");
        Material material = Material.matchMaterial(materialName == null ? "GRAY_DYE" : materialName);
        if (material == null) {
            throw new IllegalArgumentException("유효하지 않은 잠금 엔트리 material: " + materialName);
        }

        var customModelData = CustomModelDataComponentParser.parseSection(plugin.getConfig(), "locked-entry-display.custom-model-data");
        String name = plugin.getConfig().getString("locked-entry-display.name");
        String stringPrefix = plugin.getConfig().getString("locked-entry-display.string-prefix");
        return new LockedEntryDisplay(mode, material, customModelData, name, stringPrefix);
    }

    private GuiSoundSettings loadGuiSoundSettings() {
        return new GuiSoundSettings(
                loadGuiSoundSetting("sounds.menu-select", GuiSoundSettings.defaultValue().getMenuSelect()),
                loadGuiSoundSetting("sounds.registration-success", GuiSoundSettings.defaultValue().getRegistrationSuccess()),
                loadGuiSoundSetting("sounds.registration-fail", GuiSoundSettings.defaultValue().getRegistrationFail())
        );
    }

    private GuiMessageSettings loadGuiMessageSettings() {
        GuiMessageSettings defaultValue = GuiMessageSettings.defaultValue();
        return new GuiMessageSettings(
                plugin.getConfig().getString("messages.registration.success", defaultValue.getRegistrationSuccess()),
                plugin.getConfig().getString("messages.registration.already-registered", defaultValue.getRegistrationAlreadyRegistered()),
                plugin.getConfig().getString("messages.registration.requirement-not-met", defaultValue.getRegistrationRequirementNotMet()),
                plugin.getConfig().getString("messages.category-reward.success", defaultValue.getCategoryRewardSuccess()),
                plugin.getConfig().getString("messages.category-reward.not-complete", defaultValue.getCategoryRewardNotComplete()),
                plugin.getConfig().getString("messages.category-reward.already-claimed", defaultValue.getCategoryRewardAlreadyClaimed())
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

    private YamlConfiguration loadConfig() {
        ensureConfigFile();
        File guiFile = new File(plugin.getDataFolder(), GUI_FILE_NAME);
        return YamlConfiguration.loadConfiguration(guiFile);
    }

    private void ensureConfigFile() {
        File guiFile = new File(plugin.getDataFolder(), GUI_FILE_NAME);
        if (!guiFile.exists()) {
            plugin.saveResource(GUI_FILE_NAME, false);
        }
    }

    private Map<Character, DefaultSymbol> loadDefaultSymbols(@NonNull YamlNodeReader symbolsNode) {
        Map<Character, DefaultSymbol> symbols = new LinkedHashMap<>();

        for (String rawSymbol : symbolsNode.keys()) {
            char symbol = parseSymbol(rawSymbol, symbolsNode.getPath());
            YamlNodeReader symbolNode = symbolsNode.child(rawSymbol);

            String roleValue = ConfigValueValidator.requireString(symbolNode.getString("role"), symbolNode.pathForKey("role"));
            String name = symbolNode.getString("name");
            List<String> lore = symbolNode.getStringList("lore");

            symbols.put(symbol, new DefaultSymbol(
                    symbol,
                    SymbolRole.fromConfig(roleValue),
                    ConfigValueValidator.optionalMaterial(symbolNode.getString("material"), symbolNode.pathForKey("material")),
                    name,
                    lore
            ));
        }

        return symbols;
    }

    private Map<Character, CategorySymbol> loadCategorySymbols(@NonNull YamlNodeReader symbolsNode) {
        Map<Character, CategorySymbol> symbols = new LinkedHashMap<>();

        for (String rawSymbol : symbolsNode.keys()) {
            char symbol = parseSymbol(rawSymbol, symbolsNode.getPath());
            YamlNodeReader symbolNode = symbolsNode.child(rawSymbol);

            String categoryId = ConfigValueValidator.requireString(
                    symbolNode.getString("categoryId"),
                    symbolNode.pathForKey("categoryId")
            );
            String name = symbolNode.getString("name");
            List<String> lore = symbolNode.getStringList("lore");

            symbols.put(symbol, new CategorySymbol(
                    symbol,
                    SymbolRole.CATEGORY_BUTTON,
                    categoryId,
                    ConfigValueValidator.optionalMaterial(symbolNode.getString("material"), symbolNode.pathForKey("material")),
                    name,
                    lore
            ));
        }

        return symbols;
    }

    private GuiView loadView(@NonNull YamlNodeReader viewNode) {
        String title = ConfigValueValidator.requireString(viewNode.getString("title"), viewNode.pathForKey("title"));
        int rows = viewNode.getInt("rows", 0);
        if (rows <= 0 || rows > 6) {
            throw new IllegalArgumentException("rows는 1~6 이어야 합니다: " + viewNode.getPath() + ".rows=" + rows);
        }

        List<String> layout = viewNode.getStringList("layout");
        if (layout.isEmpty()) {
            throw new IllegalArgumentException("layout이 비어 있습니다: " + viewNode.getPath() + ".layout");
        }

        return new GuiView(title, rows, layout);
    }

    private void validateDuplicateSymbols(
            @NonNull Map<Character, DefaultSymbol> defaultSymbols,
            @NonNull Map<Character, CategorySymbol> categorySymbols
    ) {
        for (Character symbol : defaultSymbols.keySet()) {
            if (categorySymbols.containsKey(symbol)) {
                throw new IllegalArgumentException("defaultSymbols와 categorySymbols 심볼이 중복됩니다: " + symbol);
            }
        }
    }

    private void validateLayout(
            @NonNull GuiView view,
            @NonNull String viewPath,
            @NonNull Map<Character, DefaultSymbol> defaultSymbols,
            @NonNull Map<Character, CategorySymbol> categorySymbols
    ) {
        if (view.getLayout().size() != view.getRows()) {
            throw new IllegalArgumentException("layout 줄 수가 rows와 다릅니다: " + viewPath);
        }

        for (String row : view.getLayout()) {
            if (row.length() != 9) {
                throw new IllegalArgumentException("layout 한 줄 길이는 9여야 합니다: " + viewPath + ", row=" + row);
            }

            for (int i = 0; i < row.length(); i++) {
                char symbol = row.charAt(i);
                if (symbol == '.' || symbol == ' ') {
                    continue;
                }
                boolean exists = defaultSymbols.containsKey(symbol) || categorySymbols.containsKey(symbol);
                if (!exists) {
                    throw new IllegalArgumentException("정의되지 않은 심볼입니다: " + symbol + " in " + viewPath);
                }
            }
        }
    }

    private void validateDefaultView(
            @NonNull GuiView defaultView,
            @NonNull String viewPath,
            @NonNull Map<Character, DefaultSymbol> defaultSymbols
    ) {
        for (String row : defaultView.getLayout()) {
            for (int i = 0; i < row.length(); i++) {
                DefaultSymbol symbol = defaultSymbols.get(row.charAt(i));
                if (symbol != null && symbol.getRole() == SymbolRole.CLAIM_CATEGORY_REWARD) {
                    throw new IllegalArgumentException("defaultView에는 CLAIM_CATEGORY_REWARD를 배치할 수 없습니다: " + viewPath);
                }
            }
        }
    }

    private char parseSymbol(@NonNull String rawSymbol, @NonNull String path) {
        if (rawSymbol.length() != 1) {
            throw new IllegalArgumentException("심볼은 한 글자여야 합니다: " + path + "." + rawSymbol);
        }
        return rawSymbol.charAt(0);
    }
}
