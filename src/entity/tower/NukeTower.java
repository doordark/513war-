package entity.tower;

import entity.monster.Monster;
import entity.Projectile;

import javafx.scene.paint.Color;

/**
 * 终极核弹塔 —— 超高伤害，超长冷却。
 * 平衡数值：伤害180，射速4.0秒，范围280像素
 */
public class NukeTower extends Tower {

    public NukeTower(int row, int col, int pixelX, int pixelY) {
        super(row, col, pixelX, pixelY);
        this.name = "核弹塔";
        this.buyCost = 500;
        this.upgradeCosts = new int[]{400, 600};
        this.range = 4.67;    // 280像素 / 60像素每格
        this.fireRate = 4000; // 4.0秒
        this.damage = 180;
        this.color = Color.web("#ff4444");
    }

    @Override
    public String getType() {
        return "NUKE";
    }

    @Override
    public Projectile createProjectile(Monster target) {
        return new Projectile(pixelX, pixelY, target, damage, 4, true, 80, 0, Color.RED, getType());
    }
}
