package entity;

import entity.monster.Monster;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * 投射物 —— 从塔飞向怪物的攻击弹道。
 * 具备动态追踪功能：每帧重新计算方向向量，朝向目标当前位置移动。
 */
public class Projectile {

    private static final double HIT_DISTANCE = 5.0; // 击中判定距离（像素）

    private double x, y;
    private Monster target;
    private int damage;
    private double speed;
    private boolean splashDamage;
    private int splashRadius;
    private double slowFactor;
    private Color color;
    private boolean hit;
    private String towerType;

    // 击中时记录的位置（用于范围伤害计算）
    private double hitX, hitY;

    public Projectile(double startX, double startY, Monster target, int damage,
                      double speed, boolean splashDamage, int splashRadius,
                      double slowFactor, Color color, String towerType) {
        this.x = startX;
        this.y = startY;
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.splashDamage = splashDamage;
        this.splashRadius = splashRadius;
        this.slowFactor = slowFactor;
        this.color = color;
        this.hit = false;
        this.towerType = towerType;
        this.hitX = startX;
        this.hitY = startY;
    }

    /**
     * 每帧更新：追踪目标移动。
     * 重新计算方向向量，朝向目标当前位置飞行。
     * 当距离 < 5 像素时判定击中。
     */
    public void update() {
        if (hit) return;

        double targetX, targetY;
        if (target != null && target.isAlive()) {
            targetX = target.getX();
            targetY = target.getY();
        } else {
            // 目标已死亡，子弹继续飞向目标最后位置
            if (target != null) {
                targetX = target.getX();
                targetY = target.getY();
            } else {
                hit = true;
                return;
            }
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        // 击中判定：距离小于 5 像素
        if (dist < HIT_DISTANCE) {
            hit = true;
            hitX = targetX;
            hitY = targetY;
            return;
        }

        // 朝目标当前位置移动
        x += (dx / dist) * speed;
        y += (dy / dist) * speed;
    }

    /**
     * 对主目标造成伤害。
     */
    public void applyDamage(Monster m) {
        if (m == null || !m.isAlive()) return;
        m.takeDamage(damage, towerType);
        if (slowFactor > 0) {
            m.slow(slowFactor, 2000);
        }
    }

    /**
     * 范围伤害：对击中点周围 splashRadius 内的所有怪物造成伤害。
     * 伤害随距离衰减。
     */
    public void applySplashDamage(List<Monster> monsters) {
        double splashPx = splashRadius;
        List<Monster> affected = new ArrayList<>();
        for (Monster m : monsters) {
            if (!m.isAlive()) continue;
            double dx = m.getX() - hitX;
            double dy = m.getY() - hitY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= splashPx) {
                double ratio = 1.0 - (dist / splashPx);
                int actualDamage = (int) (damage * ratio);
                m.takeDamage(Math.max(1, actualDamage), towerType);
                affected.add(m);
            }
        }
    }

    /**
     * 处理击中事件：对目标造成伤害，如有范围伤害则额外处理。
     * 返回是否需要播放爆炸特效。
     */
    public boolean onHit(List<Monster> monsters) {
        if (hit && target != null) {
            // 对主目标造成伤害
            applyDamage(target);

            // 范围伤害
            if (splashDamage) {
                applySplashDamage(monsters);
                return true; // 需要爆炸特效
            }
        }
        return false;
    }

    public boolean hasHit() {
        return hit;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x - 3, y - 3, 6, 6);
    }

    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - 3, y - 3, 6, 6);
    }

    // ==================== Getter ====================
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isSplashDamage() { return splashDamage; }
    public Monster getTarget() { return target; }
    public String getTowerType() { return towerType; }
    public int getDamage() { return damage; }
}
