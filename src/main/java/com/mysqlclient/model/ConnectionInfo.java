package com.mysqlclient.model;

import java.util.Objects;

public class ConnectionInfo {
    private String name;
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;

    public ConnectionInfo() {
        this.host = "localhost";
        this.port = 3306;
        this.username = "root";
    }

    public ConnectionInfo(String name, String host, int port, String username, String password, String database) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getJdbcUrl() {
        StringBuilder sb = new StringBuilder("jdbc:mysql://");
        sb.append(host).append(":").append(port);
        if (database != null && !database.isEmpty()) {
            sb.append("/").append(database);
        }
        sb.append("?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionInfo that = (ConnectionInfo) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(username, that.username) &&
                Objects.equals(database, that.database);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, username, database);
    }

    @Override
    public String toString() {
        return name != null ? name : username + "@" + host + ":" + port;
    }
}
