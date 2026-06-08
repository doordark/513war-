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
 * 难度选择面板 — JavaFX 组件，匹配主菜单风格
 */
public class DifficultySelectOverlay extends StackPane {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 600;

    private final GamePanel gamePanel;

    // 背景层（预留接口）
    private Pane backgroundLayer;

    // UI 层
    private VBox uiLayer;

    // 按钮
    private Button easyBtn;
    private Button normalBtn;
    private Button hardBtn;
    private Button nightmareBtn;
    private Button backBtn;

    public DifficultySelectOverlay(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        setPrefSize(WIDTH, HEIGHT);
        setAlignment(Pos.TOP_LEFT);
        setVisible(false);
        setManaged(false);

        // 加载 CSS
        String cssPath = getClass().getResource("/styles/game.css").toExternalForm();
        getStylesheets().add(cssPath);

        // ===== 背景层（预留接口） =====
        backgroundLayer = new Pane();
        backgroundLayer.setPrefSize(WIDTH, HEIGHT);
        backgroundLayer.setMinSize(WIDTH, HEIGHT);
        backgroundLayer.setMaxSize(WIDTH, HEIGHT);
        backgroundLayer.setStyle("-fx-background-color: #0a0a1e;");

        // ===== UI 层 =====
        uiLayer = new VBox();
        uiLayer.setPrefSize(WIDTH, HEIGHT);
        uiLayer.setAlignment(Pos.TOP_CENTER);
        uiLayer.setPadding(new Insets(0));
        uiLayer.setSpacing(0);

        // 标题
        VBox titleArea = new VBox();
        titleArea.setAlignment(Pos.CENTER);
        titleArea.setPadding(new Insets(50, 0, 30, 0));

        Label titleLabel = new Label("选择难度");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(4, 0, 2, Color.color(0, 0, 0, 0.35)));

        // 标题呼吸动画
        setupTitleAnimation(titleLabel);
        titleArea.getChildren().add(titleLabel);

        // 按钮列表
        VBox btnBox = new VBox();
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(14);
        btnBox.setPadding(new Insets(0, 0, 30, 0));

        easyBtn = createDiffButton("简单", "HP×1.0  速度×0.8", "#48bb78", "#38a169", e -> {
            gamePanel.getSettings().setDifficulty(GameSettings.Difficulty.EASY);
            System.out.println("[设置] 难度=简单");
            startGame();
        });

        normalBtn = createDiffButton("普通", "HP×1.0  速度×1.0", "#4299e1", "#3182ce", e -> {
            gamePanel.getSettings().setDifficulty(GameSettings.Difficulty.NORMAL);
            System.out.println("[设置] 难度=普通");
            startGame();
        });

        hardBtn = createDiffButton("困难", "HP×1.5  速度×1.2", "#ed8936", "#dd6b20", e -> {
            gamePanel.getSettings().setDifficulty(GameSettings.Difficulty.HARD);
            System.out.println("[设置] 难度=困难");
            startGame();
        });

        nightmareBtn = createDiffButton("噩梦", "HP×2.0  速度×1.5", "#e53e3e", "#c53030", e -> {
            gamePanel.getSettings().setDifficulty(GameSettings.Difficulty.NIGHTMARE);
            System.out.println("[设置] 难度=噩梦");
            startGame();
        });

        btnBox.getChildren().addAll(easyBtn, normalBtn, hardBtn, nightmareBtn);

        // 返回按钮
        backBtn = createBackButton("← 返回", e -> returnToMenu());

        uiLayer.getChildren().addAll(titleArea, btnBox, backBtn);

        getChildren().addAll(backgroundLayer, uiLayer);
    }

    /** 标题呼吸发光特效 */
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

    /** 创建难度按钮 */
    private Button createDiffButton(String name, String desc, String colorNormal, String colorHover,
                                    javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
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

        // 渐变背景
        btn.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
            "-fx-background-radius: 14; " +
            "-fx-border-color: rgba(255,255,255,0.2); " +
            "-fx-border-radius: 14; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 3);",
            colorNormal, colorHover));

        // 初始状态（入场动画用）
        btn.setOpacity(0);
        btn.setTranslateY(20);

        // 悬停动画
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(180), btn);
        scaleUp.setToX(1.06);
        scaleUp.setToY(1.06);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            scaleDown.stop();
            btn.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                "-fx-background-radius: 14; " +
                "-fx-border-color: rgba(255,255,255,0.4); " +
                "-fx-border-radius: 14; " +
                "-fx-border-width: 1.5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);",
                lightenColor(colorNormal), colorNormal));
            scaleUp.playFromStart();
        });

        btn.setOnMouseExited(e -> {
            scaleUp.stop();
            btn.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                "-fx-background-radius: 14; " +
                "-fx-border-color: rgba(255,255,255,0.2); " +
                "-fx-border-radius: 14; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 3);",
                colorNormal, colorHover));
            scaleDown.playFromStart();
        });

        btn.setOnAction(handler);
        return btn;
    }

    /** 创建返回按钮 */
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

    /** 颜色变亮辅助方法 */
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

    private void startGame() {
        setVisible(false);
        setManaged(false);
        gamePanel.startGame(1);
    }

    private void returnToMenu() {
        gamePanel.returnToMenuFromOverlay();
        setVisible(false);
        setManaged(false);
    }

    /** 显示面板并播放入场动画 */
    public void show() {
        setVisible(true);
        setManaged(true);

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
    }
}
