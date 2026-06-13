package entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 爆炸效果 —— 炮塔子弹击中时绘制逐渐变大、透明度降低的橙色圆形。
 */
public class ExplosionEffect {

    private double x, y;
    private double radius;
    private long spawnTime;
    private static final long LIFETIME = 400; // 0.4秒
    private static final double MAX_RADIUS = 50;
    private Color color;

    public ExplosionEffect(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.radius = 0;
        this.color = color;
        this.spawnTime = System.currentTimeMillis();
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        double progress = (double) elapsed / LIFETIME;
        radius = MAX_RADIUS * progress;
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
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.restore();
    }
}
