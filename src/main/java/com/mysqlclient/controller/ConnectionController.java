package com.mysqlclient.controller;

import com.mysqlclient.db.DatabaseManager;
import com.mysqlclient.model.ConnectionInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    private MainController mainController;
    private DatabaseManager databaseManager;

    public ConnectionController() {
        databaseManager = new DatabaseManager();
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
}
