package entity.monster;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * 怪物基类 —— 沿路径移动、承受伤害。
 */
public abstract class Monster {

    protected int id;
    protected String name;
    protected double x, y;
    protected int waypointIndex;
    protected int maxHp, currentHp;
    protected double speed;
    protected int armor;
    protected int rewardGold;
    protected int xpReward;
    protected int damageToPlayer;
    protected boolean alive;
    protected boolean reachedEnd;
    protected Color color;
    protected double slowFactor;    // 减速因子 (0.5 = 半速)
    protected long slowEndTime;     // 减速结束时间戳
    protected String lastHitByTowerType; // 最后一次攻击的塔类型

    private static int nextId = 0;

    public Monster(double x, double y, int maxHp, double speed, int armor,
                   int rewardGold, int xpReward, int damageToPlayer, Color color) {
        this.id = nextId++;
        this.x = x;
        this.y = y;
        this.waypointIndex = 0;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.speed = speed;
        this.armor = armor;
        this.rewardGold = rewardGold;
        this.xpReward = xpReward;
        this.damageToPlayer = damageToPlayer;
        this.alive = true;
        this.reachedEnd = false;
        this.color = color;
        this.slowFactor = 1.0;
        this.slowEndTime = 0;

        System.out.println("[怪物生成] ID=" + id + " 类型=" + getType()
            + " 出生点=(" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")"
            + " HP=" + maxHp + " 速度=" + speed);
    }

    /**
     * 每帧更新：沿路径移动。
     */
    public void update(List<Point2D> waypoints) {
        if (!alive) return;

        // 检查减速是否过期
        if (slowFactor < 1.0 && System.currentTimeMillis() > slowEndTime) {
            slowFactor = 1.0;
            System.out.println("[减速恢复] ID=" + id + " 类型=" + getType() + " 恢复正常速度");
        }

        // 沿路径移动
        moveAlongPath(waypoints);
    }

    /**
     * 沿路径移动到下一个路径点。
     */
    public void moveAlongPath(List<Point2D> waypoints) {
        if (!alive || waypoints.isEmpty()) return;
        if (waypointIndex >= waypoints.size()) {
            reachedEnd = true;
            System.out.println("[到达终点] ID=" + id + " 类型=" + getType()
                + " 最终坐标=(" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")");
            return;
        }

        Point2D target = waypoints.get(waypointIndex);
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < 2) {
            int oldIndex = waypointIndex;
            waypointIndex++;
            if (waypointIndex >= waypoints.size()) {
                reachedEnd = true;
                System.out.println("[到达终点] ID=" + id + " 类型=" + getType()
                    + " 最终坐标=(" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")");
            } else {
                Point2D nextTarget = waypoints.get(waypointIndex);
                System.out.println("[到达路径点] ID=" + id + " 类型=" + getType()
                    + " 从点" + oldIndex + "(" + String.format("%.0f", target.getX()) + "," + String.format("%.0f", target.getY()) + ")"
                    + " -> 点" + waypointIndex + "(" + String.format("%.0f", nextTarget.getX()) + "," + String.format("%.0f", nextTarget.getY()) + ")"
                    + " 进度=" + waypointIndex + "/" + waypoints.size());
            }
            return;
        }

        double effectiveSpeed = speed * slowFactor;
        double moveX = (dx / dist) * effectiveSpeed;
        double moveY = (dy / dist) * effectiveSpeed;

        x += moveX;
        y += moveY;
    }

    /**
     * 承受伤害，考虑护甲。
     */
    public void takeDamage(int rawDamage, String towerType) {
        int actualDamage = Math.max(1, rawDamage - armor);
        currentHp -= actualDamage;
        this.lastHitByTowerType = towerType;

        System.out.println("[受到伤害] ID=" + id + " 类型=" + getType()
            + " 原始伤害=" + rawDamage + " 护甲=" + armor + " 实际伤害=" + actualDamage
            + " 剩余HP=" + currentHp + "/" + maxHp
            + " 攻击塔=" + towerType);

        if (currentHp <= 0) {
            currentHp = 0;
            alive = false;
            System.out.println("[怪物死亡] ID=" + id + " 类型=" + getType()
                + " 坐标=(" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")"
                + " 击杀塔=" + towerType
                + " 路径进度=" + waypointIndex + "/" + (waypointIndex > 0 ? waypointIndex : "起点")
                + " 奖励金币=" + rewardGold);
        }
    }

    /**
     * 承受伤害（不记录击杀来源，用于溅射等场景）。
     */
    public void takeDamage(int rawDamage) {
        takeDamage(rawDamage, lastHitByTowerType != null ? lastHitByTowerType : "未知");
    }

    /**
     * 施加减速效果。
     */
    public void slow(double factor, long duration) {
        slowFactor = Math.min(slowFactor, factor);
        slowEndTime = System.currentTimeMillis() + duration;
        System.out.println("[减速效果] ID=" + id + " 类型=" + getType()
            + " 减速因子=" + factor + " 持续=" + duration + "ms");
    }

    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, maxHp);
    }

    public void render(GraphicsContext gc) {
        if (!alive) return;

        // 怪物身体
        int size = 16;
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size);

        // 减速效果标记（蓝色光环）
        if (slowFactor < 1.0) {
            gc.setFill(Color.rgb(100, 200, 255, 0.4));
            gc.fillOval(x - size / 2 - 2, y - size / 2 - 2, size + 4, size + 4);
        }

        // 血条
        renderHealthBar(gc);
    }

    private void renderHealthBar(GraphicsContext gc) {
        int barWidth = 24;
        int barHeight = 4;
        int barX = (int) x - barWidth / 2;
        int barY = (int) y - 16;

        // 背景
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(barX, barY, barWidth, barHeight);

        // 血量
        double ratio = (double) currentHp / maxHp;
        if (ratio > 0.5) {
            gc.setFill(Color.LIMEGREEN);
        } else if (ratio > 0.25) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.RED);
        }
        gc.fillRect(barX, barY, (int) (barWidth * ratio), barHeight);
    }

    /**
     * 获取碰撞矩形。
     */
    public Rectangle2D getBounds() {
        return new Rectangle2D(x - 8, y - 8, 16, 16);
    }

    // ==================== 抽象方法 ====================
    public abstract String getType();

    // ==================== Getter ====================
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWaypointIndex() { return waypointIndex; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public double getSpeed() { return speed; }
    public int getArmor() { return armor; }
    public int getRewardGold() { return rewardGold; }
    public int getXpReward() { return xpReward; }
    public int getDamageToPlayer() { return damageToPlayer; }
    public boolean isAlive() { return alive; }
    public boolean isReachedEnd() { return reachedEnd; }
    public String getLastHitByTowerType() { return lastHitByTowerType; }
    public double getSlowFactor() { return slowFactor; }
    public long getSlowEndTime() { return slowEndTime; }

    // ==================== Setter ====================
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setGoldReward(int rewardGold) { this.rewardGold = rewardGold; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
}
