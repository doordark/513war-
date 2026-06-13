package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * 游戏主窗口 —— JavaFX 入口。
 */
public class GameMain extends Application {

    private GamePanel gamePanel;
    private GameEngine gameEngine;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("513保卫战");
        primaryStage.setResizable(false);

        // 创建游戏面板
        gamePanel = new GamePanel();

        // 使用 StackPane 叠加：底层游戏区域 + 上层菜单/暂停弹窗
        StackPane root = new StackPane();
        root.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        // 根容器背景色设为与底栏一致的暗色，防止子组件间缝隙漏出纯黑色
        root.setStyle("-fx-background-color: #0f121c;");

        // 游戏区域容器（Canvas + HUD）
        BorderPane gameArea = new BorderPane();
        gameArea.setMinSize(960, 710);
        gameArea.setMaxSize(960, 710);
        gameArea.setStyle("-fx-background-color: #0f121c;");

        // 用 StackPane 包裹 Canvas 并左对齐，防止居中产生黑边
        StackPane canvasContainer = new StackPane(gamePanel.getCanvas());
        canvasContainer.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        canvasContainer.setPadding(new javafx.geometry.Insets(0));
        canvasContainer.setStyle("-fx-background-color: #0f121c;");
        BorderPane.setMargin(canvasContainer, javafx.geometry.Insets.EMPTY);
        gameArea.setCenter(canvasContainer);
        gameArea.setBottom(gamePanel.getHudRoot());
        BorderPane.setMargin(gamePanel.getHudRoot(), javafx.geometry.Insets.EMPTY);

        root.getChildren().addAll(gameArea, gamePanel.getMainMenu(), gamePanel.getDifficultyOverlay(),
                                   gamePanel.getSettingsOverlay(), gamePanel.getShopOverlay(),
                                   gamePanel.getPauseOverlay(), gamePanel.getLeaderboardOverlay());

        Scene scene = new Scene(root, 960, 710);
        scene.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());

        // 键盘事件过滤器（优先于所有子组件捕获按键，确保 ESC 始终有效）
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            gamePanel.handleKeyPress(e.getCode());
            // 只消费 ESC 键，防止传播到 Canvas 导致重复处理
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                e.consume();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        // 显示主菜单前先加载存档（如有）
        GameSave.SaveData savedData = GameSave.load();
        if (savedData != null) {
            GameData.hp = savedData.playerLives;
            GameData.gold = savedData.playerGold;
            GameData.exp = 0;
            GameData.level = 1;
            GameData.score = savedData.score;
            System.out.println("[启动] 已加载存档数据到 GameData");
        }

        gamePanel.getMainMenu().show();

        // 主菜单时隐藏 HUD
        gamePanel.getHudRoot().setVisible(false);
        gamePanel.getHudRoot().setManaged(false);

        // 设置主菜单背景图片
        gamePanel.getMainMenu().setBackgroundImage("file:image/meau.jpg");

        // 创建并启动游戏引擎
        gameEngine = new GameEngine(gamePanel);
        gamePanel.setGameEngine(gameEngine);
        gameEngine.start();

        // 窗口关闭时清理
        primaryStage.setOnCloseRequest(e -> gameEngine.stop());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
