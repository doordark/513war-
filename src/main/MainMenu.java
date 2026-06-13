package main;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * 主菜单 — 参考"513保卫战"风格
 * 布局：左上头像 + 右上货币 + 居中标题 + 5个彩色按钮
 * 背景预留接口，用户可自行替换图片
 */
public class MainMenu extends StackPane {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 710; // 与场景高度一致，消除底部黑边

    private final GamePanel gamePanel;

    // 背景层（预留接口）
    private Pane backgroundLayer;
    private ImageView backgroundImageView;

    // UI 层
    private VBox uiLayer;

    // 顶部栏
    private HBox topBar;
    private Label hpLabel;
    private Label xpLabel;
    private Label levelLabel;
    private Label goldLabel;

    // 按钮
    private Button startBtn;
    private Button continueBtn;  // 继续游戏
    private Button endlessBtn;
    private Button difficultyBtn;
    private Button shopBtn;
    private Button leaderboardBtn;
    private Button settingsBtn;
    private Button exitBtn;

    public MainMenu(GamePanel gamePanel) {
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

        // 默认背景色（深空渐变）
        backgroundLayer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0a0a1a, #1a1a3e, #0d1b2a);"
        );

        // 背景图片接口 — 用户调用 setBackgroundImage(path) 替换
        backgroundImageView = new ImageView();
        backgroundImageView.setFitWidth(WIDTH);
        backgroundImageView.setFitHeight(HEIGHT);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setManaged(false);
        backgroundImageView.setVisible(false);

        backgroundLayer.getChildren().add(backgroundImageView);

        // ===== UI 层 =====
        uiLayer = new VBox();
        uiLayer.setPrefSize(WIDTH, HEIGHT);
        uiLayer.setAlignment(Pos.TOP_CENTER);
        uiLayer.setPadding(new Insets(0));
        uiLayer.setSpacing(0);

        // 顶部栏
        topBar = createTopBar();
        uiLayer.getChildren().add(topBar);

        // 标题 + 按钮区域
        VBox centerArea = createCenterArea();
        uiLayer.getChildren().add(centerArea);

