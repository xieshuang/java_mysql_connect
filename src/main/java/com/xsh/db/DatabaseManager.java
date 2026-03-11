package com.xsh.db;

import com.xsh.model.ConnectionInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;
    private ConnectionInfo currentConnectionInfo;

    public boolean connect(ConnectionInfo connectionInfo) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    connectionInfo.getJdbcUrl(),
                    connectionInfo.getUsername(),
                    connectionInfo.getPassword()
            );
            currentConnectionInfo = connectionInfo;
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("连接失败: " + e.getMessage());
            return false;
        }
    }

    public boolean testConnection(ConnectionInfo connectionInfo) {
        Connection testConn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            testConn = DriverManager.getConnection(
                    connectionInfo.getJdbcUrl(),
                    connectionInfo.getUsername(),
                    connectionInfo.getPassword()
            );
            return testConn != null;
        } catch (Exception e) {
            return false;
        } finally {
            if (testConn != null) {
                try {
                    testConn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<String> getDatabases() {
        List<String> databases = new ArrayList<>();
        if (connection == null) return databases;

        String sql = "SHOW DATABASES";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return databases;
    }

    public List<String> getTables(String database) {
        List<String> tables = new ArrayList<>();
        if (connection == null) return tables;

        String sql = "SHOW TABLES FROM `" + database + "`";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public boolean useDatabase(String database) {
        if (connection == null) return false;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE `" + database + "`");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentDatabase() {
        if (connection == null) return null;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ColumnInfo> getColumns(String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();
        if (connection == null) return columns;

        String sql = "DESCRIBE " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ColumnInfo info = new ColumnInfo();
                info.setName(rs.getString("Field"));
                info.setType(rs.getString("Type"));
                info.setNullable(rs.getString("Null").equals("YES"));
                info.setKey(rs.getString("Key"));
                info.setDefaultValue(rs.getString("Default"));
                info.setExtra(rs.getString("Extra"));
                columns.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }

    public String getCreateTableStatement(String database, String tableName) {
        if (connection == null) return null;
        String sql = "SHOW CREATE TABLE `" + tableName + "`";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ConnectionInfo getCurrentConnectionInfo() {
        return currentConnectionInfo;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static class ColumnInfo {
        private String name;
        private String type;
        private boolean nullable;
        private String key;
        private String defaultValue;
        private String extra;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public String getExtra() { return extra; }
        public void setExtra(String extra) { this.extra = extra; }
    }
}
