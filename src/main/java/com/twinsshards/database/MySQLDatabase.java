package com.twinsshards.database;

import com.twinsshards.TwinsShards;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLDatabase implements DatabaseManager {

    private final TwinsShards plugin;
    private HikariDataSource dataSource;

    public MySQLDatabase(TwinsShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() throws Exception {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.mysql.host", "localhost");
        int port = config.getInt("database.mysql.port", 3306);
        String database = config.getString("database.mysql.database", "minecraft");
        String username = config.getString("database.mysql.username", "root");
        String password = config.getString("database.mysql.password", "");
        boolean ssl = config.getBoolean("database.mysql.ssl", false);
        int poolSize = config.getInt("database.mysql.pool-size", 10);
        long timeout = config.getLong("database.mysql.timeout", 5000);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl + "&autoReconnect=true&characterEncoding=utf8");
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setConnectionTimeout(timeout);
        hikariConfig.setPoolName("TwinsShards-Pool");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS shards_data (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "player_name VARCHAR(16), " +
                            "balance DOUBLE DEFAULT 0.0, " +
                            "INDEX idx_balance (balance), " +
                            "INDEX idx_name (player_name)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public double loadBalance(UUID uuid, String name, double defaultBalance) {
        String query = "SELECT balance FROM shards_data WHERE uuid = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    updatePlayerName(uuid, name);
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL error loading balance for: " + uuid, e);
        }

        saveBalance(uuid, name, defaultBalance);
        return defaultBalance;
    }

    private void updatePlayerName(UUID uuid, String name) {
        if (name == null || name.isEmpty()) return;
        String update = "UPDATE shards_data SET player_name = ? WHERE uuid = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    @Override
    public void saveBalance(UUID uuid, String name, double balance) {
        String query = "INSERT INTO shards_data (uuid, player_name, balance) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE balance = VALUES(balance), player_name = VALUES(player_name);";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name != null ? name : "Unknown");
            ps.setDouble(3, balance);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL error saving balance for: " + uuid, e);
        }
    }

    @Override
    public List<TopEntry> getTop(int limit) {
        List<TopEntry> list = new ArrayList<>();
        String query = "SELECT uuid, player_name, balance FROM shards_data ORDER BY balance DESC LIMIT ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID u = UUID.fromString(rs.getString("uuid"));
                    String pName = rs.getString("player_name");
                    double bal = rs.getDouble("balance");
                    list.add(new TopEntry(u, pName, bal));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL error fetching top list: ", e);
        }
        return list;
    }

    @Override
    public UUID getUuidByName(String name) {
        if (name == null) return null;
        String query = "SELECT uuid FROM shards_data WHERE LOWER(player_name) = LOWER(?) LIMIT 1;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL error searching UUID for name: " + name, e);
        }
        return null;
    }
}
