package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 名字输入对话框 —— 游戏结束后让玩家输入名字保存分数。
 */
public class NameInputDialog {

    private String playerName;

    public String show(String title, String message, int finalScore) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(16);
        root.setPadding(new Insets(24, 32, 24, 32));
        root.setPrefWidth(360);
        root.setStyle(
            "-fx-background-color: rgba(20, 25, 40, 0.97);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(100, 140, 200, 0.3);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 4);"
        );

        // 标题
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        // 分数显示
        Label scoreLabel = new Label("最终得分: " + finalScore);
        scoreLabel.setFont(Font.font("Microsoft YaHei", 14));
        scoreLabel.setTextFill(Color.web("#ffd700"));

        // 提示
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Microsoft YaHei", 13));
        msgLabel.setTextFill(Color.color(0.6, 0.65, 0.75));
        msgLabel.setWrapText(true);

        // 输入框
        TextField nameField = new TextField();
        nameField.setPromptText("请输入你的名字...");
        nameField.setFont(Font.font("Microsoft YaHei", 14));
        nameField.setPrefWidth(280);
        nameField.setStyle(
            "-fx-background-color: rgba(40, 50, 70, 0.8);" +
            "-fx-background-radius: 6;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(150, 160, 180, 0.6);" +
            "-fx-border-color: rgba(100, 140, 200, 0.3);" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 8 12 8 12;"
        );
        nameField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                playerName = nameField.getText().trim();
                if (playerName.isEmpty()) playerName = "匿名玩家";
                dialog.close();
            }
        });

        // 按钮
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(12);

        Button confirmBtn = new Button("确认");
        confirmBtn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        confirmBtn.setTextFill(Color.WHITE);
        confirmBtn.setPrefSize(100, 36);
        confirmBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #66bb6a, #43a047);" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        confirmBtn.setOnAction(e -> {
            playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "匿名玩家";
            dialog.close();
        });

        Button skipBtn = new Button("跳过");
        skipBtn.setFont(Font.font("Microsoft YaHei", 13));
        skipBtn.setTextFill(Color.color(0.6, 0.65, 0.75));
        skipBtn.setPrefSize(100, 36);
        skipBtn.setStyle(
            "-fx-background-color: rgba(50, 55, 70, 0.6);" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(90, 100, 120, 0.2);" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        skipBtn.setOnAction(e -> {
            playerName = "匿名玩家";
            dialog.close();
        });

        buttonBox.getChildren().addAll(confirmBtn, skipBtn);

        root.getChildren().addAll(titleLabel, scoreLabel, msgLabel, nameField, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        playerName = null;
        dialog.showAndWait();
        return playerName;
    }
}
