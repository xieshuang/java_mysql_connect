package com.xsh.controller;

import com.xsh.db.DatabaseManager;
import com.xsh.model.ConnectionInfo;
import com.xsh.util.ConnectionHistoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class ConnectionController {

    @FXML
    private TextField connectionName;
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField databaseField;
    @FXML
    private Label testResult;
    @FXML
    private ComboBox<String> savedConnections;
    @FXML
    private CheckBox saveConnectionCheckBox;

    private MainController mainController;
    private DatabaseManager databaseManager;
    private ObservableList<String> connectionNames;

    public ConnectionController() {
        databaseManager = new DatabaseManager();
        connectionNames = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        loadSavedConnections();
    }

    private void loadSavedConnections() {
        List<ConnectionInfo> connections = ConnectionHistoryManager.loadConnections();
        connectionNames.clear();
        connectionNames.add("-- 选择已保存的连接 --");
        for (ConnectionInfo info : connections) {
            connectionNames.add(info.toString());
        }
        savedConnections.setItems(connectionNames);
        savedConnections.getSelectionModel().select(0);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setConnectionInfo(ConnectionInfo info) {
        connectionName.setText(info.getName());
        hostField.setText(info.getHost());
        portField.setText(String.valueOf(info.getPort()));
        usernameField.setText(info.getUsername());
        passwordField.setText(info.getPassword());
        databaseField.setText(info.getDatabase());
    }

    @FXML
    public void onSavedConnectionSelected() {
        int index = savedConnections.getSelectionModel().getSelectedIndex();
        if (index <= 0) {
            return;
        }

        List<ConnectionInfo> connections = ConnectionHistoryManager.loadConnections();
        if (index - 1 < connections.size()) {
            ConnectionInfo info = connections.get(index - 1);
            setConnectionInfo(info);
            saveConnectionCheckBox.setSelected(true);
        }
    }

    @FXML
    public void deleteSavedConnection() {
        int index = savedConnections.getSelectionModel().getSelectedIndex();
        if (index <= 0) {
            showError("请先选择一个要删除的连接");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("确定要删除此连接吗？");
        confirm.setContentText("此操作不可撤销");

        if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            List<ConnectionInfo> connections = ConnectionHistoryManager.loadConnections();
            if (index - 1 < connections.size()) {
                ConnectionHistoryManager.deleteConnection(connections.get(index - 1));
                loadSavedConnections();
                clearFields();
                showInfo("连接已删除");
            }
        }
    }

    private void clearFields() {
        connectionName.setText("");
        hostField.setText("localhost");
        portField.setText("3306");
        usernameField.setText("root");
        passwordField.setText("");
        databaseField.setText("");
        saveConnectionCheckBox.setSelected(false);
    }

    @FXML
    public void testConnection() {
        ConnectionInfo info = buildConnectionInfo();
        boolean success = databaseManager.testConnection(info);
        
        if (success) {
            testResult.setText("连接成功!");
            testResult.getStyleClass().remove("error-text");
            testResult.getStyleClass().add("success-text");
        } else {
            testResult.setText("连接失败!");
            testResult.getStyleClass().remove("success-text");
            testResult.getStyleClass().add("error-text");
        }
    }

    @FXML
    public void connect() {
        ConnectionInfo info = buildConnectionInfo();
        
        if (hostField.getText().isEmpty() || usernameField.getText().isEmpty()) {
            showError("请填写主机和用户名");
            return;
        }

        if (saveConnectionCheckBox.isSelected()) {
            ConnectionHistoryManager.saveConnection(info);
        }

        mainController.connect(info);
        closeDialog();
    }

    @FXML
    public void cancel() {
        closeDialog();
    }

    private ConnectionInfo buildConnectionInfo() {
        ConnectionInfo info = new ConnectionInfo();
        info.setName(connectionName.getText());
        info.setHost(hostField.getText());
        try {
            info.setPort(Integer.parseInt(portField.getText()));
        } catch (NumberFormatException e) {
            info.setPort(3306);
        }
        info.setUsername(usernameField.getText());
        info.setPassword(passwordField.getText());
        info.setDatabase(databaseField.getText());
        return info;
    }

    private void closeDialog() {
        Stage stage = (Stage) hostField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("输入错误");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
