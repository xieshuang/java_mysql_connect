package com.xsh.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.ArrayList;

public class QueryResult {
    private List<String> columns;
    private ObservableList<ObservableList<StringProperty>> rows;
    private int rowCount;
    private long executionTime;
    private String message;
    private boolean success;
    private int affectedRows;

    public QueryResult() {
        this.columns = new ArrayList<>();
        this.rows = FXCollections.observableArrayList();
        this.rowCount = 0;
        this.executionTime = 0;
        this.success = true;
        this.affectedRows = 0;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public ObservableList<ObservableList<StringProperty>> getRows() {
        return rows;
    }

    public void setRows(ObservableList<ObservableList<StringProperty>> rows) {
        this.rows = rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    public void addRow(List<String> rowData) {
        ObservableList<StringProperty> row = FXCollections.observableArrayList();
        for (String value : rowData) {
            row.add(new SimpleStringProperty(value != null ? value : "NULL"));
        }
        rows.add(row);
    }
}
