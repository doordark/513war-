package main;

import enums.TowerType;
import entity.tower.Tower;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.Optional;

/**
 * HUD 面板 — 单行底栏三段式：左侧塔信息 | 中间塔购买 | 右侧控制按钮
 */
public class HudPanel extends VBox {

    private final GamePanel gamePanel;

    // 顶部数据栏标签
    private Label hpLabel;
    private Label goldLabel;
    private Label scoreLabel;
    private Label levelLabel;
    private StackPane expBarContainer;
    private Region expBarFill;
    private Label expBarText;
    private Label waveLabel;

    // 左侧：防御塔详情信息卡
    private VBox detailInfoCard;
    private Label detailNameLabel;
    private Label detailStatsLabel;
    private Button upgradeButton;
    private Button sellButton;

    // 塔购买按钮
    private HBox towerButtonBox;
    private Button[] towerButtons;
    private TowerType[] towerTypes = TowerType.values();
    private String[] towerNames = {"箭塔", "炮塔", "魔法塔", "减速塔", "闪电塔", "核弹塔"};
    private String[] towerIcons = {"", "", "✨", "❄️", "", "☢️"};
    private int[] towerCosts = {50, 100, 150, 75, 150, 500};

    // 取消放置按钮
    private Button cancelPlacementButton;

    // 右侧按钮
    private Button nextWaveButton;
    private Button shopButton;
    private Button endGameButton;  // 无尽模式结束游戏按钮

    // 动画
    private Timeline hpPulseAnimation;
    private Timeline goldPulseAnimation;

    public HudPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        // 加载 CSS
        String cssPath = getClass().getResource("/styles/game.css").toExternalForm();
        getStylesheets().add(cssPath);
        getStyleClass().add("hud-panel");
        setPrefHeight(110);
        setMinHeight(110);
        setMaxHeight(110);
        setPadding(new Insets(0));
        setSpacing(0);

        // ========== 顶部数据栏 ==========
        HBox topBar = createTopInfoBar();
        getChildren().add(topBar);

