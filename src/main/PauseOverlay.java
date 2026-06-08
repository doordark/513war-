package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 暂停弹窗 —— 类似植物大战僵尸的暂停菜单。
 * 使用 JavaFX 组件实现，覆盖在游戏画布上方。
 */
public class PauseOverlay extends VBox {

    private final GamePanel gamePanel;

    private Button continueBtn;
    private Button menuBtn;
    private Button settingsBtn;

    public PauseOverlay(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(30));
        setPrefSize(960, 600);

        // 半透明背景
        setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.75);" +
            "-fx-background-radius: 0;"
        );

        // 标题
        Label titleLabel = new Label("游戏暂停");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(100,150,255,0.5), 10, 0, 0, 0);");

        // 当前波次信息
        Label waveInfo = new Label();
        waveInfo.setFont(Font.font("Microsoft YaHei", 14));
        waveInfo.setTextFill(Color.color(0.7, 0.8, 1, 0.8));

        // 按钮容器
        VBox btnContainer = new VBox();
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(12);

        continueBtn = createButton("继续游戏", "#4299e1", e -> {
            gamePanel.resumeGame();
            setVisible(false);
            setManaged(false);
        });

        menuBtn = createButton("返回主菜单", "#ed8936", e -> {
            gamePanel.saveGame();
            gamePanel.returnToMenu();
            setVisible(false);
            setManaged(false);
        });

        settingsBtn = createButton("游戏设置", "#9f7aea", e -> {
            gamePanel.pauseGame();
            setVisible(false);
            setManaged(false);
            gamePanel.showSettingsFromPause();
        });

        btnContainer.getChildren().addAll(continueBtn, menuBtn, settingsBtn);

        getChildren().addAll(titleLabel, waveInfo, btnContainer);

        // 初始隐藏
        setVisible(false);
        setManaged(false);
    }

    private Button createButton(String text, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setPrefSize(220, 48);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + color + ", " + darkenColor(color) + ");" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #00000044, 6, 0, 0, 2);"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + lightenColor(color) + ", " + color + ");" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(255,255,255,0.5);" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 2;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + color + "aa, 12, 0, 0, 0);"
            );
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + color + ", " + darkenColor(color) + ");" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(255,255,255,0.2);" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, #00000044, 6, 0, 0, 2);"
            );
        });

        btn.setOnAction(handler);
        return btn;
    }

    private String darkenColor(String hex) {
        // Simple darken: reduce each component by 20%
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        r = Math.max(0, r - 40);
        g = Math.max(0, g - 40);
        b = Math.max(0, b - 40);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private String lightenColor(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        r = Math.min(255, r + 40);
        g = Math.min(255, g + 40);
        b = Math.min(255, b + 40);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public void show() {
        setVisible(true);
        setManaged(true);
    }
}
