package com.jeongns.mindex.mindexGui.loader;

import com.jeongns.mindex.config.YamlNodeReader;
import com.jeongns.mindex.config.validation.ConfigValueValidator;
import com.jeongns.mindex.mindexGui.model.GuiModel;
import com.jeongns.mindex.mindexGui.model.CategorySymbol;
import com.jeongns.mindex.mindexGui.model.DefaultSymbol;
import com.jeongns.mindex.mindexGui.model.GuiView;
import lombok.AllArgsConstructor;
import lombok.NonNull;
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

    public GuiModel load() {
        YamlConfiguration config = loadConfig();
        YamlNodeReader gui = YamlNodeReader.root(config, "gui");

        Map<Character, DefaultSymbol> defaultSymbols = loadDefaultSymbols(gui.child("defaultSymbols"));
        Map<Character, CategorySymbol> categorySymbols = loadCategorySymbols(gui.child("categorySymbols"));
        validateDuplicateSymbols(defaultSymbols, categorySymbols);

        YamlNodeReader defaultViewNode = gui.child("defaultView");
        YamlNodeReader entryViewNode = gui.child("entryView");
        GuiView defaultView = loadView(defaultViewNode);
        GuiView entryView = loadView(entryViewNode);

        validateLayout(defaultView, defaultViewNode.getPath(), defaultSymbols, categorySymbols);
        validateLayout(entryView, entryViewNode.getPath(), defaultSymbols, categorySymbols);

        return new GuiModel(
                Map.copyOf(defaultSymbols),
                Map.copyOf(categorySymbols),
                defaultView,
                entryView
        );
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

            String role = ConfigValueValidator.requireString(symbolNode.getString("role"), symbolNode.pathForKey("role"));
            String name = symbolNode.getString("name");
            List<String> lore = symbolNode.getStringList("lore");

            symbols.put(symbol, new DefaultSymbol(
                    symbol,
                    role,
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

            String role = ConfigValueValidator.requireString(symbolNode.getString("role"), symbolNode.pathForKey("role"));
            String categoryId = ConfigValueValidator.requireString(
                    symbolNode.getString("categoryId"),
                    symbolNode.pathForKey("categoryId")
            );
            String name = symbolNode.getString("name");
            List<String> lore = symbolNode.getStringList("lore");

            symbols.put(symbol, new CategorySymbol(
                    symbol,
                    role,
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

    private char parseSymbol(@NonNull String rawSymbol, @NonNull String path) {
        if (rawSymbol.length() != 1) {
            throw new IllegalArgumentException("심볼은 한 글자여야 합니다: " + path + "." + rawSymbol);
        }
        return rawSymbol.charAt(0);
    }
}
