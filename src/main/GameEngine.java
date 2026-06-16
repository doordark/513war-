package main;

import javafx.animation.AnimationTimer;

/**
 * 游戏引擎 —— 使用 JavaFX AnimationTimer 实现 60FPS 主循环。
 */
public class GameEngine {

    private static final long FRAME_TIME_NANOS = 1_000_000_000L / 60; // 60 FPS

    private final GamePanel gamePanel;
    private AnimationTimer gameLoop;
    private boolean running;
    private long lastUpdateTime;

    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.running = false;
        this.lastUpdateTime = 0;
    }

    /**
     * 启动游戏循环。
     */
    public void start() {
        if (running) return;
        running = true;
        lastUpdateTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running) return;

                // 固定帧率：确保每帧至少间隔 FRAME_TIME_NANOS
                long elapsed = now - lastUpdateTime;
                if (elapsed < FRAME_TIME_NANOS) return;

                lastUpdateTime = now;

                // 更新游戏逻辑
                gamePanel.updateGame();

                // 更新 BGM 循环
                sound.SoundManager.getInstance().updateBGM();

                // 请求 Canvas 重绘
                gamePanel.render();
            }
        };

        gameLoop.start();
    }

    /**
     * 停止游戏循环。
     */
    public void stop() {
        running = false;
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    public boolean isRunning() {
        return running;
    }
}
