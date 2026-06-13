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
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
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

    // 记录打开来源：0=从主菜单打开，1=从暂停打开，2=从游戏中打开
    private int openSource;

    // 所有武器数据（来自枚举）
    private final WeaponData[] weapons = WeaponData.values();

    private VBox detailPanel;
    private Label detailTitle;
    private Label detailContent;
    private Pane imagePane; // 图片展示区
    private TextFlow detailUpgrade;
    private Label goldLabel;
    private Label levelLabel;
    private Button[] buyButtons;
    private VBox[] cardContainers;
    private StackPane[] cardStacks; // 每个卡片加上锁蒙层

    // 提示标签（金币不足/等级不足）
    private Label toastLabel;
    private FadeTransition toastFadeOut;

    public ShopOverlay(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.openSource = 0;

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

        // 半透明背景 — 深空渐变
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, rgba(8, 8, 24, 0.96), rgba(15, 15, 35, 0.96));"
        );
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
        scrollPane.setPrefWidth(610);
        scrollPane.setPrefHeight(510);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        // 右侧：详情面板
        detailPanel = createDetailPanel();
        detailPanel.setPrefWidth(270);
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
            if (openSource == 1) {
                // 从暂停打开，返回暂停
                gamePanel.pauseGame();
                setVisible(false);
                setManaged(false);
                gamePanel.getPauseOverlay().show();
            } else if (openSource == 2) {
                // 从游戏中打开，返回游戏
                gamePanel.resumeGame();
                setVisible(false);
                setManaged(false);
            } else {
                // 从主菜单打开，返回主菜单
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
        levelLabel = new Label("LV.1");
        levelLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        levelLabel.setTextFill(Color.web("#ffd700"));

        // 金币显示
        goldLabel = new Label("$ 150");
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
        cardStacks = new StackPane[weapons.length];

        for (int i = 0; i < weapons.length; i++) {
            WeaponData w = weapons[i];
            StackPane cardStack = createShopCard(w, i);
            cardStacks[i] = cardStack;

            int col = i % 3;
            int row = i / 3;
            grid.add(cardStack, col, row);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private StackPane createShopCard(WeaponData w, int index) {
        // 主卡片容器
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(16));
        card.setPrefWidth(182);
        card.setPrefHeight(220);

        // 初始卡片样式
        updateCardStyle(card, w, false);

        // 卡片名称
        Label nameLabel = new Label(w.getName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web(w.getColor()));

        // 简短描述（攻击类型）
        Label descLabel = new Label(w.getTargetType() + "攻击");
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.color(0.6, 0.65, 0.7));

        // 属性信息，使用英文缩写 ATK/SPD/RNG
        VBox infoBox = new VBox();
        infoBox.setSpacing(6);
        infoBox.setAlignment(Pos.CENTER);

        Label damageLabel = new Label(String.format("ATK: %d", w.getDamage()));
        damageLabel.setFont(Font.font("Microsoft YaHei", 12));
        damageLabel.setTextFill(Color.color(0.65, 0.7, 0.8));

        Label speedLabel = new Label(String.format("SPD: %.1fs", w.getFireRate()));
        speedLabel.setFont(Font.font("Microsoft YaHei", 12));
        speedLabel.setTextFill(Color.color(0.65, 0.7, 0.8));

        Label rangeLabel = new Label(String.format("RNG: %.1f", w.getRange()));
        rangeLabel.setFont(Font.font("Microsoft YaHei", 12));
        rangeLabel.setTextFill(Color.color(0.65, 0.7, 0.8));

        infoBox.getChildren().addAll(damageLabel, speedLabel, rangeLabel);

        // 分隔线
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setPrefWidth(150);
        separator.setStyle("-fx-background-color: rgba(100,120,150,0.2);");

        // 解锁等级
        Label unlockLabel = new Label("解锁: LV " + w.getUnlockLevel());
        unlockLabel.setFont(Font.font("Microsoft YaHei", 11));
        unlockLabel.setTextFill(Color.color(0.55, 0.6, 0.7));

        // 价格
        Label priceLabel = new Label("$ " + w.getPrice());
        priceLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        priceLabel.setTextFill(Color.web("#ffd700"));

        // 购买按钮
        Button buyBtn = createBuyButton(w, index);
        buyButtons[index] = buyBtn;

        card.getChildren().addAll(nameLabel, descLabel, infoBox, separator, unlockLabel, priceLabel, buyBtn);
        cardContainers[index] = card;

        // 叠加上锁蒙层（StackPane）
        StackPane cardStack = new StackPane();
        cardStack.getChildren().add(card);

        // 锁头蒙层（初始隐藏）— 将锁图标放入 overlay 内部，隐藏时一起隐藏
        StackPane lockOverlay = new StackPane();
        lockOverlay.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.5);" +
            "-fx-background-radius: 10;"
        );
        lockOverlay.setVisible(false);
        lockOverlay.setPrefSize(182, 220);

        Label lockLabel = new Label("🔒");
        lockLabel.setFont(Font.font(40));
        lockLabel.setTextFill(Color.web("#ffffffaa"));
        lockOverlay.getChildren().add(lockLabel);

        cardStack.getChildren().add(lockOverlay);
        cardStack.setUserData(lockOverlay);

        // 悬停放大效果
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(180), cardStack);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), cardStack);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        cardStack.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.play();
            updateCardStyle(card, w, true);
            showDetail(w);
        });

        cardStack.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.play();
            updateCardStyle(card, w, false);
        });

        return cardStack;
    }

    private void updateCardStyle(VBox card, WeaponData w, boolean hovered) {
        if (hovered) {
            card.setStyle(
                "-fx-background-color: rgba(40, 48, 65, 0.95);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + w.getColor() + ";" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, " + w.getColor() + "55, 12, 0, 0, 0);"
            );
        } else {
            card.setStyle(
                "-fx-background-color: rgba(28, 33, 45, 0.88);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: rgba(80, 100, 130, 0.25);" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"
            );
        }
    }

    private Button createBuyButton(WeaponData w, int index) {
        Button btn = new Button();
        btn.setPrefWidth(120);
        btn.setPrefHeight(34);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        btn.setStyle("-fx-background-radius: 6;");
        return btn;
    }

    public void updateAllButtons() {
        int playerLevel = GameData.level;
        int gold = GameData.gold;

        for (int i = 0; i < weapons.length; i++) {
            WeaponData w = weapons[i];
            Button btn = buyButtons[i];
            StackPane cardStack = cardStacks[i];
            StackPane lockOverlay = (StackPane) cardStack.getUserData();
            VBox card = cardContainers[i];
            boolean unlocked = playerLevel >= w.getUnlockLevel();
            boolean canAfford = gold >= w.getPrice();

            if (unlocked && canAfford) {
                card.setOpacity(1.0);
                lockOverlay.setVisible(false);
                btn.setText("购买");
                btn.setTextFill(Color.WHITE);
                btn.setStyle(
                    "-fx-pref-width: 120px;" +
                    "-fx-pref-height: 34px;" +
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
                card.setOpacity(0.5);
                lockOverlay.setVisible(true);
                btn.setText("LV." + w.getUnlockLevel() + " 解锁");
                btn.setTextFill(Color.color(0.45, 0.48, 0.52));
                btn.setStyle(
                    "-fx-pref-width: 120px;" +
                    "-fx-pref-height: 34px;" +
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
                card.setOpacity(1.0);
                lockOverlay.setVisible(false);
                btn.setText("$ 金币不足");
                btn.setTextFill(Color.color(0.65, 0.42, 0.32));
                btn.setStyle(
                    "-fx-pref-width: 120px;" +
                    "-fx-pref-height: 34px;" +
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

        goldLabel.setText("$ " + gold);
        levelLabel.setText("LV." + playerLevel);
    }

    private VBox createDetailPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setSpacing(10);
        panel.setPadding(new Insets(14));
        panel.setPrefWidth(270);
        panel.setPrefHeight(510);

        panel.setStyle(
            "-fx-background-color: rgba(22, 28, 40, 0.92);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(80, 100, 140, 0.2);" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        );

        Label panelTitle = new Label("商品详情");
        panelTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        panelTitle.setTextFill(Color.WHITE);
        panelTitle.setPadding(new Insets(0, 0, 6, 0));

        // 分隔线
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setPrefWidth(240);
        sep.setStyle("-fx-background-color: rgba(100,120,150,0.2);");

        detailTitle = new Label("选择商品查看详情");
        detailTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        detailTitle.setTextFill(Color.color(0.6, 0.7, 0.9));
        detailTitle.setWrapText(true);

        // 图片展示区域（空的，等待动态加载）
        imagePane = new Pane();
        imagePane.setPrefSize(240, 160);
        imagePane.setMinSize(240, 160);
        imagePane.setMaxSize(240, 160);
        imagePane.setStyle(
            "-fx-background-color: rgba(30, 36, 52, 0.6);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(80, 100, 130, 0.15);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );

        detailContent = new Label("将鼠标悬停在左侧商品卡片上，此处会显示该武器的详细介绍。");
        detailContent.setFont(Font.font("Microsoft YaHei", 12));
        detailContent.setTextFill(Color.color(0.5, 0.55, 0.65));
        detailContent.setWrapText(true);
        detailContent.setPrefWidth(240);

        Label upgradeTitle = new Label("升级路线");
        upgradeTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        upgradeTitle.setTextFill(Color.color(0.65, 0.75, 0.9));
        upgradeTitle.setPadding(new Insets(4, 0, 4, 0));

        detailUpgrade = new TextFlow();
        detailUpgrade.setPrefWidth(240);

        panel.getChildren().addAll(panelTitle, sep, imagePane, detailTitle, detailContent, upgradeTitle, detailUpgrade);
        return panel;
    }

    private void showDetail(WeaponData w) {
        detailTitle.setText(w.getName());
        detailTitle.setTextFill(Color.web(w.getColor()));
        detailContent.setText(w.getDetailDesc());

        // 清空图片展示区并重新添加
        imagePane.getChildren().clear();

        // 如果有塔的图片，可以在这里加载
        // 暂时在中间显示名称，保持美观
        Label placeHolder = new Label(w.getName());
        placeHolder.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        placeHolder.setTextFill(Color.web(w.getColor()));
        StackPane.setAlignment(placeHolder, Pos.CENTER);

        StackPane holderStack = new StackPane(placeHolder);
        holderStack.setPrefSize(240, 160);
        imagePane.getChildren().add(holderStack);

        // 解析升级路线，颜色高亮加成部分
        detailUpgrade.getChildren().clear();
        String upgradeText = w.getUpgradePath();
        String[] lines = upgradeText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            parseUpgradeLine(line);
            if (i < lines.length - 1) {
                detailUpgrade.getChildren().add(new Text("\n"));
            }
        }
    }

    /** 解析升级路线，给 +XX% 加上绿色高亮 */
    private void parseUpgradeLine(String line) {
        // line 格式: "Lv.1 > Lv.2: 伤害+30% 射速+10%"
        String[] parts = line.split(":");
        if (parts.length < 2) {
            detailUpgrade.getChildren().add(new Text(line));
            return;
        }

        // 左边部分（升级路径）
        Text leftText = new Text(parts[0] + ": ");
        leftText.setFont(Font.font("Microsoft YaHei", 12));
        leftText.setFill(Color.color(0.55, 0.6, 0.7));
        detailUpgrade.getChildren().add(leftText);

        // 右边部分（属性加成）
        String rightPart = parts[1].trim();
        String[] attrs = rightPart.split("\\s+");
        for (int i = 0; i < attrs.length; i++) {
            String attr = attrs[i];
            if (attr.contains("+")) {
                // 加成部分使用亮绿色
                Text attrText = new Text(attr + " ");
                attrText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
                attrText.setFill(Color.web("#69f0ae"));
                detailUpgrade.getChildren().add(attrText);
            } else {
                Text attrText = new Text(attr + " ");
                attrText.setFont(Font.font("Microsoft YaHei", 12));
                attrText.setFill(Color.color(0.55, 0.6, 0.7));
                detailUpgrade.getChildren().add(attrText);
            }
        }
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
        this.openSource = fromPause ? 1 : 0;
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

    /** 从游戏中打开商城（不暂停） */
    public void showFromGame() {
        this.openSource = 2;
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

    /** 获取商城打开来源 */
    public int getOpenSource() {
        return openSource;
    }
}
