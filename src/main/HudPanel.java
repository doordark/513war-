package main;

import enums.TowerType;
import entity.tower.Tower;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

/**
 * HUD 面板 — 现代扁平科技风（磨砂玻璃卡片、塔按钮美化、图标修复）
 */
public class HudPanel extends VBox {

    private final GamePanel gamePanel;

    // 游戏信息标签
    private Label hpLabel;
    private Label xpLabel;
    private Label goldLabel;
    private Label waveLabel;
    private Label scoreLabel;
    private Label levelLabel;

    // 塔购买按钮
    private HBox towerButtonBox;
    private Button[] towerButtons;
    private TowerType[] towerTypes = TowerType.values();
    private String[] towerNames = {"箭塔", "炮塔", "魔法塔", "减速塔", "闪电塔", "核弹塔"};
    private int[] towerCosts = {50, 100, 150, 75, 150, 500};
    private Color[] towerColors = {
        Color.web("#4caf50"),
        Color.web("#ff7043"),
        Color.web("#ab47bc"),
        Color.web("#42a5f5"),
        Color.web("#9664ff"),
        Color.web("#ff4444")
    };

    // 选中塔信息面板
    private HBox towerInfoPanel;
    private Label towerInfoLabel;
    private Label towerStatsLabel;
    private Button upgradeButton;
    private Button sellButton;

    // 取消放置按钮
    private Button cancelPlacementButton;

    // 下一波按钮
    private Button nextWaveButton;

    public HudPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        // 加载 CSS
        String cssPath = getClass().getResource("/styles/game.css").toExternalForm();
        getStylesheets().add(cssPath);
        getStyleClass().add("hud-panel");
        setPrefHeight(60);
        setMinHeight(60);
        setMaxHeight(60);
        setPadding(new Insets(6, 16, 6, 16));
        setSpacing(0);
        setAlignment(Pos.CENTER);

        // 布局：左侧信息 + 分隔线 + 中间塔按钮 + 分隔线 + 右侧选中塔信息
        HBox mainRow = new HBox();
        mainRow.setSpacing(0);
        mainRow.setAlignment(Pos.CENTER);
        mainRow.setPadding(new Insets(0));

        // 左侧：游戏信息（磨砂玻璃卡片）
        HBox infoBox = createInfoBox();
        HBox.setHgrow(infoBox, Priority.NEVER);
        mainRow.getChildren().add(infoBox);

        // 分隔线
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setPrefHeight(50);
        sep1.getStyleClass().add("hud-separator");
        mainRow.getChildren().add(sep1);

        // 中间：塔购买按钮
        HBox towerBox = createTowerButtons();
        HBox.setHgrow(towerBox, Priority.ALWAYS);
        mainRow.getChildren().add(towerBox);

        // 取消放置按钮（初始隐藏）
        cancelPlacementButton = createCancelButton();
        cancelPlacementButton.setVisible(false);
        cancelPlacementButton.setManaged(false);
        mainRow.getChildren().add(cancelPlacementButton);

        // 下一波按钮
        nextWaveButton = createNextWaveButton();
        mainRow.getChildren().add(nextWaveButton);

        // 分隔线
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setPrefHeight(50);
        sep2.getStyleClass().add("hud-separator");
        mainRow.getChildren().add(sep2);

        // 右侧：选中塔信息（初始隐藏）
        towerInfoPanel = createTowerInfoPanel();
        towerInfoPanel.setVisible(false);
        towerInfoPanel.setManaged(false);
        mainRow.getChildren().add(towerInfoPanel);

