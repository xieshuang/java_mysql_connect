package com.xsh.controller;

import com.xsh.db.DatabaseManager;
import com.xsh.db.QueryExecutor;
import com.xsh.model.ConnectionInfo;
import com.xsh.model.QueryResult;
import com.xsh.util.ConnectionHistoryManager;
import com.xsh.util.CsvExporter;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    private TextArea sqlEditor;
    @FXML
    private TableView<ObservableList<StringProperty>> resultTable;
    @FXML
    private TextArea messageArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label connectionInfoLabel;
    @FXML
    private Label queryTime;
    @FXML
    private Label resultInfo;
    @FXML
    private TreeView<String> databaseTree;
    @FXML
    private ListView<String> sqlHistoryList;

    private DatabaseManager databaseManager;
    private QueryExecutor queryExecutor;
    private ConnectionInfo currentConnection;
    private List<String> currentColumns;

    @FXML
    public void initialize() {
        databaseManager = new DatabaseManager();
        updateConnectionStatus(false);
        initDatabaseTree();
        initSqlHistory();
        initResultTableContextMenu();
    }

    private void initResultTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyRow = new MenuItem("复制选中行");
        copyRow.setOnAction(e -> copySelectedRow());
        MenuItem exportRow = new MenuItem("导出选中行");
        exportRow.setOnAction(e -> exportSelectedRow());
        
        contextMenu.getItems().addAll(copyRow, exportRow);
        resultTable.setContextMenu(contextMenu);
    }

    private void initDatabaseTree() {
        TreeItem<String> root = new TreeItem<>("数据库");
        root.setExpanded(true);
        databaseTree.setRoot(root);
    }

    private void initSqlHistory() {
        List<String> history = ConnectionHistoryManager.loadSqlHistory();
        ObservableList<String> items = FXCollections.observableArrayList(history);
        sqlHistoryList.setItems(items);
    }

    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            statusLabel.setText("已连接");
            statusLabel.getStyleClass().remove("status-disconnected");
            statusLabel.getStyleClass().add("status-connected");
        } else {
            statusLabel.setText("未连接");
            statusLabel.getStyleClass().remove("status-connected");
            statusLabel.getStyleClass().add("status-disconnected");
            connectionInfoLabel.setText("未连接");
        }
    }

    @FXML
    public void showConnectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConnectionDialog.fxml"));
            Parent root = loader.load();
            
            ConnectionController controller = loader.getController();
            controller.setMainController(this);
            if (currentConnection != null) {
                controller.setConnectionInfo(currentConnection);
            }

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("新建连接");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            showError("打开连接对话框失败: " + e.getMessage());
        }
    }

    public void connect(ConnectionInfo connInfo) {
        boolean success = databaseManager.connect(connInfo);
        if (success) {
            currentConnection = connInfo;
            queryExecutor = new QueryExecutor(databaseManager.getConnection());
            updateConnectionStatus(true);
            connectionInfoLabel.setText(connInfo.toString());
            showMessage("连接成功: " + connInfo.getJdbcUrl());
            refreshDatabaseTree();
        } else {
            showError("连接失败，请检查连接信息是否正确");
        }
    }

    private void refreshDatabaseTree() {
        TreeItem<String> root = databaseTree.getRoot();
        root.getChildren().clear();

        List<String> databases = databaseManager.getDatabases();
        for (String db : databases) {
            if (db.equals("information_schema") || db.equals("mysql") || 
                db.equals("performance_schema") || db.equals("sys")) {
                continue;
            }
            TreeItem<String> dbItem = new TreeItem<>(db);
            
            List<String> tables = databaseManager.getTables(db);
            for (String table : tables) {
                TreeItem<String> tableItem = new TreeItem<>(table);
                dbItem.getChildren().add(tableItem);
            }
            root.getChildren().add(dbItem);
        }
    }

    private void showTableStructure(String database, String table) {
        List<DatabaseManager.ColumnInfo> columns = databaseManager.getColumns(table);
        
        StringBuilder sb = new StringBuilder();
        sb.append("表名: ").append(database).append(".").append(table).append("\n\n");
        sb.append(String.format("%-20s %-20s %-10s %-10s %s\n", 
            "字段名", "类型", "可空", "键", "备注"));
        sb.append("----------------------------------------------------------------\n");
        
        for (DatabaseManager.ColumnInfo col : columns) {
            sb.append(String.format("%-20s %-20s %-10s %-10s %s\n",
                col.getName(),
                col.getType(),
                col.isNullable() ? "YES" : "NO",
                col.getKey() != null ? col.getKey() : "",
                col.getExtra() != null ? col.getExtra() : ""));
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("表结构");
        alert.setHeaderText("表: " + table);
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @FXML
    public void disconnect() {
        if (databaseManager.isConnected()) {
            databaseManager.disconnect();
            queryExecutor = null;
            currentConnection = null;
            updateConnectionStatus(false);
            initDatabaseTree();
            clearResult();
            showMessage("已断开连接");
        }
    }

    @FXML
    public void executeQuery() {
        String sql = sqlEditor.getText().trim();
        if (sql.isEmpty()) {
            showError("请输入SQL语句");
            return;
        }

        if (!databaseManager.isConnected()) {
            showError("请先连接数据库");
            return;
        }

        try {
            QueryResult result = queryExecutor.executeQuery(sql);
            displayResult(result, sql);
        } catch (Exception e) {
            showError("执行失败: " + e.getMessage());
        }
    }

    private void displayResult(QueryResult result, String sql) {
        if (!result.isSuccess()) {
            showError(result.getMessage());
            queryTime.setText("执行时间: " + result.getExecutionTime() + "ms");
            return;
        }

        showMessage(result.getMessage() != null ? result.getMessage() : "执行成功");
        queryTime.setText("执行时间: " + result.getExecutionTime() + "ms");

        resultTable.getColumns().clear();
        resultTable.getItems().clear();
        currentColumns = result.getColumns();

        for (String column : result.getColumns()) {
            TableColumn<ObservableList<StringProperty>, String> col = new TableColumn<>(column);
            final int index = result.getColumns().indexOf(column);
            col.setCellValueFactory(param -> {
                if (index < param.getValue().size()) {
                    return param.getValue().get(index);
                }
                return null;
            });
            resultTable.getColumns().add(col);
        }

        resultTable.setItems(result.getRows());
        
        if (result.getRowCount() > 0) {
            resultInfo.setText("返回 " + result.getRowCount() + " 行");
            ConnectionHistoryManager.saveSqlHistory(sql);
            refreshSqlHistory();
        } else {
            resultInfo.setText(result.getAffectedRows() > 0 ? "影响 " + result.getAffectedRows() + " 行" : "");
            if (!sql.toLowerCase().startsWith("select")) {
                ConnectionHistoryManager.saveSqlHistory(sql);
                refreshSqlHistory();
            }
        }
    }

    private void refreshSqlHistory() {
        List<String> history = ConnectionHistoryManager.loadSqlHistory();
        ObservableList<String> items = FXCollections.observableArrayList(history);
        sqlHistoryList.setItems(items);
    }

    @FXML
    public void onHistoryItemClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String sql = sqlHistoryList.getSelectionModel().getSelectedItem();
            if (sql != null) {
                sqlEditor.setText(sql);
            }
        }
    }

    @FXML
    public void clearSqlHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认清空");
        confirm.setHeaderText("确定要清空SQL历史记录吗？");
        
        if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            ConnectionHistoryManager.clearSqlHistory();
            refreshSqlHistory();
            showMessage("历史记录已清空");
        }
    }

    @FXML
    public void clearEditor() {
        sqlEditor.clear();
    }

    @FXML
    public void clearResult() {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
        currentColumns = null;
        resultInfo.setText("");
        messageArea.clear();
        queryTime.setText("");
    }

    @FXML
    public void onTreeItemClicked(MouseEvent event) {
        TreeItem<String> item = databaseTree.getSelectionModel().getSelectedItem();
        if (item == null || item.getParent() == null || item.getParent().getParent() == null) {
            return;
        }

        String tableName = item.getValue();
        String dbName = item.getParent().getValue();

        if (event.getClickCount() == 2) {
            sqlEditor.setText("SELECT * FROM " + dbName + "." + tableName + " LIMIT 100");
            executeQuery();
        } else if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem viewStructure = new MenuItem("查看表结构");
            viewStructure.setOnAction(e -> showTableStructure(dbName, tableName));
            
            MenuItem selectAll = new MenuItem("查询全部数据");
            selectAll.setOnAction(e -> {
                sqlEditor.setText("SELECT * FROM " + dbName + "." + tableName + " LIMIT 100");
                executeQuery();
            });
            
            MenuItem selectTop10 = new MenuItem("查询前10条");
            selectTop10.setOnAction(e -> {
                sqlEditor.setText("SELECT * FROM " + dbName + "." + tableName + " LIMIT 10");
                executeQuery();
            });
            
            contextMenu.getItems().addAll(viewStructure, selectAll, selectTop10);
            contextMenu.show(databaseTree, event.getScreenX(), event.getScreenY());
        }
    }

    @FXML
    public void exportResult() {
        if (resultTable.getItems().isEmpty()) {
            showError("没有可导出的数据");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出CSV文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );
        fileChooser.setInitialFileName("query_result.csv");

        File file = fileChooser.showSaveDialog(resultTable.getScene().getWindow());
        if (file != null) {
            boolean success = CsvExporter.exportToCsv(
                resultTable.getItems(), 
                currentColumns, 
                file
            );
            
            if (success) {
                showMessage("导出成功: " + file.getAbsolutePath());
            } else {
                showError("导出失败");
            }
        }
    }

    @FXML
    public void copySelectedRow() {
        ObservableList<ObservableList<StringProperty>> selectedItems = resultTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showError("请先选择要复制的数据");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (ObservableList<StringProperty> row : selectedItems) {
            for (int i = 0; i < row.size(); i++) {
                sb.append(row.get(i).getValue());
                if (i < row.size() - 1) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }

        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(sb.toString());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
        
        showMessage("已复制到剪贴板");
    }

    @FXML
    public void exportSelectedRow() {
        ObservableList<ObservableList<StringProperty>> selectedItems = resultTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showError("请先选择要导出的数据");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出选中行");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );
        fileChooser.setInitialFileName("selected_rows.csv");

        File file = fileChooser.showSaveDialog(resultTable.getScene().getWindow());
        if (file != null) {
            ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList(selectedItems);
            boolean success = CsvExporter.exportToCsv(data, currentColumns, file);
            
            if (success) {
                showMessage("导出成功: " + file.getAbsolutePath());
            } else {
                showError("导出失败");
            }
        }
    }

    @FXML
    public void formatSql() {
        String sql = sqlEditor.getText();
        sql = sql.replaceAll("\\s+", " ").trim();
        sqlEditor.setText(sql);
    }

    @FXML
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("MySQL Client");
        alert.setContentText("版本: 1.0.0\n一个简单的JavaFX MySQL客户端工具");
        alert.showAndWait();
    }

    @FXML
    public void exitApp() {
        disconnect();
        Platform.exit();
    }

    private void showMessage(String message) {
        messageArea.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("操作失败");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
