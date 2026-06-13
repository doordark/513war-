package entity.monster;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Boss怪物 —— 超高血量。
 */
public class BossMonster extends Monster {

    public BossMonster(double x, double y) {
        super(x, y, 500, 0.5, 10, 50, 100, 5, Color.MAGENTA, "boss");
        this.name = "Boss";
        this.size = 28; // Boss 更大
    }

    @Override
    public String getType() {
        return "BOSS";
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isAlive()) return;

        // Boss 贴图绘制（优先使用图片，找不到则用兜底图形）
        if (texture != null) {
            gc.drawImage(texture, x - size / 2, y - size / 2, size, size);
        } else {
            // 兜底：彩色圆圈
            gc.setFill(color);
            gc.fillOval(x - size / 2, y - size / 2, size, size);
        }

        // 减速效果
        if (slowFactor < 1.0) {
            gc.setFill(Color.rgb(100, 200, 255, 0.4));
            gc.fillOval(x - size / 2 - 2, y - size / 2 - 2, size + 4, size + 4);
        }

        // 血条（更宽）
        renderHealthBar(gc);
    }

    private void renderHealthBar(GraphicsContext gc) {
        int barWidth = 36;
        int barHeight = 5;
        int barX = (int) x - barWidth / 2;
        int barY = (int) y - 22;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(barX, barY, barWidth, barHeight);

        double ratio = (double) getCurrentHp() / getMaxHp();
        if (ratio > 0.5) {
            gc.setFill(Color.LIMEGREEN);
        } else if (ratio > 0.25) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.RED);
        }
        gc.fillRect(barX, barY, (int) (barWidth * ratio), barHeight);
    }
}
