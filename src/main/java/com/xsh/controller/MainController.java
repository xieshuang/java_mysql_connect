package com.xsh.controller;

import com.xsh.db.DatabaseManager;
import com.xsh.db.QueryExecutor;
import com.xsh.model.ConnectionInfo;
import com.xsh.model.QueryResult;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

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

    private DatabaseManager databaseManager;
    private QueryExecutor queryExecutor;
    private ConnectionInfo currentConnection;

    @FXML
    public void initialize() {
        databaseManager = new DatabaseManager();
        updateConnectionStatus(false);
        initDatabaseTree();
    }

    private void initDatabaseTree() {
        TreeItem<String> root = new TreeItem<>("数据库");
        root.setExpanded(true);
        databaseTree.setRoot(root);
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
                dbItem.getChildren().add(new TreeItem<>(table));
            }
            root.getChildren().add(dbItem);
        }
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
            displayResult(result);
        } catch (Exception e) {
            showError("执行失败: " + e.getMessage());
        }
    }

    private void displayResult(QueryResult result) {
        if (!result.isSuccess()) {
            showError(result.getMessage());
            queryTime.setText("执行时间: " + result.getExecutionTime() + "ms");
            return;
        }

        showMessage(result.getMessage() != null ? result.getMessage() : "执行成功");
        queryTime.setText("执行时间: " + result.getExecutionTime() + "ms");

        resultTable.getColumns().clear();
        resultTable.getItems().clear();

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
        } else {
            resultInfo.setText(result.getAffectedRows() > 0 ? "影响 " + result.getAffectedRows() + " 行" : "");
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
        resultInfo.setText("");
        messageArea.clear();
        queryTime.setText("");
    }

    @FXML
    public void onTreeItemClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            TreeItem<String> item = databaseTree.getSelectionModel().getSelectedItem();
            if (item != null && item.getParent() != null && item.getParent().getParent() != null) {
                String tableName = item.getValue();
                String dbName = item.getParent().getValue();
                sqlEditor.setText("SELECT * FROM " + dbName + "." + tableName + " LIMIT 100");
                executeQuery();
            }
        }
    }

    @FXML
    public void saveConnection() {
        if (currentConnection == null) {
            showError("请先连接数据库");
            return;
        }
        showMessage("保存连接功能待实现");
    }

    @FXML
    public void exportResult() {
        if (resultTable.getItems().isEmpty()) {
            showError("没有可导出的数据");
            return;
        }
        showMessage("导出功能待实现");
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