        getChildren().addAll(backgroundLayer, uiLayer);
    }

    /**
     * 【背景接口】设置背景图片。
     * 用户传入图片路径（相对于 classpath 或绝对路径）即可替换背景。
     *
     * @param imagePath 图片路径，例如 "/images/menu_bg.png" 或 "file:images/menu_bg.png"
     */
    public void setBackgroundImage(String imagePath) {
        try {
            Image img;
            if (imagePath.startsWith("file:") || imagePath.startsWith("/")) {
                img = new Image(imagePath);
            } else {
                img = new Image(getClass().getResourceAsStream("/" + imagePath));
            }
            if (img != null && !img.isError()) {
                backgroundImageView.setImage(img);
                backgroundImageView.setVisible(true);
                backgroundImageView.setManaged(true);
                backgroundLayer.setStyle("-fx-background-color: transparent;");
            }
        } catch (Exception e) {
            System.err.println("[MainMenu] 背景图片加载失败: " + imagePath + " — " + e.getMessage());
        }
    }

    /**
     * 【背景接口】清除背景图片，恢复默认颜色。
     */
    public void clearBackgroundImage() {
        backgroundImageView.setImage(null);
        backgroundImageView.setVisible(false);
        backgroundImageView.setManaged(false);
        backgroundLayer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0a0a1a, #1a1a3e, #0d1b2a);"
        );
    }

    // ===== 顶部栏 =====
    private HBox createTopBar() {
        HBox bar = new HBox();
        bar.setPrefWidth(WIDTH);
        bar.setPadding(new Insets(15, 20, 0, 20));
        bar.setAlignment(Pos.TOP_LEFT);
        bar.setSpacing(0);

        // 左侧：血量 + 经验 + 等级
        HBox leftBox = new HBox();
        leftBox.setSpacing(8);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        hpLabel = createStatBox("♥ ---", "#ff4444", "rgba(255,68,68,0.25)");
        xpLabel = createStatBox("★ ---/100", "#4fc3f7", "rgba(79,195,247,0.25)");
        levelLabel = createStatBox("⭐ LV.---", "#FFD54F", "rgba(255,213,79,0.25)");

        leftBox.getChildren().addAll(hpLabel, xpLabel, levelLabel);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        bar.getChildren().add(leftBox);

        // 右侧：金币显示
        HBox currencyBox = new HBox();
        currencyBox.setSpacing(12);
        currencyBox.setAlignment(Pos.CENTER_RIGHT);

        goldLabel = createStatBox("💰 ---", "#FFD54F", "rgba(255,213,79,0.25)");

        currencyBox.getChildren().add(goldLabel);
        bar.getChildren().add(currencyBox);

        return bar;
    }

    private Label createStatBox(String text, String textColor, String bgColor) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(textColor));
        label.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 5 14 5 14;" +
            "-fx-border-color: " + textColor + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 0, 1);"
        );
        return label;
    }

    // ===== 标题 + 按钮区域 =====
    private VBox createCenterArea() {
        VBox area = new VBox();
        area.setAlignment(Pos.CENTER);
        area.setSpacing(0);
        area.setPadding(new Insets(20, 0, 0, 0));

        // 标题
        Label titleLabel = new Label("513保卫战");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 52));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(4, 0, 2, Color.color(0, 0, 0, 0.35)));
        titleLabel.setPadding(new Insets(0, 0, 30, 0));

        // 标题呼吸动画
        setupTitleAnimation(titleLabel);

        // 按钮列表
        VBox btnBox = new VBox();
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(12);

        // 【新游戏】：直接用当前保存的难度开始游戏
        startBtn = createMenuButton("开始游戏", "#4299e1", "#3182ce", e -> {
            GameData.resetToDefault();  // 核心修复：新游戏必须重置数据
            GameSave.deleteSave();
            gamePanel.startGame(1);
            setVisible(false);
            setManaged(false);
        });

        // 【继续游戏】：有存档时才显示，不重置数据直接继续
        continueBtn = createMenuButton("继续游戏", "#48bb78", "#38a169", e -> {
            // 不重置数据，直接恢复战场
            gamePanel.continueGame();
            setVisible(false);
            setManaged(false);
        });

        // 【无尽模式】：重置数据后启动无尽模式
        endlessBtn = createMenuButton("无尽模式", "#ed8936", "#dd6b20", e -> {
            GameData.resetToDefault();  // 无尽模式也要重置数据
            GameData.isEndless = true;  // 标记为无尽模式
            GameSave.deleteSave();
            gamePanel.startEndlessGame();
            setVisible(false);
            setManaged(false);
        });

        difficultyBtn = createMenuButton("难度选择", "#48bb78", "#38a169", e -> {
            gamePanel.showDifficultySelect();
            setVisible(false);
            setManaged(false);
        });

        shopBtn = createMenuButton("游戏商城", "#ecc94b", "#d69e2e", e -> {
            // 商城始终可访问，无论游戏是否开始
            setVisible(false);
            setManaged(false);
            if (gamePanel.getGameState() == enums.GameState.PLAYING || gamePanel.getGameState() == enums.GameState.PAUSED) {
                // 游戏中打开，从暂停进入
                gamePanel.showShopFromPause();
            } else {
                // 主菜单打开，使用默认值
                gamePanel.showShopFromMenu();
            }
        });

        leaderboardBtn = createMenuButton("排行榜", "#9f7aea", "#805ad5", e -> {
            gamePanel.showLeaderboard();
            setVisible(false);
            setManaged(false);
        });

        settingsBtn = createMenuButton("设置", "#a0aec0", "#718096", e -> {
            gamePanel.showSettingsFromMenu();
            setVisible(false);
            setManaged(false);
        });

        exitBtn = createMenuButton("退出游戏", "#e53e3e", "#c53030", e -> {
            System.exit(0);
        });

        btnBox.getChildren().addAll(startBtn, continueBtn, endlessBtn, difficultyBtn, shopBtn, leaderboardBtn, settingsBtn, exitBtn);

        area.getChildren().addAll(titleLabel, btnBox);
        return area;
    }

    /** 标题呼吸发光特效 — 霓虹光效 */
    private void setupTitleAnimation(Label titleLabel) {
        DropShadow shadow1 = new DropShadow();
        shadow1.setColor(Color.web("#64b5f6"));
        shadow1.setRadius(20);
        shadow1.setOffsetX(0);
        shadow1.setOffsetY(0);

        DropShadow shadow2 = new DropShadow();
        shadow2.setColor(Color.web("#42a5f5"));
        shadow2.setRadius(10);
        shadow2.setOffsetX(0);
        shadow2.setOffsetY(0);

        Timeline anim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(shadow1.radiusProperty(), 15),
                new KeyValue(shadow1.colorProperty(), Color.web("#64b5f6", 0.4)),
                new KeyValue(shadow2.radiusProperty(), 8),
                new KeyValue(shadow2.colorProperty(), Color.web("#42a5f5", 0.5))),
            new KeyFrame(Duration.seconds(2.5),
                new KeyValue(shadow1.radiusProperty(), 28),
                new KeyValue(shadow1.colorProperty(), Color.web("#64b5f6", 0.7)),
                new KeyValue(shadow2.radiusProperty(), 14),
                new KeyValue(shadow2.colorProperty(), Color.web("#42a5f5", 0.8)))
        );
        anim.setCycleCount(Timeline.INDEFINITE);
        anim.setAutoReverse(true);

        // 组合两个阴影
        javafx.scene.effect.InnerShadow inner = new javafx.scene.effect.InnerShadow();
        inner.setColor(Color.web("#ffffff", 0.1));
        inner.setRadius(3);
        inner.setOffsetX(0);
        inner.setOffsetY(-1);

        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.3);

        // 使用 Group 来组合多个效果
        titleLabel.setEffect(shadow1);
        anim.play();
    }

    /** 创建菜单按钮 — 大圆角、彩色渐变、悬停放大 */
    private Button createMenuButton(String text, String colorNormal, String colorHover,
                                    javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setPrefSize(280, 52);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);
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

    /** 启用商城按钮（游戏开始后调用） */
    public void enableShopButton() {
        if (shopBtn != null) {
            shopBtn.setOpacity(1.0);
        }
    }

    /** 禁用商城按钮（返回主菜单时调用） */
    public void disableShopButton() {
        if (shopBtn != null) {
            shopBtn.setOpacity(0.5);
        }
    }

    /** 同步顶部数据标签：从全局 GameData 读取最新数据 */
    public void updateTopLabels() {
        hpLabel.setText("♥ " + GameData.hp);
        xpLabel.setText("★ " + GameData.exp + "/100");
        levelLabel.setText("⭐ LV." + GameData.level);
        goldLabel.setText("💰 " + GameData.gold);
        System.out.println("[主菜单UI刷新] HP=" + GameData.hp + " 经验=" + GameData.exp
            + " 等级=" + GameData.level + " 金币=" + GameData.gold);
    }

    /** 强制刷新主菜单 UI（返回主菜单时调用） */
    public void refreshUI() {
        updateTopLabels();
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

    /** 显示菜单并播放入场动画 */
    public void show() {
        // 1. 强行赋予固定尺寸与激活渲染
        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setVisible(true);
        setManaged(true);
        setOpacity(1.0);
        toFront(); // 强制提升到 StackPane 最顶层

        // 2. 刷新子组件的显示状态
        backgroundLayer.setVisible(true);
        backgroundLayer.setManaged(true);
        uiLayer.setVisible(true);
        uiLayer.setManaged(true);

        // 3. 确保所有按钮可见
        Button[] buttons = {startBtn, continueBtn, endlessBtn, difficultyBtn, shopBtn, leaderboardBtn, settingsBtn, exitBtn};
        for (Button btn : buttons) {
            btn.setVisible(true);
            btn.setManaged(true);
            btn.setOpacity(0);
            btn.setTranslateY(25);
        }

        // 继续游戏按钮：有存档时才显示
        boolean hasSave = GameSave.hasSave();
        continueBtn.setVisible(hasSave);
        continueBtn.setManaged(hasSave);

        // 按钮依次淡入 + 上移
        SequentialTransition anim = new SequentialTransition();

        for (int i = 0; i < buttons.length; i++) {
            Button btn = buttons[i];

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

        // 核心修复：主菜单显示时强制刷新 UI，同步 GameData 初始值
        refreshUI();
    }
}
