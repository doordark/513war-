package entity.tower;

import entity.monster.Monster;
import entity.Projectile;

import javafx.scene.paint.Color;

/**
 * 连锁闪电塔 —— 中速中伤，可弹跳多个目标。
 * 平衡数值：伤害24，射速1.0秒，范围180像素，闪电链最多弹跳3个目标
 */
public class LightningTower extends Tower {

    public LightningTower(int row, int col, int pixelX, int pixelY) {
        super(row, col, pixelX, pixelY, "lightning");
        this.name = "闪电塔";
        this.buyCost = 260;
        this.upgradeCosts = new int[]{200, 300};
        this.range = 3.0;     // 180像素 / 60像素每格
        this.fireRate = 1000; // 1.0秒
        this.damage = 24;
        this.color = Color.web("#9664ff");
    }

    @Override
    public String getType() {
        return "LIGHTNING";
    }

    @Override
    public Projectile createProjectile(Monster target) {
        return new Projectile(pixelX, pixelY, target, damage, 6, false, 0, 0, Color.web("#9664ff"), getType());
    }
}
