package com.mysqlclient.db;

import com.mysqlclient.model.ConnectionInfo;
import com.mysqlclient.model.QueryResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {
    private Connection connection;

    public QueryExecutor(Connection connection) {
        this.connection = connection;
    }

    public QueryResult executeQuery(String sql) {
        QueryResult result = new QueryResult();
        long startTime = System.currentTimeMillis();

        try {
            sql = sql.trim();
            boolean isSelect = sql.toLowerCase().startsWith("select") ||
                    sql.toLowerCase().startsWith("show") ||
                    sql.toLowerCase().startsWith("describe") ||
                    sql.toLowerCase().startsWith("desc");

            if (isSelect) {
                ResultSet rs = connection.createStatement().executeQuery(sql);
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    result.getColumns().add(metaData.getColumnLabel(i));
                }

                List<String> rowData;
                while (rs.next()) {
                    rowData = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        rowData.add(value != null ? value.toString() : null);
                    }
                    result.addRow(rowData);
                }

                result.setRowCount(result.getRows().size());
                rs.close();
            } else {
                int affectedRows = connection.createStatement().executeUpdate(sql);
                result.setAffectedRows(affectedRows);
                result.setMessage("操作成功，影响 " + affectedRows + " 行");
            }

            result.setSuccess(true);
            result.setExecutionTime(System.currentTimeMillis() - startTime);

        } catch (SQLException e) {
            result.setSuccess(false);
            result.setMessage("SQL执行错误: " + e.getMessage());
            result.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    public QueryResult executeMultiple(String sqlScript) {
        QueryResult result = new QueryResult();
        String[] statements = sqlScript.split(";");
        StringBuilder allMessages = new StringBuilder();
        long startTime = System.currentTimeMillis();
        boolean hasError = false;

        for (String sql : statements) {
            sql = sql.trim();
            if (sql.isEmpty()) continue;

            QueryResult singleResult = executeQuery(sql);
            if (!singleResult.isSuccess()) {
                allMessages.append("错误: ").append(singleResult.getMessage()).append("\n");
                hasError = true;
            } else if (singleResult.getMessage() != null) {
                allMessages.append(singleResult.getMessage()).append("\n");
            }
        }

        result.setSuccess(!hasError);
        result.setMessage(allMessages.toString().trim());
        result.setExecutionTime(System.currentTimeMillis() - startTime);

        return result;
    }

    public void close() {
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
}
