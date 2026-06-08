package entity.tower;

import entity.monster.Monster;
import entity.Projectile;
import map.GameMap;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * 防御塔基类 —— 抽象塔的共同属性和行为。
 */
public abstract class Tower {

    protected final int row, col;
    protected final int pixelX, pixelY;
    protected String name;
    protected int level;
    protected int buyCost;
    protected int[] upgradeCosts;  // [2级花费, 3级花费]
    protected double range;        // 攻击范围（格子数）
    protected long fireRate;       // 攻击间隔（毫秒）
    protected int damage;          // 基础伤害
    protected long lastFireTime;
    protected Monster currentTarget;
    protected Color color;

    public Tower(int row, int col, int pixelX, int pixelY) {
        this.row = row;
        this.col = col;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.level = 1;
        this.lastFireTime = 0;
        this.currentTarget = null;
    }

    /**
     * 每帧更新：寻找目标 → 开火。
     */
    public void update(List<Monster> monsters, List<Projectile> projectiles) {
        currentTarget = findTarget(monsters);
        if (currentTarget != null && canFire(System.currentTimeMillis())) {
            lastFireTime = System.currentTimeMillis();
            Projectile proj = createProjectile(currentTarget);
            projectiles.add(proj);
            System.out.println("[防御塔开火] 类型=" + getType() + " 目标=" + currentTarget.getType()
                + " 伤害=" + damage + " 坐标=(" + pixelX + "," + pixelY + ")");
        }
    }

    /**
     * 寻找攻击目标（优先选择离终点最近的）。
     */
    protected Monster findTarget(List<Monster> monsters) {
        Monster best = null;
        double bestProgress = -1;

        for (Monster m : monsters) {
            if (!m.isAlive()) continue;
            if (!isInRange(m)) continue;

            double progress = m.getWaypointIndex();
            if (progress > bestProgress) {
                bestProgress = progress;
                best = m;
            }
        }
        return best;
    }

    protected boolean isInRange(Monster m) {
        double dx = m.getX() - pixelX;
        double dy = m.getY() - pixelY;
        double rangePx = range * GameMap.TILE_SIZE;
        return dx * dx + dy * dy <= rangePx * rangePx;
    }

    protected boolean canFire(long now) {
        return now - lastFireTime >= fireRate;
    }

    public boolean canUpgrade() {
        return level < 3;
    }

    public void upgrade() {
        if (!canUpgrade()) return;
        level++;
        damage += (int) (damage * 0.3);
        range += 0.5;
        fireRate = (long) (fireRate * 0.9);
    }

    public int getUpgradeCost() {
        if (level >= 3) return Integer.MAX_VALUE;
        return upgradeCosts[level - 1];
    }

    public int getSellValue() {
        return (int) (buyCost * 0.5 * level);
    }

    public void render(GraphicsContext gc) {
        // 塔身
        gc.setFill(color);
        int size = 20 + level * 4;
        gc.fillOval(pixelX - size / 2, pixelY - size / 2, size, size);

        // 等级标记
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(level), pixelX - 3, pixelY + 4);
    }

    public void renderRangeCircle(GraphicsContext gc) {
        int rangePx = (int) (range * 48);
        gc.setStroke(Color.rgb(255, 255, 255, 0.2));
        gc.setLineWidth(1.5);
        gc.strokeOval(pixelX - rangePx, pixelY - rangePx, rangePx * 2, rangePx * 2);
    }

    // ==================== 抽象方法 ====================
    public abstract String getType();
    public abstract Projectile createProjectile(Monster target);

    // ==================== Getter ====================
    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getPixelX() { return pixelX; }
    public int getPixelY() { return pixelY; }
    public int getLevel() { return level; }
    public double getRange() { return range; }
    public int getDamage() { return damage; }
    public int getBuyCost() { return buyCost; }
    public void setDamage(int damage) { this.damage = damage; }
    public void setRange(double range) { this.range = range; }
    public void setFireRate(long fireRate) { this.fireRate = fireRate; }
    public void setName(String name) { this.name = name; }
    public void setColor(Color color) { this.color = color; }
}
