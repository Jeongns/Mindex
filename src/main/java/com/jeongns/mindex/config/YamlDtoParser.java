package com.jeongns.mindex.config;

import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class YamlDtoParser {
    private final JavaPlugin plugin;
    private final Yaml yaml;

    public YamlDtoParser(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.yaml = new Yaml();
    }

    public <T> T loadFromDataFolder(@NonNull String relativePath, @NonNull Class<T> targetType) {
        Path path = plugin.getDataFolder().toPath().resolve(relativePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("YAML 파일이 존재하지 않습니다: " + relativePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            T data = yaml.loadAs(inputStream, targetType);
            if (data == null) {
                throw new IllegalArgumentException("YAML 파일이 비어있습니다: " + relativePath);
            }
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("YAML 파일 로드 실패: " + relativePath, e);
        }
    }
}
