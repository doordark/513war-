package entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 闪电效果 —— 防御塔攻击时在塔与目标之间绘制闪电折线。
 */
public class LightningEffect {

    private double startX, startY, endX, endY;
    private long spawnTime;
    private static final long LIFETIME = 150; // 0.15秒
    private Color color;

    public LightningEffect(double startX, double startY, double endX, double endY, Color color) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.spawnTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime >= LIFETIME;
    }

    public double getAlpha() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        return Math.max(0, 1.0 - (double) elapsed / LIFETIME);
    }

    public void render(GraphicsContext gc) {
        double alpha = getAlpha();
        if (alpha <= 0) return;

        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.setEffect(new javafx.scene.effect.Glow(0.8));

        // 绘制闪电折线（3段随机偏移）
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        double offsetX = (Math.random() - 0.5) * 30;
        double offsetY = (Math.random() - 0.5) * 30;

        gc.strokeLine(startX, startY, midX + offsetX, midY + offsetY);
        gc.strokeLine(midX + offsetX, midY + offsetY, endX, endY);

        gc.restore();
    }
}
