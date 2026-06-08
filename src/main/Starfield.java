package main;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 粒子星空背景 — AnimationTimer 驱动的动态星空效果。
 * 包含 80 个随机微粒，以极慢速度漂移并随机改变透明度（星星眨眼效果）。
 */
public class Starfield {

    private static final int STAR_COUNT = 80;

    private final Star[] stars;

    public Starfield(int width, int height) {
        stars = new Star[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i] = new Star(width, height);
        }
    }

    /** 更新所有微粒状态（每帧调用） */
    public void update(long elapsedMs) {
        double dt = elapsedMs / 1000.0;
        for (Star s : stars) {
            s.update(dt);
        }
    }

    /** 渲染星空背景 */
    public void render(GraphicsContext gc, int width, int height) {
        // 深色渐变底色
        gc.setFill(Color.web("#0a0a1a"));
        gc.fillRect(0, 0, width, height);

        // 绘制微粒
        for (Star s : stars) {
            gc.setFill(Color.rgb(180, 200, 255, s.alpha));
            gc.fillOval(s.x - s.size / 2, s.y - s.size / 2, s.size, s.size);
        }
    }

    /** 单个微粒 */
    private static class Star {
        double x, y;
        double size;
        double alpha;
        double alphaSpeed;
        double alphaDir;
        double driftX, driftY;

        Star(int width, int height) {
            x = Math.random() * width;
            y = Math.random() * height;
            size = 0.5 + Math.random() * 2.0;
            alpha = 0.2 + Math.random() * 0.6;
            alphaSpeed = 0.3 + Math.random() * 0.8;
            alphaDir = Math.random() > 0.5 ? 1 : -1;
            driftX = (Math.random() - 0.5) * 3;
            driftY = (Math.random() - 0.5) * 2;
        }

        void update(double dt) {
            // 极慢漂移
            x += driftX * dt;
            y += driftY * dt;

            // 边界环绕
            if (x < -5) x = 965;
            if (x > 965) x = -5;
            if (y < -5) y = 605;
            if (y > 605) y = -5;

            // 透明度呼吸（眨眼效果）
            alpha += alphaSpeed * alphaDir * dt;
            if (alpha > 0.9) { alpha = 0.9; alphaDir = -1; }
            if (alpha < 0.1) { alpha = 0.1; alphaDir = 1; }
        }
    }
}
