package main;

import enums.WeaponData;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * 武器商城面板 —— 现代化卡片式商店界面。
 * 使用 JavaFX 组件实现，覆盖在游戏画布上方。
 * 数据源：WeaponData 枚举。
 */
public class ShopOverlay extends StackPane {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 680;

    private final GamePanel gamePanel;

    // 记录打开来源：true=从暂停打开，false=从主菜单打开
    private boolean openedFromPause;

    // 所有武器数据（来自枚举）
    private final WeaponData[] weapons = WeaponData.values();

    private VBox detailPanel;
    private Label detailTitle;
    private Label detailContent;
    private Label detailUpgrade;
    private Label goldLabel;
    private Label levelLabel;
    private Button[] buyButtons;
    private VBox[] cardContainers;

    // 提示标签（金币不足/等级不足）
    private Label toastLabel;
    private FadeTransition toastFadeOut;

    public ShopOverlay(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.openedFromPause = false;

        setPrefSize(WIDTH, HEIGHT);
        setAlignment(Pos.TOP_LEFT);
        setVisible(false);
        setManaged(false);

        // 加载 CSS
        try {
            getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());
        } catch (Exception e) {
            // CSS 加载失败不影响运行
        }

        // 半透明背景
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: rgba(8, 8, 24, 0.95);");
        backgroundLayer.setPrefSize(WIDTH, HEIGHT);

