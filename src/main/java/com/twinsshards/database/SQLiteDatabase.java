package com.twinsshards.database;

import com.twinsshards.TwinsShards;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteDatabase implements DatabaseManager {

    private final TwinsShards plugin;
    private final File dbFile;
    private Connection connection;

    public SQLiteDatabase(TwinsShards plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "database.db");
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite JDBC driver not found!", e);
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
        return connection;
    }

    @Override
    public void init() throws Exception {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS shards_data (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "player_name VARCHAR(16), " +
                            "balance DOUBLE DEFAULT 0.0" +
                            ");"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_shards_balance ON shards_data (balance DESC);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_shards_name ON shards_data (player_name);");
        }
    }

    @Override
    public synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing SQLite connection: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized double loadBalance(UUID uuid, String name, double defaultBalance) {
        String query = "SELECT balance FROM shards_data WHERE uuid = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    updatePlayerName(uuid, name);
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading balance for: " + uuid, e);
        }

        saveBalance(uuid, name, defaultBalance);
        return defaultBalance;
    }

    private void updatePlayerName(UUID uuid, String name) {
        if (name == null || name.isEmpty()) return;
        String update = "UPDATE shards_data SET player_name = ? WHERE uuid = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    @Override
    public synchronized void saveBalance(UUID uuid, String name, double balance) {
        String query = "INSERT INTO shards_data (uuid, player_name, balance) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance, player_name = excluded.player_name;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name != null ? name : "Unknown");
            ps.setDouble(3, balance);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving balance for: " + uuid, e);
        }
    }

    @Override
    public synchronized List<TopEntry> getTop(int limit) {
        List<TopEntry> list = new ArrayList<>();
        String query = "SELECT uuid, player_name, balance FROM shards_data ORDER BY balance DESC LIMIT ?;";
        try (Connection conn = getConnection();
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
            plugin.getLogger().log(Level.SEVERE, "Error fetching top leaderboard: ", e);
        }
        return list;
    }

    @Override
    public synchronized UUID getUuidByName(String name) {
        if (name == null) return null;
        String query = "SELECT uuid FROM shards_data WHERE LOWER(player_name) = LOWER(?) LIMIT 1;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error finding UUID from name: " + name, e);
        }
        return null;
    }
}