        getChildren().add(mainRow);
    }

    /**
     * 创建左侧游戏信息区域 — 磨砂玻璃卡片包裹。
     */
    private HBox createInfoBox() {
        HBox box = new HBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 12, 0, 0));
        box.getStyleClass().add("stat-card");

        hpLabel = createInfoLabel("♥ 20", "stat-label stat-hp");
        xpLabel = createInfoLabel("★ 0/100", "stat-label stat-xp");
        goldLabel = createInfoLabel("G 500", "stat-label stat-gold");
        waveLabel = createInfoLabel("W 0/0", "stat-label stat-wave");
        scoreLabel = createInfoLabel("S 0", "stat-label stat-score");
        levelLabel = createInfoLabel("LV.1", "stat-label stat-level");

        box.getChildren().addAll(hpLabel, xpLabel, goldLabel, waveLabel, scoreLabel, levelLabel);
        return box;
    }

    private Label createInfoLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClass.split(" "));
        return label;
    }

    /**
     * 创建中间塔购买按钮区域。
     */
    private HBox createTowerButtons() {
        towerButtonBox = new HBox();
        towerButtonBox.setSpacing(10);
        towerButtonBox.setAlignment(Pos.CENTER);
        towerButtonBox.setPadding(new Insets(0, 15, 0, 15));

        // 初始只创建箭塔按钮（LV.1 解锁）
        updateTowerButtons();
        return towerButtonBox;
    }

    /**
     * 根据已购买的塔类型动态更新按钮列表。
     */
    public void updateTowerButtons() {
        towerButtonBox.getChildren().clear();
        
        // 获取已购买的塔索引列表（从 GamePanel）
        java.util.List<Integer> purchasedTowers = gamePanel.getPurchasedTowerIndices();
        
        // 始终包含箭塔（索引 0）
        if (!purchasedTowers.contains(0)) {
            purchasedTowers.add(0);
        }
        
        // 按顺序添加按钮
        towerButtons = new Button[purchasedTowers.size()];
        for (int i = 0; i < purchasedTowers.size(); i++) {
            int towerIdx = purchasedTowers.get(i);
            towerButtons[i] = createTowerButton(towerIdx);
            towerButtonBox.getChildren().add(towerButtons[i]);
        }
    }

    private Button createTowerButton(int index) {
        Button btn = new Button();
        btn.setPrefSize(72, 58);
        btn.setMinSize(72, 58);
        btn.setMaxSize(72, 58);
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add("tower-button");

        // 使用 VBox 堆叠内容
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(1);

        // 快捷键标签（小字号、半透明白）
        Label keyLabel = new Label("[" + (index + 1) + "]");
        keyLabel.getStyleClass().add("tower-key");

        // 塔名称
        Label nameLabel = new Label(towerNames[index]);
        nameLabel.getStyleClass().add("tower-name");

        // 解锁等级/价格
        Label costLabel = new Label("LV." + gamePanel.getTowerUnlockLevel(index));
        costLabel.getStyleClass().add("tower-cost");

        content.getChildren().addAll(keyLabel, nameLabel, costLabel);
        btn.setGraphic(content);

        // 点击事件
        final int idx = index;
        btn.setOnAction(e -> gamePanel.selectTowerType(towerTypes[idx]));

        return btn;
    }

    /**
     * 创建右侧选中塔信息面板。
     */
    private HBox createTowerInfoPanel() {
        HBox panel = new HBox();
        panel.setSpacing(12);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(5, 15, 5, 15));
        panel.getStyleClass().add("tower-info-panel");

        // 信息列
        VBox infoCol = new VBox();
        infoCol.setSpacing(4);
        towerInfoLabel = new Label("");
        towerInfoLabel.getStyleClass().add("tower-info-name");
        towerStatsLabel = new Label("");
        towerStatsLabel.getStyleClass().add("tower-info-stats");
        infoCol.getChildren().addAll(towerInfoLabel, towerStatsLabel);

        // 按钮列
        VBox btnCol = new VBox();
        btnCol.setSpacing(6);
        btnCol.setAlignment(Pos.CENTER);

        upgradeButton = new Button("升级");
        upgradeButton.setPrefSize(80, 28);
        upgradeButton.getStyleClass().add("btn-upgrade");
        upgradeButton.setOnAction(e -> {
            Tower t = gamePanel.getSelectedTower();
            if (t != null) gamePanel.upgradeTower(t);
        });

        sellButton = new Button("出售");
        sellButton.setPrefSize(80, 28);
        sellButton.getStyleClass().add("btn-sell");
        sellButton.setOnAction(e -> {
            Tower t = gamePanel.getSelectedTower();
            if (t != null) gamePanel.sellTower(t);
        });

        btnCol.getChildren().addAll(upgradeButton, sellButton);
        panel.getChildren().addAll(infoCol, btnCol);
        return panel;
    }

    /**
     * 更新 HUD 状态（游戏数据变化时调用）。
     */
    public void updateState() {
        // 更新游戏信息
        hpLabel.setText("♥ " + gamePanel.getPlayerLives());
        int currentXP = gamePanel.getPlayerXP();
        int nextLevelXP = gamePanel.getXpToNextLevel();
        xpLabel.setText("★ " + currentXP + "/" + nextLevelXP);
        goldLabel.setText("G " + gamePanel.getPlayerGold());

        // 低血量时红色警告
        boolean lowLives = gamePanel.getPlayerLives() <= 5;
        if (lowLives) {
            hpLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Microsoft YaHei', 'PingFang SC'; -fx-font-size: 13px; -fx-font-weight: bold;");
        } else {
            hpLabel.setStyle("");
            hpLabel.getStyleClass().clear();
            hpLabel.getStyleClass().addAll("stat-label", "stat-hp");
        }

        waveLabel.setText("W " + gamePanel.getCurrentWave() + "/" + gamePanel.getTotalWaves());
        scoreLabel.setText("S " + gamePanel.getScore());
        int playerLevel = gamePanel.getPlayerLevel();
        levelLabel.setText("LV." + playerLevel);

        // 更新塔按钮：根据等级解锁/锁定
        int gold = gamePanel.getPlayerGold();
        for (int i = 0; i < towerButtons.length; i++) {
            int hudIndex = gamePanel.getPurchasedTowerIndices().get(i);
            int unlockLevel = gamePanel.getTowerUnlockLevel(hudIndex);
            boolean unlocked = playerLevel >= unlockLevel;
            boolean canAfford = gold >= towerCosts[hudIndex];

            // 动态更新按钮文字：未解锁显示等级，已解锁显示价格
            VBox content = (VBox) towerButtons[i].getGraphic();
            Label costLabel = (Label) content.getChildren().get(2);
            if (unlocked) {
                costLabel.setText("$" + towerCosts[hudIndex]);
                costLabel.setTextFill(Color.web("#ffd54f"));
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

            // 更新选中状态
            TowerType selected = gamePanel.getSelectedTowerType();
            if (selected == towerTypes[hudIndex]) {
                towerButtons[i].getStyleClass().add("selected");
            } else {
                towerButtons[i].getStyleClass().remove("selected");
            }
        }

        // 更新选中塔信息面板
        Tower tower = gamePanel.getSelectedTower();
        if (tower != null) {
            towerInfoPanel.setVisible(true);
            towerInfoPanel.setManaged(true);
            towerInfoLabel.setText(tower.getType() + " Lv." + tower.getLevel());
            towerStatsLabel.setText("伤害: " + tower.getDamage() + "  范围: " + String.format("%.1f", tower.getRange()));

            if (tower.canUpgrade()) {
                upgradeButton.setVisible(true);
                upgradeButton.setManaged(true);
                upgradeButton.setText("升级 $" + tower.getUpgradeCost());
                upgradeButton.setDisable(gamePanel.getPlayerGold() < tower.getUpgradeCost());
            } else {
                upgradeButton.setVisible(false);
                upgradeButton.setManaged(false);
            }

            sellButton.setText("出售 $" + tower.getSellValue());
        } else {
            towerInfoPanel.setVisible(false);
            towerInfoPanel.setManaged(false);
        }

        // 更新取消放置按钮
        if (gamePanel.isPlacementMode()) {
            cancelPlacementButton.setVisible(true);
            cancelPlacementButton.setManaged(true);
        } else {
            cancelPlacementButton.setVisible(false);
            cancelPlacementButton.setManaged(false);
        }

        // 更新下一波按钮
        updateNextWaveButton();
    }

    private Button createCancelButton() {
        Button btn = new Button("取消放置 (ESC)");
        btn.setPrefSize(120, 40);
        btn.getStyleClass().add("btn-cancel-placement");
        btn.setOnAction(e -> gamePanel.cancelPlacement());
        return btn;
    }

    private Button createNextWaveButton() {
        Button btn = new Button("下一波");
        btn.setPrefSize(100, 40);
        btn.getStyleClass().add("btn-next-wave");
        btn.setOnAction(e -> gamePanel.startNextWave());
        return btn;
    }

    /**
     * 更新下一波按钮状态。
     */
    private void updateNextWaveButton() {
        boolean waveActive = gamePanel.isWaveActive();
        boolean allComplete = gamePanel.isAllWavesComplete();
        int currentWave = gamePanel.getCurrentWave();
        int totalWaves = gamePanel.getTotalWaves();

        if (allComplete) {
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
            nextWaveButton.setText("开始第 1 波");
            nextWaveButton.setDisable(false);
            nextWaveButton.setOpacity(1.0);
            nextWaveButton.getStyleClass().add("btn-next-wave-ready");
            nextWaveButton.getStyleClass().remove("btn-next-wave-disabled");
        } else {
            nextWaveButton.setText("下一波 (" + (currentWave + 1) + "/" + totalWaves + ")");
            nextWaveButton.setDisable(false);
            nextWaveButton.setOpacity(1.0);
            nextWaveButton.getStyleClass().add("btn-next-wave-ready");
            nextWaveButton.getStyleClass().remove("btn-next-wave-disabled");
        }
    }
}
