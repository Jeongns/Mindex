package com.jeongns.mindex.player.repository.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.jeongns.mindex.player.repository.PlayerStateRepository;

import com.jeongns.mindex.player.entity.PlayerMindexState;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MySqlPlayerStateRepository implements PlayerStateRepository {
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private static final String PLAYER_STATE_TABLE = "mindex_player_state";
    private static final String UNLOCKED_ENTRIES_TABLE = "mindex_unlocked_entries";
    private static final String CLAIMED_CATEGORY_REWARDS_TABLE = "mindex_claimed_category_rewards";

    private static final String CREATE_PLAYER_STATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS mindex_player_state (
                player_uuid CHAR(36) PRIMARY KEY
            ) ENGINE=InnoDB
            """;
    private static final String CREATE_UNLOCKED_ENTRIES_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS mindex_unlocked_entries (
                player_uuid CHAR(36) NOT NULL,
                entry_id VARCHAR(255) NOT NULL,
                PRIMARY KEY (player_uuid, entry_id),
                CONSTRAINT fk_mindex_unlocked_entries_player
                    FOREIGN KEY (player_uuid) REFERENCES mindex_player_state (player_uuid)
                    ON DELETE CASCADE
            ) ENGINE=InnoDB
            """;
    private static final String CREATE_CLAIMED_CATEGORY_REWARDS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS mindex_claimed_category_rewards (
                player_uuid CHAR(36) NOT NULL,
                category_id VARCHAR(255) NOT NULL,
                PRIMARY KEY (player_uuid, category_id),
                CONSTRAINT fk_mindex_claimed_category_rewards_player
                    FOREIGN KEY (player_uuid) REFERENCES mindex_player_state (player_uuid)
                    ON DELETE CASCADE
            ) ENGINE=InnoDB
            """;

    @NonNull
    private final HikariDataSource dataSource;

    public MySqlPlayerStateRepository(@NonNull JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String jdbcUrl = requireString(config, "database.jdbc-url");
        String username = requireString(config, "database.username");
        String password = config.getString("database.password", "");
        loadDriver();
        this.dataSource = createDataSource(jdbcUrl, username, password);
        initializeSchema();
    }

    @Override
    public Optional<PlayerMindexState> findByPlayerId(@NonNull UUID playerId) {
        try (Connection connection = getConnection()) {
            if (!existsPlayer(connection, playerId)) {
                return Optional.empty();
            }

            Set<String> unlockedEntryIds = loadStringSet(
                    connection,
                    "SELECT entry_id FROM " + UNLOCKED_ENTRIES_TABLE + " WHERE player_uuid = ?",
                    playerId
            );
            Set<String> claimedCategoryRewardIds = loadStringSet(
                    connection,
                    "SELECT category_id FROM " + CLAIMED_CATEGORY_REWARDS_TABLE + " WHERE player_uuid = ?",
                    playerId
            );
            return Optional.of(new PlayerMindexState(playerId, unlockedEntryIds, claimedCategoryRewardIds));
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL 플레이어 상태 조회에 실패했습니다: " + playerId, e);
        }
    }

    @Override
    public void create(@NonNull UUID playerId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT IGNORE INTO " + PLAYER_STATE_TABLE + " (player_uuid) VALUES (?)"
             )) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL 플레이어 상태 생성에 실패했습니다: " + playerId, e);
        }
    }

    @Override
    public boolean unlock(@NonNull UUID playerId, @NonNull String entryId) {
        try (Connection connection = getConnection()) {
            ensurePlayerExists(connection, playerId);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT IGNORE INTO " + UNLOCKED_ENTRIES_TABLE + " (player_uuid, entry_id) VALUES (?, ?)"
            )) {
                statement.setString(1, playerId.toString());
                statement.setString(2, entryId);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL 엔트리 해금 저장에 실패했습니다: "
                    + playerId + " / " + entryId, e);
        }
    }

    @Override
    public boolean claimCategoryReward(@NonNull UUID playerId, @NonNull String categoryId) {
        try (Connection connection = getConnection()) {
            ensurePlayerExists(connection, playerId);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT IGNORE INTO " + CLAIMED_CATEGORY_REWARDS_TABLE
                            + " (player_uuid, category_id) VALUES (?, ?)"
            )) {
                statement.setString(1, playerId.toString());
                statement.setString(2, categoryId);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL 카테고리 보상 수령 저장에 실패했습니다: "
                    + playerId + " / " + categoryId, e);
        }
    }

    @Override
    public void reset(@NonNull UUID playerId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM " + PLAYER_STATE_TABLE + " WHERE player_uuid = ?"
             )) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL 플레이어 상태 초기화에 실패했습니다: " + playerId, e);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private void initializeSchema() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_PLAYER_STATE_TABLE_SQL);
            statement.executeUpdate(CREATE_UNLOCKED_ENTRIES_TABLE_SQL);
            statement.executeUpdate(CREATE_CLAIMED_CATEGORY_REWARDS_TABLE_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL 스키마 초기화에 실패했습니다.", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void loadDriver() {
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC 드라이버를 찾을 수 없습니다.", e);
        }
    }

    private HikariDataSource createDataSource(
            @NonNull String jdbcUrl,
            @NonNull String username,
            @NonNull String password
    ) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("Mindex-MySQL");
        hikariConfig.setDriverClassName(DRIVER_CLASS_NAME);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        return new HikariDataSource(hikariConfig);
    }

    private boolean existsPlayer(@NonNull Connection connection, @NonNull UUID playerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM " + PLAYER_STATE_TABLE + " WHERE player_uuid = ?"
        )) {
            statement.setString(1, playerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void ensurePlayerExists(@NonNull Connection connection, @NonNull UUID playerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT IGNORE INTO " + PLAYER_STATE_TABLE + " (player_uuid) VALUES (?)"
        )) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        }
    }

    private Set<String> loadStringSet(@NonNull Connection connection, @NonNull String sql, @NonNull UUID playerId)
            throws SQLException {
        Set<String> values = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    values.add(resultSet.getString(1));
                }
            }
        }
        return values;
    }

    private String requireString(@NonNull FileConfiguration config, @NonNull String path) {
        String value = config.getString(path);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(path + " 값이 비어 있습니다.");
        }
        return value;
    }
}
