package main;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * 难度选择面板 — 设置面板，选择后点保存返回主菜单
 * 流程：选难度（高亮）→ 点保存 → 返回主菜单 → 开始游戏用保存的难度
 */
public class DifficultySelectOverlay extends StackPane {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 600;

    private final GamePanel gamePanel;

    private Pane backgroundLayer;
    private VBox uiLayer;

    private Button easyBtn;
    private Button normalBtn;
    private Button hardBtn;
    private Button nightmareBtn;
    private Button saveBtn;
    private Button backBtn;

    // 当前选中的难度（高亮）
    private int selectedIndex = 1; // 默认普通

    private final String[] colorNormal = {"#48bb78", "#4299e1", "#ed8936", "#e53e3e"};
    private final String[] colorHover  = {"#38a169", "#3182ce", "#dd6b20", "#c53030"};
    private final String[] colorNames  = {"简单", "普通", "困难", "噩梦"};
    private final String[] colorDescs  = {"HP×1.0  速度×0.8", "HP×1.0  速度×1.0", "HP×1.5  速度×1.2", "HP×2.0  速度×1.5"};
    private final GameSettings.Difficulty[] difficulties = {
        GameSettings.Difficulty.EASY, GameSettings.Difficulty.NORMAL,
        GameSettings.Difficulty.HARD, GameSettings.Difficulty.NIGHTMARE
    };

    public DifficultySelectOverlay(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        setPrefSize(WIDTH, HEIGHT);
        setAlignment(Pos.TOP_LEFT);
        setVisible(false);
        setManaged(false);

        String cssPath = getClass().getResource("/styles/game.css").toExternalForm();
        getStylesheets().add(cssPath);

        backgroundLayer = new Pane();
        backgroundLayer.setPrefSize(WIDTH, HEIGHT);
        backgroundLayer.setMinSize(WIDTH, HEIGHT);
        backgroundLayer.setMaxSize(WIDTH, HEIGHT);
        backgroundLayer.setStyle("-fx-background-color: #0a0a1e;");

        uiLayer = new VBox();
        uiLayer.setPrefSize(WIDTH, HEIGHT);
        uiLayer.setAlignment(Pos.TOP_CENTER);
        uiLayer.setPadding(new Insets(0));
        uiLayer.setSpacing(0);

        // 左上角返回按钮
        backBtn = createBackButton("← 返回", e -> returnToMenu());
        StackPane topBar = new StackPane();
        topBar.setPrefSize(WIDTH, 60);
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        backBtn.setTranslateX(20);
        backBtn.setTranslateY(15);
        topBar.getChildren().add(backBtn);

        // 标题
        VBox titleArea = new VBox();
        titleArea.setAlignment(Pos.CENTER);
        titleArea.setPadding(new Insets(30, 0, 20, 0));

        Label titleLabel = new Label("选择难度");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(4, 0, 2, Color.color(0, 0, 0, 0.35)));
        setupTitleAnimation(titleLabel);
        titleArea.getChildren().add(titleLabel);

        Label hintLabel = new Label("选择难度后点击保存");
        hintLabel.setFont(Font.font("Microsoft YaHei", 14));
        hintLabel.setTextFill(Color.color(1, 1, 1, 0.5));
        titleArea.getChildren().add(hintLabel);

        // 难度按钮列表
        VBox btnBox = new VBox();
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(14);
        btnBox.setPadding(new Insets(10, 0, 20, 0));

        easyBtn = createDiffButton(0);
        normalBtn = createDiffButton(1);
        hardBtn = createDiffButton(2);
        nightmareBtn = createDiffButton(3);

        btnBox.getChildren().addAll(easyBtn, normalBtn, hardBtn, nightmareBtn);

        // 保存按钮（默认隐藏）
        saveBtn = createSaveButton("保存", e -> {
            GameSettings.Difficulty chosen = difficulties[selectedIndex];
            gamePanel.getSettings().setDifficulty(chosen);
            System.out.println("[难度设置] 已保存: " + colorNames[selectedIndex]);
            returnToMenu();
        });
        saveBtn.setVisible(false);
        saveBtn.setManaged(false);