        // 主内容区
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(16, 24, 16, 24));
        mainContent.setSpacing(14);
        mainContent.setPrefSize(WIDTH - 48, HEIGHT - 32);

        // 顶部栏：返回按钮 + 标题 + 金币/等级
        HBox topBar = createTopBar();
        mainContent.getChildren().add(topBar);

        // 商品列表 + 详情面板
        HBox contentArea = new HBox();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setSpacing(18);
        contentArea.setPrefWidth(WIDTH - 60);

        // 左侧：商品网格
        ScrollPane scrollPane = createShopGrid();
        scrollPane.setPrefWidth(600);
        scrollPane.setPrefHeight(510);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        // 右侧：详情面板
        detailPanel = createDetailPanel();
        detailPanel.setPrefWidth(280);
        detailPanel.setPrefHeight(510);

        contentArea.getChildren().addAll(scrollPane, detailPanel);
        mainContent.getChildren().add(contentArea);

        // Toast 提示标签（居中底部）
        toastLabel = new Label();
        toastLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        toastLabel.setTextFill(Color.WHITE);
        toastLabel.setVisible(false);
        toastLabel.setManaged(false);
        toastLabel.setStyle(
            "-fx-background-color: rgba(220, 40, 40, 0.9);" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 24 8 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);"
        );
        toastFadeOut = new FadeTransition(Duration.millis(1500), toastLabel);
        toastFadeOut.setFromValue(1);
        toastFadeOut.setToValue(0);
        toastFadeOut.setOnFinished(e -> {
            toastLabel.setVisible(false);
            toastLabel.setManaged(false);
        });

        StackPane.setAlignment(toastLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toastLabel, new Insets(0, 0, 40, 0));

        getChildren().addAll(backgroundLayer, mainContent, toastLabel);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(16);
        topBar.setPrefWidth(WIDTH - 48);

        // 返回按钮
        Button backBtn = createBackButton("← 返回", e -> {
            if (openedFromPause) {
                gamePanel.pauseGame();
                setVisible(false);
                setManaged(false);
                gamePanel.getPauseOverlay().show();
            } else {
                setVisible(false);
                setManaged(false);
                gamePanel.returnToMenuFromOverlay();
            }
        });

        // 标题
        Label titleLabel = new Label(" 武器商城");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(100,150,255,0.4), 8, 0, 0, 0);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 等级显示
        levelLabel = new Label("⭐ LV.1");
        levelLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        levelLabel.setTextFill(Color.web("#ffd700"));

        // 金币显示
        goldLabel = new Label("💰 500");
        goldLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        goldLabel.setTextFill(Color.web("#ffd700"));

        topBar.getChildren().addAll(backBtn, titleLabel, spacer, levelLabel, goldLabel);
        return topBar;
    }

    private ScrollPane createShopGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setPadding(new Insets(8));

        buyButtons = new Button[weapons.length];
        cardContainers = new VBox[weapons.length];

        for (int i = 0; i < weapons.length; i++) {
            WeaponData w = weapons[i];
            VBox card = createShopCard(w, i);
            cardContainers[i] = card;

            int col = i % 3;
            int row = i / 3;
            grid.add(card, col, row);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox createShopCard(WeaponData w, int index) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(7);
        card.setPadding(new Insets(12));
        card.setPrefWidth(186);
        card.setPrefHeight(240);

        // 卡片样式
        updateCardStyle(card, w, false);

        // 悬停放大效果
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(180), card);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        card.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.play();
            updateCardStyle(card, w, true);
            showDetail(w);
        });

        card.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.play();
            updateCardStyle(card, w, false);
        });

        // 图标 + 名称行
        HBox nameRow = new HBox();
        nameRow.setAlignment(Pos.CENTER);
        nameRow.setSpacing(4);

        Label iconLabel = new Label(w.getIcon().isEmpty() ? "" : w.getIcon());
        iconLabel.setFont(Font.font("Microsoft YaHei", 16));

        Label nameLabel = new Label(w.getName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web(w.getColor()));

        Label keyLabel = new Label("[" + (index + 1) + "]");
        keyLabel.setFont(Font.font("Microsoft YaHei", 10));
        keyLabel.setTextFill(Color.color(0.5, 0.55, 0.65));

        nameRow.getChildren().addAll(iconLabel, nameLabel, keyLabel);

        // 简短描述
        Label descLabel = new Label(w.getTargetType() + "攻击");
        descLabel.setFont(Font.font("Microsoft YaHei", 10));
        descLabel.setTextFill(Color.color(0.6, 0.65, 0.75));
        descLabel.setWrapText(true);
        descLabel.setPrefWidth(160);

        // 属性信息
        VBox infoBox = new VBox();
        infoBox.setSpacing(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label statsLabel = new Label(String.format("⚔ %d   %.1fs", w.getDamage(), w.getFireRate()));
        statsLabel.setFont(Font.font("Microsoft YaHei", 10));
        statsLabel.setTextFill(Color.color(0.65, 0.7, 0.8));

        Label rangeLabel = new Label(String.format("◎ %.0f  🎯 %s", w.getRange(), w.getTargetType()));
        rangeLabel.setFont(Font.font("Microsoft YaHei", 10));
        rangeLabel.setTextFill(Color.color(0.65, 0.7, 0.8));

        infoBox.getChildren().addAll(statsLabel, rangeLabel);

        // 分隔线
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setPrefWidth(160);
        separator.setStyle("-fx-background-color: rgba(100,120,150,0.2);");

        // 解锁等级
        Label unlockLabel = new Label("解锁: LV " + w.getUnlockLevel());
        unlockLabel.setFont(Font.font("Microsoft YaHei", 10));
        unlockLabel.setTextFill(Color.color(0.55, 0.6, 0.7));

        // 价格
        Label priceLabel = new Label("💰 " + w.getPrice());
        priceLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        priceLabel.setTextFill(Color.web("#ffd700"));

        // 购买按钮
        Button buyBtn = createBuyButton(w, index);
        buyButtons[index] = buyBtn;

        card.getChildren().addAll(nameRow, descLabel, infoBox, separator, unlockLabel, priceLabel, buyBtn);
        return card;
    }

    private void updateCardStyle(VBox card, WeaponData w, boolean hovered) {
        if (hovered) {
            card.setStyle(
                "-fx-background-color: rgba(40, 48, 65, 0.95);" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + w.getColor() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, " + w.getColor() + "55, 12, 0, 0, 0);"
            );
        } else {
            card.setStyle(
                "-fx-background-color: rgba(28, 33, 45, 0.88);" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: rgba(80, 100, 130, 0.25);" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"
            );
        }
    }

    private Button createBuyButton(WeaponData w, int index) {
        Button btn = new Button();
        btn.setPrefSize(158, 34);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        return btn;
    }

    private void updateAllButtons() {
        int playerLevel = gamePanel.getPlayerLevel();
        int gold = gamePanel.getPlayerGold();

        for (int i = 0; i < weapons.length; i++) {
            WeaponData w = weapons[i];
            Button btn = buyButtons[i];
            boolean unlocked = playerLevel >= w.getUnlockLevel();
            boolean canAfford = gold >= w.getPrice();

            if (unlocked && canAfford) {
                btn.setText("🛒 购买");
                btn.setTextFill(Color.WHITE);
                btn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #48bb78, #38a169);" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: rgba(255,255,255,0.2);" +
                    "-fx-border-radius: 6;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);"
                );
                btn.setDisable(false);

                final int idx = i;
                btn.setOnAction(e -> {
                    gamePanel.purchaseAndBuildTower(idx);
                    updateAllButtons();
                });
            } else if (!unlocked) {
                btn.setText(" LV." + w.getUnlockLevel() + " 解锁");
                btn.setTextFill(Color.color(0.45, 0.48, 0.52));
                btn.setStyle(
                    "-fx-background-color: rgba(45, 50, 60, 0.5);" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: rgba(70, 75, 85, 0.25);" +
                    "-fx-border-radius: 6;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: default;"
                );
                btn.setDisable(true);
                btn.setOnAction(null);
            } else {
                btn.setText("💰 金币不足");
                btn.setTextFill(Color.color(0.65, 0.42, 0.32));
                btn.setStyle(
                    "-fx-background-color: rgba(55, 38, 32, 0.5);" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: rgba(90, 60, 50, 0.25);" +
                    "-fx-border-radius: 6;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: default;"
                );
                btn.setDisable(true);
                btn.setOnAction(null);
            }
        }

        goldLabel.setText("💰 " + gold);
        levelLabel.setText("⭐ LV." + playerLevel);
    }

    private VBox createDetailPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setSpacing(10);
        panel.setPadding(new Insets(14));
        panel.setPrefWidth(280);
        panel.setPrefHeight(430);

        panel.setStyle(
            "-fx-background-color: rgba(22, 28, 40, 0.92);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(80, 100, 140, 0.2);" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        );

        Label panelTitle = new Label("📋 商品详情");
        panelTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        panelTitle.setTextFill(Color.WHITE);
        panelTitle.setPadding(new Insets(0, 0, 4, 0));

        // 分隔线
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setPrefWidth(250);
        sep.setStyle("-fx-background-color: rgba(100,120,150,0.2);");

        detailTitle = new Label("选择商品查看详情");
        detailTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        detailTitle.setTextFill(Color.color(0.6, 0.7, 0.9));
        detailTitle.setWrapText(true);

        detailContent = new Label("将鼠标悬停在左侧商品卡片上，此处会显示该武器的详细介绍。");
        detailContent.setFont(Font.font("Microsoft YaHei", 11));
        detailContent.setTextFill(Color.color(0.5, 0.55, 0.65));
        detailContent.setWrapText(true);
        detailContent.setPrefWidth(250);

        Label upgradeTitle = new Label("📈 升级路线");
        upgradeTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        upgradeTitle.setTextFill(Color.color(0.65, 0.75, 0.9));
        upgradeTitle.setPadding(new Insets(6, 0, 0, 0));

        detailUpgrade = new Label("");
        detailUpgrade.setFont(Font.font("Microsoft YaHei", 10));
        detailUpgrade.setTextFill(Color.color(0.55, 0.6, 0.7));
        detailUpgrade.setWrapText(true);
        detailUpgrade.setPrefWidth(250);

        panel.getChildren().addAll(panelTitle, sep, detailTitle, detailContent, upgradeTitle, detailUpgrade);
        return panel;
    }

    private void showDetail(WeaponData w) {
        detailTitle.setText(w.getIcon().isEmpty() ? w.getName() : w.getIcon() + " " + w.getName());
        detailTitle.setTextFill(Color.web(w.getColor()));
        detailContent.setText(w.getDetailDesc());
        detailUpgrade.setText(w.getUpgradePath());
    }

    private Button createBackButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Microsoft YaHei", 13));
        btn.setTextFill(Color.color(0.7, 0.8, 1));
        btn.setStyle(
            "-fx-background-color: rgba(45, 55, 75, 0.6);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(90, 120, 170, 0.3);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: rgba(65, 80, 105, 0.8);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(120, 160, 220, 0.6);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.5;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: rgba(45, 55, 75, 0.6);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(90, 120, 170, 0.3);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        ));

        btn.setOnAction(handler);
        return btn;
    }

    /** 显示 Toast 提示 */
    public void showToast(String message, boolean isError) {
        toastLabel.setText(message);
        if (isError) {
            toastLabel.setStyle(
                "-fx-background-color: rgba(220, 40, 40, 0.9);" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 24 8 24;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);"
            );
        } else {
            toastLabel.setStyle(
                "-fx-background-color: rgba(40, 167, 69, 0.9);" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 24 8 24;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);"
            );
        }
        toastLabel.setVisible(true);
        toastLabel.setManaged(true);
        toastLabel.setOpacity(1);

        toastFadeOut.stop();
        toastFadeOut.playFromStart();
    }

    public void show(boolean fromPause) {
        this.openedFromPause = fromPause;
        updateAllButtons();

        // 淡入动画
        setOpacity(0);
        setVisible(true);
        setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void show() {
        show(false);
    }
}