        // 分隔线
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.08);");
        getChildren().add(separator);

        // ========== 底部控制栏（三段式 HBox）==========
        HBox bottomBar = createBottomControlBar();
        bottomBar.setPrefHeight(70);
        bottomBar.setMaxHeight(70);
        getChildren().add(bottomBar);

        // 初始化动画
        initAnimations();
    }

    /**
     * 顶部数据栏：HP / Gold / Score 左对齐，Wave 右对齐
     */
    private HBox createTopInfoBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(8, 20, 8, 20));
        bar.setSpacing(0);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: rgba(20, 24, 35, 0.85);");

        HBox leftData = new HBox();
        leftData.setSpacing(24);
        leftData.setAlignment(Pos.CENTER_LEFT);

        hpLabel = createTopLabel("HP: 20", "#ff6b6b");
        goldLabel = createTopLabel("Gold: 150", "#ffd54f");
        scoreLabel = createTopLabel("Score: 0", "#ffffff");
        levelLabel = createTopLabel("Lv.1", "#a78bfa");

        // 经验进度条容器
        expBarContainer = new StackPane();
        expBarContainer.setPrefSize(80, 14);
        expBarContainer.setMaxSize(80, 14);
        expBarContainer.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-background-radius: 7;" +
            "-fx-border-color: rgba(167,139,250,0.3);" +
            "-fx-border-radius: 7;" +
            "-fx-border-width: 1;"
        );

        expBarFill = new Region();
        expBarFill.setPrefHeight(14);
        expBarFill.setStyle(
            "-fx-background-color: linear-gradient(to right, #a78bfa, #7c3aed);" +
            "-fx-background-radius: 7;"
        );
        StackPane.setAlignment(expBarFill, Pos.CENTER_LEFT);

        expBarText = new Label("0/100");
        expBarText.setStyle("-fx-font-size: 9px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        expBarContainer.getChildren().addAll(expBarFill, expBarText);

        leftData.getChildren().addAll(hpLabel, goldLabel, scoreLabel, levelLabel, expBarContainer);
        HBox.setHgrow(leftData, Priority.NEVER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        waveLabel = createTopLabel("W 0/0", "#64b5f6");

        // 无尽模式结束游戏按钮（默认隐藏）
        endGameButton = createEndGameButton();

        bar.getChildren().addAll(leftData, spacer, waveLabel, endGameButton);
        return bar;
    }

    private Label createTopLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-family: 'Microsoft YaHei', 'PingFang SC'; " +
            "-fx-font-size: 14px; -fx-font-weight: bold;", color));
        return label;
    }

    /**
     * 底部控制栏：三段式 HBox
     * 左侧：防御塔详情信息卡（固定宽度 180）
     * 中间：防御塔购买卡片（自适应撑满）
     * 右侧：下一波 + 商城按钮（固定宽度 220）
     */
    private HBox createBottomControlBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(4, 10, 4, 10));
        bar.setSpacing(10);
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color: rgba(15, 18, 28, 0.9);");

        // 【左侧】防御塔详情信息卡
        detailInfoCard = createDetailInfoCard();
        HBox.setHgrow(detailInfoCard, Priority.NEVER);
        bar.getChildren().add(detailInfoCard);

        // 【中间】防御塔购买卡片
        towerButtonBox = createTowerButtons();
        HBox.setHgrow(towerButtonBox, Priority.ALWAYS);
        bar.getChildren().add(towerButtonBox);

        // 【右侧】控制按钮
        HBox rightBox = createRightButtons();
        HBox.setHgrow(rightBox, Priority.NEVER);
        bar.getChildren().add(rightBox);

        return bar;
    }

    /**
     * 左侧详情信息卡：显示选中塔的属性，未选中时显示提示。
     */
    private VBox createDetailInfoCard() {
        VBox card = new VBox();
        card.setMinWidth(180);
        card.setPrefWidth(180);
        card.setMaxWidth(180);
        card.setPrefHeight(60);
        card.setMaxHeight(60);
        card.setPadding(new Insets(6, 10, 6, 10));
        card.setSpacing(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: rgba(28, 33, 45, 0.88);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(80, 100, 130, 0.25);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );

        detailNameLabel = new Label("未选中防御塔");
        detailNameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #a0aec0;");

        detailStatsLabel = new Label("");
        detailStatsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

        HBox btnRow = new HBox(6);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        upgradeButton = new Button("升级");
        upgradeButton.setFocusTraversable(false);
        upgradeButton.setPrefSize(65, 22);
        upgradeButton.setStyle(
            "-fx-font-size: 11px; -fx-background-color: linear-gradient(to bottom, #48bb78, #38a169);" +
            "-fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
        upgradeButton.setVisible(false);
        upgradeButton.setManaged(false);
        upgradeButton.setOnAction(e -> {
            Tower t = gamePanel.getSelectedTower();
            if (t != null) gamePanel.upgradeTower(t);
        });

        sellButton = new Button("出售");
        sellButton.setFocusTraversable(false);
        sellButton.setPrefSize(65, 22);
        sellButton.setStyle(
            "-fx-font-size: 11px; -fx-background-color: linear-gradient(to bottom, #e53e3e, #c53030);" +
            "-fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
        sellButton.setVisible(false);
        sellButton.setManaged(false);
        sellButton.setOnAction(e -> {
            Tower t = gamePanel.getSelectedTower();
            if (t != null) gamePanel.sellTower(t);
        });

        btnRow.getChildren().addAll(upgradeButton, sellButton);

        card.getChildren().addAll(detailNameLabel, detailStatsLabel, btnRow);
        return card;
    }

    /**
     * 右侧按钮容器：锁死宽度，防止文字截断。
     */
    private HBox createRightButtons() {
        HBox box = new HBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setMinWidth(220);
        box.setPrefWidth(220);
        box.setMaxWidth(220);

        nextWaveButton = createNextWaveButton();
        shopButton = createShopButton();

        box.getChildren().addAll(nextWaveButton, shopButton);
        return box;
    }

    private void initAnimations() {
        hpPulseAnimation = new Timeline();
        hpPulseAnimation.setCycleCount(Timeline.INDEFINITE);
        hpPulseAnimation.setAutoReverse(true);
        KeyValue kv = new KeyValue(hpLabel.opacityProperty(), 0.4);
        KeyFrame kf = new KeyFrame(Duration.millis(600), kv);
        hpPulseAnimation.getKeyFrames().add(kf);

        goldPulseAnimation = new Timeline();
        goldPulseAnimation.setCycleCount(2);
        goldPulseAnimation.setAutoReverse(true);
        KeyValue gkv = new KeyValue(goldLabel.scaleXProperty(), 1.15);
        KeyValue gkv2 = new KeyValue(goldLabel.scaleYProperty(), 1.15);
        KeyFrame gkf = new KeyFrame(Duration.millis(200), gkv, gkv2);
        goldPulseAnimation.getKeyFrames().add(gkf);
    }

    /**
     * 创建中间塔购买按钮区域。
     */
    private HBox createTowerButtons() {
        towerButtonBox = new HBox();
        towerButtonBox.setSpacing(10);
        towerButtonBox.setAlignment(Pos.CENTER);
        towerButtonBox.setPadding(new Insets(0, 10, 0, 10));
        updateTowerButtons();
        return towerButtonBox;
    }

    /**
     * 根据已购买的塔类型动态更新按钮列表。
     */
    public void updateTowerButtons() {
        towerButtonBox.getChildren().clear();
        java.util.List<Integer> purchasedTowers = gamePanel.getPurchasedTowerIndices();
        if (!purchasedTowers.contains(0)) {
            purchasedTowers.add(0);
        }
        towerButtons = new Button[purchasedTowers.size()];
        for (int i = 0; i < purchasedTowers.size(); i++) {
            int towerIdx = purchasedTowers.get(i);
            towerButtons[i] = createTowerButton(towerIdx);
            towerButtonBox.getChildren().add(towerButtons[i]);
        }
    }

    private Button createTowerButton(int index) {
        Button btn = new Button();
        btn.setFocusTraversable(false);
        btn.setPrefSize(76, 56);
        btn.setMinSize(76, 56);
        btn.setMaxSize(76, 56);
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add("tower-button");

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(0);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER);
        topRow.setSpacing(3);
        Label iconLabel = new Label(towerIcons[index]);
        iconLabel.setStyle("-fx-font-size: 14px;");
        Label keyLabel = new Label("[" + (index + 1) + "]");
        keyLabel.getStyleClass().add("tower-key");
        topRow.getChildren().addAll(iconLabel, keyLabel);

        Label nameLabel = new Label(towerNames[index]);
        nameLabel.getStyleClass().add("tower-name");

        Label costLabel = new Label("LV." + gamePanel.getTowerUnlockLevel(index));
        costLabel.getStyleClass().add("tower-cost");

        content.getChildren().addAll(topRow, nameLabel, costLabel);
        btn.setGraphic(content);

        final int idx = index;
        btn.setOnAction(e -> gamePanel.selectTowerType(towerTypes[idx]));

        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
                st.setToX(1.08);
                st.setToY(1.08);
                st.play();
            }
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return btn;
    }

    /**
     * 更新 HUD 状态。
     */
    public void updateState() {
        int lives = gamePanel.getPlayerLives();
        hpLabel.setText("HP: " + lives);

        int oldGold = Integer.parseInt(goldLabel.getText().replaceAll("[^0-9]", ""));
        int newGold = gamePanel.getPlayerGold();
        goldLabel.setText("Gold: " + newGold);

        if (newGold != oldGold && oldGold != 0) {
            goldPulseAnimation.playFromStart();
        }

        boolean lowLives = lives <= 5;
        if (lowLives) {
            if (!hpPulseAnimation.getStatus().equals(Animation.Status.RUNNING)) {
                hpPulseAnimation.play();
            }
            hpLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Microsoft YaHei', 'PingFang SC'; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            hpPulseAnimation.stop();
            hpLabel.setOpacity(1.0);
            hpLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-family: 'Microsoft YaHei', 'PingFang SC'; -fx-font-size: 14px; -fx-font-weight: bold;");
        }

        if (gamePanel.isEndlessMode()) {
            waveLabel.setText("无尽模式 第 " + gamePanel.getCurrentWave() + " 波");
            waveLabel.setStyle("-fx-text-fill: #ff6b3d; -fx-font-family: 'Microsoft YaHei', 'PingFang SC'; -fx-font-size: 14px; -fx-font-weight: bold;");
            // 无尽模式显示结束游戏按钮
            endGameButton.setVisible(true);
            endGameButton.setManaged(true);
        } else {
            waveLabel.setText("Wave: " + gamePanel.getCurrentWave() + "/" + gamePanel.getTotalWaves());
            waveLabel.setStyle("-fx-text-fill: #64b5f6; -fx-font-family: 'Microsoft YaHei', 'PingFang SC'; -fx-font-size: 14px; -fx-font-weight: bold;");
            // 普通模式隐藏结束游戏按钮
            endGameButton.setVisible(false);
            endGameButton.setManaged(false);
        }
        scoreLabel.setText("Score: " + gamePanel.getScore());

        // 更新等级和经验条
        int currentLevel = gamePanel.getPlayerLevel();
        int currentExp = gamePanel.getPlayerXP();
        int expMax = gamePanel.getXpMax();
        levelLabel.setText("Lv." + currentLevel);
        expBarText.setText(currentExp + "/" + expMax);
        double ratio = expMax > 0 ? (double) currentExp / expMax : 0;
        expBarFill.setPrefWidth(Math.max(0, Math.min(80, (int) (80 * ratio))));

        // 更新塔按钮
        int gold = gamePanel.getPlayerGold();
        int playerLevel = gamePanel.getPlayerLevel();
        for (int i = 0; i < towerButtons.length; i++) {
            int hudIndex = gamePanel.getPurchasedTowerIndices().get(i);
            int unlockLevel = gamePanel.getTowerUnlockLevel(hudIndex);
            boolean unlocked = playerLevel >= unlockLevel;
            boolean canAfford = gold >= towerCosts[hudIndex];

            VBox content = (VBox) towerButtons[i].getGraphic();
            Label costLabel = (Label) content.getChildren().get(2);
            if (unlocked) {
                costLabel.setText("$" + towerCosts[hudIndex]);
                costLabel.setTextFill(canAfford ? Color.web("#ffd54f") : Color.web("#ff6b6b"));
            } else {
                costLabel.setText("LV." + unlockLevel);
                costLabel.setTextFill(Color.web("#ff6b6b"));
            }

            if (unlocked) {
                towerButtons[i].setDisable(false);
                towerButtons[i].setOpacity(1.0);
                towerButtons[i].setCursor(javafx.scene.Cursor.HAND);
            } else {
                towerButtons[i].setDisable(true);
                towerButtons[i].setOpacity(0.45);
                towerButtons[i].setCursor(javafx.scene.Cursor.DEFAULT);
            }

            TowerType selected = gamePanel.getSelectedTowerType();
            if (selected == towerTypes[hudIndex]) {
                if (!towerButtons[i].getStyleClass().contains("selected")) {
                    towerButtons[i].getStyleClass().add("selected");
                }
            } else {
                towerButtons[i].getStyleClass().remove("selected");
            }
        }

        // 左侧详情信息卡：更新选中塔属性
        Tower tower = gamePanel.getSelectedTower();
        if (tower != null) {
            detailNameLabel.setText(tower.getType() + "  Lv." + tower.getLevel());
            detailNameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            detailStatsLabel.setText("伤害:" + tower.getDamage() + "  范围:" + String.format("%.1f", tower.getRange())
                + "  攻速:" + String.format("%.1f", tower.getFireRate() / 1000.0) + "s");
            detailStatsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");

            // 升级按钮
            if (tower.canUpgrade()) {
                upgradeButton.setVisible(true);
                upgradeButton.setManaged(true);
                upgradeButton.setText("升级 $" + tower.getUpgradeCost());
                upgradeButton.setDisable(gamePanel.getPlayerGold() < tower.getUpgradeCost());
            } else {
                upgradeButton.setVisible(false);
                upgradeButton.setManaged(false);
            }

            // 出售按钮
            sellButton.setVisible(true);
            sellButton.setManaged(true);
            sellButton.setText("出售 $" + tower.getSellValue());
        } else {
            detailNameLabel.setText("未选中防御塔");
            detailNameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #a0aec0;");
            detailStatsLabel.setText("");
            upgradeButton.setVisible(false);
            upgradeButton.setManaged(false);
            sellButton.setVisible(false);
            sellButton.setManaged(false);
        }

        // 取消放置按钮
        if (gamePanel.isPlacementMode()) {
            if (cancelPlacementButton == null) {
                cancelPlacementButton = createCancelButton();
                getChildren().add(cancelPlacementButton);
            }
            cancelPlacementButton.setVisible(true);
            cancelPlacementButton.setManaged(true);
        } else {
            if (cancelPlacementButton != null) {
                cancelPlacementButton.setVisible(false);
                cancelPlacementButton.setManaged(false);
            }
        }

        updateNextWaveButton();
    }

    private Button createCancelButton() {
        Button btn = new Button("✕ 取消放置");
        btn.setFocusTraversable(false);
        btn.setPrefSize(120, 40);
        btn.getStyleClass().add("btn-cancel-placement");
        btn.setOnAction(e -> gamePanel.cancelPlacement());
        return btn;
    }

    private Button createNextWaveButton() {
        Button btn = new Button("下一波");
        btn.setFocusTraversable(false);
        btn.setMinWidth(120);
        btn.setPrefWidth(120);
        btn.setPrefHeight(36);
        btn.getStyleClass().add("btn-next-wave");
        btn.setOnAction(e -> gamePanel.startNextWave());
        return btn;
    }

    private Button createShopButton() {
        Button btn = new Button(" 商城");
        btn.setFocusTraversable(false);
        btn.setMinWidth(90);
        btn.setPrefWidth(90);
        btn.setPrefHeight(36);
        btn.getStyleClass().add("btn-shop");
        btn.setOnAction(e -> gamePanel.showShopFromPlaying());
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.06);
            st.setToY(1.06);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        return btn;
    }

    /**
     * 无尽模式【结束游戏】按钮：红色高亮，点击弹出确认框后结算。
     */
    private Button createEndGameButton() {
        Button btn = new Button("结束游戏");
        btn.setFocusTraversable(false);
        btn.setMinWidth(100);
        btn.setPrefWidth(100);
        btn.setPrefHeight(30);
        btn.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-color: linear-gradient(to bottom, #e53e3e, #c53030);" +
            "-fx-text-fill: white; -fx-background-radius: 6;" +
            "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 6; -fx-border-width: 1;" +
            "-fx-cursor: hand;");
        btn.setVisible(false);
        btn.setManaged(false);

        btn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("结束游戏确认");
            alert.setHeaderText("确定要现在结束并结算无尽模式吗？");
            alert.setContentText("提前结束将按照当前积分 [" + gamePanel.getScore() + "] 计入排行榜！");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                gamePanel.triggerEndlessSettlement();
            }
        });

        return btn;
    }

    /**
     * 显示临时提示信息。
     */
    public void showToast(String message, boolean isWarning) {
        Label toast = new Label(message);
        toast.setFont(javafx.scene.text.Font.font("Microsoft YaHei", 14));
        toast.setTextFill(isWarning ? Color.web("#ff6b6b") : Color.web("#64ffda"));
        toast.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 8px 16px; -fx-background-radius: 6px;");
        toast.setOpacity(0);
        getChildren().add(0, toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(1.5));
        fadeOut.setOnFinished(e -> getChildren().remove(toast));
        new SequentialTransition(fadeIn, fadeOut).play();
    }

    /**
     * 更新下一波按钮状态。
     */
    private void updateNextWaveButton() {
        boolean waveActive = gamePanel.isWaveActive();
        boolean allComplete = gamePanel.isAllWavesComplete();
        int currentWave = gamePanel.getCurrentWave();
        int totalWaves = gamePanel.getTotalWaves();
        boolean endless = gamePanel.isEndlessMode();

        if (allComplete && !endless) {
            nextWaveButton.setText("已完成");
            nextWaveButton.setDisable(true);
            nextWaveButton.setOpacity(0.5);
            nextWaveButton.getStyleClass().remove("btn-next-wave-ready");
            nextWaveButton.getStyleClass().add("btn-next-wave-disabled");
        } else if (waveActive) {
            nextWaveButton.setText("波次中...");
            nextWaveButton.setDisable(true);
            nextWaveButton.setOpacity(0.5);
            nextWaveButton.getStyleClass().remove("btn-next-wave-ready");
            nextWaveButton.getStyleClass().add("btn-next-wave-disabled");
        } else if (currentWave == 0) {
            nextWaveButton.setText(endless ? "开始无尽" : "开始第 1 波");
            nextWaveButton.setDisable(false);
            nextWaveButton.setOpacity(1.0);
            nextWaveButton.getStyleClass().add("btn-next-wave-ready");
            nextWaveButton.getStyleClass().remove("btn-next-wave-disabled");
        } else {
            String label = endless ? "下一波 (" + (currentWave + 1) + ")"
                                   : "下一波 (" + (currentWave + 1) + "/" + totalWaves + ")";
            nextWaveButton.setText(label);
            nextWaveButton.setDisable(false);
            nextWaveButton.setOpacity(1.0);
            nextWaveButton.getStyleClass().add("btn-next-wave-ready");
            nextWaveButton.getStyleClass().remove("btn-next-wave-disabled");
        }
    }
}
