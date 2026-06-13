package entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 伤害飘字 —— 怪物受伤时在头顶显示伤害数值。
 * 每帧向上移动，透明度逐渐降低，0.5秒后自动销毁。
 */
public class FloatingText {

    private double x, y;
    private String text;
    private Color color;
    private long spawnTime;
    private static final long LIFETIME = 500; // 0.5秒
    private static final double RISE_SPEED = 0.8; // 每帧上升像素

    public FloatingText(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.spawnTime = System.currentTimeMillis();
    }

    public void update() {
        y -= RISE_SPEED;
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
        gc.setFill(color);
        gc.setFont(javafx.scene.text.Font.font("Microsoft YaHei", javafx.scene.text.FontWeight.BOLD, 14));
        gc.fillText(text, x, y);
        gc.restore();
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
