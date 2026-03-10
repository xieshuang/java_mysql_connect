package com.xsh.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xsh.model.ConnectionInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHistoryManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".mysql_client";
    private static final String CONNECTIONS_FILE = CONFIG_DIR + File.separator + "connections.json";
    private static final String SQL_HISTORY_FILE = CONFIG_DIR + File.separator + "sql_history.json";
    
    private static final int MAX_HISTORY_SIZE = 100;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveConnection(ConnectionInfo connectionInfo) {
        List<ConnectionInfo> connections = loadConnections();
        
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).equals(connectionInfo)) {
                connections.remove(i);
                break;
            }
        }
        
        connections.add(0, connectionInfo);
        
        if (connections.size() > MAX_HISTORY_SIZE) {
            connections = connections.subList(0, MAX_HISTORY_SIZE);
        }
        
        saveConnections(connections);
    }

    public static List<ConnectionInfo> loadConnections() {
        File file = new File(CONNECTIONS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<ConnectionInfo>>(){}.getType();
            List<ConnectionInfo> connections = gson.fromJson(reader, listType);
            return connections != null ? connections : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void deleteConnection(ConnectionInfo connectionInfo) {
        List<ConnectionInfo> connections = loadConnections();
        connections.removeIf(c -> c.equals(connectionInfo));
        saveConnections(connections);
    }

    private static void saveConnections(List<ConnectionInfo> connections) {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONNECTIONS_FILE)) {
            gson.toJson(connections, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSqlHistory(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }
        
        List<String> history = loadSqlHistory();
        history.add(0, sql.trim());
        
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }
        
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(SQL_HISTORY_FILE)) {
            gson.toJson(history, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadSqlHistory() {
        File file = new File(SQL_HISTORY_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            List<String> history = gson.fromJson(reader, listType);
            return history != null ? history : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void clearSqlHistory() {
        File file = new File(SQL_HISTORY_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