        uiLayer.getChildren().addAll(topBar, titleArea, btnBox, saveBtn);

        getChildren().addAll(backgroundLayer, uiLayer);
    }

    private void setupTitleAnimation(Label titleLabel) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0.1, 0.3, 0.6, 0.5));
        shadow.setRadius(8);
        shadow.setOffsetY(2);

        Timeline anim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(shadow.radiusProperty(), 6),
                new KeyValue(shadow.colorProperty(), Color.color(0.1, 0.3, 0.6, 0.3))),
            new KeyFrame(Duration.seconds(2.5),
                new KeyValue(shadow.radiusProperty(), 14),
                new KeyValue(shadow.colorProperty(), Color.color(0.2, 0.5, 0.9, 0.6)))
        );
        anim.setCycleCount(Timeline.INDEFINITE);
        anim.setAutoReverse(true);

        titleLabel.setEffect(shadow);
        anim.play();
    }

    private Button createDiffButton(int index) {
        String name = colorNames[index];
        String desc = colorDescs[index];
        String colorNormal = this.colorNormal[index];
        String colorHover = this.colorHover[index];

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(4);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Microsoft YaHei", 13));
        descLabel.setTextFill(Color.color(1, 1, 1, 0.75));

        content.getChildren().addAll(nameLabel, descLabel);

        Button btn = new Button();
        btn.setPrefSize(260, 62);
        btn.setGraphic(content);
        btn.setCursor(javafx.scene.Cursor.HAND);

        String defaultStyle = String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
            "-fx-background-radius: 14; " +
            "-fx-border-color: rgba(255,255,255,0.2); " +
            "-fx-border-radius: 14; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 3);",
            colorNormal, colorHover);
        btn.setStyle(defaultStyle);

        btn.setOpacity(0);
        btn.setTranslateY(20);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(180), btn);
        scaleUp.setToX(1.06);
        scaleUp.setToY(1.06);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        final int finalIndex = index;

        btn.setOnMouseEntered(e -> {
            scaleDown.stop();
            if (finalIndex == selectedIndex) {
                // 已选中状态，保持金色边框
                applySelectedStyle(btn, finalIndex);
            } else {
                btn.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                    "-fx-background-radius: 14; " +
                    "-fx-border-color: rgba(255,255,255,0.4); " +
                    "-fx-border-radius: 14; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);",
                    lightenColor(colorNormal), colorNormal));
            }
            scaleUp.playFromStart();
        });

        btn.setOnMouseExited(e -> {
            scaleUp.stop();
            if (finalIndex == selectedIndex) {
                applySelectedStyle(btn, finalIndex);
            } else {
                btn.setStyle(defaultStyle);
            }
            scaleDown.playFromStart();
        });

        // 点击选择难度（高亮 + 显示保存按钮）
        btn.setOnAction(e -> selectDifficulty(finalIndex));

        return btn;
    }

    /** 选中某个难度 */
    private void selectDifficulty(int index) {
        selectedIndex = index;
        System.out.println("[难度设置] 已选中: " + colorNames[index]);

        Button[] buttons = {easyBtn, normalBtn, hardBtn, nightmareBtn};
        for (int i = 0; i < buttons.length; i++) {
            if (i == index) {
                applySelectedStyle(buttons[i], i);
            } else {
                applyNormalStyle(buttons[i], i);
            }
        }

        // 显示保存按钮
        saveBtn.setVisible(true);
        saveBtn.setManaged(true);
        saveBtn.setOpacity(0);
        saveBtn.setTranslateY(15);

        ParallelTransition pt = new ParallelTransition();
        FadeTransition fade = new FadeTransition(Duration.millis(300), saveBtn);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition trans = new TranslateTransition(Duration.millis(300), saveBtn);
        trans.setFromY(15);
        trans.setToY(0);
        pt.getChildren().addAll(fade, trans);
        pt.play();
    }

    /** 金色高亮样式 */
    private void applySelectedStyle(Button btn, int index) {
        btn.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
            "-fx-background-radius: 14; " +
            "-fx-border-color: #ffd700; " +
            "-fx-border-radius: 14; " +
            "-fx-border-width: 2.5; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.4), 12, 0, 0, 4);",
            lightenColor(colorNormal[index]), colorNormal[index]));
    }

    /** 普通样式 */
    private void applyNormalStyle(Button btn, int index) {
        btn.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
            "-fx-background-radius: 14; " +
            "-fx-border-color: rgba(255,255,255,0.2); " +
            "-fx-border-radius: 14; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 3);",
            colorNormal[index], colorHover[index]));
    }

    /** 保存按钮 */
    private Button createSaveButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setPrefSize(200, 50);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ffd700, #f0c000); " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: rgba(255,255,255,0.3); " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.3), 8, 0, 0, 3);");

        btn.setOpacity(0);
        btn.setTranslateY(15);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), btn);
        scaleUp.setToX(1.06);
        scaleUp.setToY(1.06);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            scaleDown.stop();
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffe44d, #ffd700); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: rgba(255,255,255,0.5); " +
                "-fx-border-radius: 12; " +
                "-fx-border-width: 1.5; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.5), 12, 0, 0, 4);");
            scaleUp.playFromStart();
        });

        btn.setOnMouseExited(e -> {
            scaleUp.stop();
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffd700, #f0c000); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-radius: 12; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.3), 8, 0, 0, 3);");
            scaleDown.playFromStart();
        });

        btn.setOnAction(handler);
        return btn;
    }

    /** 返回按钮 */
    private Button createBackButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setPrefSize(120, 42);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        btn.setTextFill(Color.WHITE);
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748); " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: rgba(255,255,255,0.15); " +
            "-fx-border-radius: 10; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        btn.setOpacity(0);
        btn.setTranslateY(15);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), btn);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            scaleDown.stop();
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #5a6578, #4a5568); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");
            scaleUp.playFromStart();
        });

        btn.setOnMouseExited(e -> {
            scaleUp.stop();
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255,255,255,0.15); " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");
            scaleDown.playFromStart();
        });

        btn.setOnAction(handler);
        return btn;
    }

    private String lightenColor(String hex) {
        try {
            Color c = Color.web(hex);
            double r = Math.min(1, c.getRed() + 0.15);
            double g = Math.min(1, c.getGreen() + 0.15);
            double b = Math.min(1, c.getBlue() + 0.15);
            return String.format("#%02x%02x%02x",
                (int)(r * 255), (int)(g * 255), (int)(b * 255));
        } catch (Exception e) {
            return hex;
        }
    }

    private void returnToMenu() {
        setVisible(false);
        setManaged(false);
        gamePanel.returnToMenuFromOverlay();
    }

    /** 显示面板并播放入场动画 */
    public void show() {
        setVisible(true);
        setManaged(true);

        // 同步当前已保存的难度为选中状态
        GameSettings.Difficulty current = gamePanel.getSettings().getDifficulty();
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i] == current) {
                selectedIndex = i;
                break;
            }
        }

        // 隐藏保存按钮
        saveBtn.setVisible(false);
        saveBtn.setManaged(false);

        SequentialTransition anim = new SequentialTransition();

        Button[] buttons = {easyBtn, normalBtn, hardBtn, nightmareBtn, backBtn};
        for (int i = 0; i < buttons.length; i++) {
            Button btn = buttons[i];
            btn.setOpacity(0);
            btn.setTranslateY(25);

            ParallelTransition pt = new ParallelTransition();

            FadeTransition fade = new FadeTransition(Duration.millis(400), btn);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition trans = new TranslateTransition(Duration.millis(400), btn);
            trans.setFromY(25);
            trans.setToY(0);

            pt.getChildren().addAll(fade, trans);
            anim.getChildren().add(pt);
        }
        anim.play();

        // 动画结束后，将当前已保存的难度高亮
        anim.setOnFinished(e -> {
            Button[] allBtns = {easyBtn, normalBtn, hardBtn, nightmareBtn};
            for (int i = 0; i < allBtns.length; i++) {
                if (i == selectedIndex) {
                    applySelectedStyle(allBtns[i], i);
                } else {
                    applyNormalStyle(allBtns[i], i);
                }
            }
        });
    }
}
