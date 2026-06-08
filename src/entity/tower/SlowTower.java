package entity.tower;

import entity.monster.Monster;
import entity.Projectile;

import javafx.scene.paint.Color;

/**
 * 减速塔 —— 低伤害 + 减速效果。
 * 平衡数值：伤害6，射速1.2秒，范围120像素，减速40%持续2秒
 */
public class SlowTower extends Tower {

    public SlowTower(int row, int col, int pixelX, int pixelY) {
        super(row, col, pixelX, pixelY);
        this.name = "减速塔";
        this.buyCost = 150;
        this.upgradeCosts = new int[]{60, 90};
        this.range = 2.0;     // 120像素 / 60像素每格
        this.fireRate = 1200; // 1.2秒
        this.damage = 6;
        this.color = Color.web("#6496ff");
    }

    @Override
    public String getType() {
        return "SLOW";
    }

    @Override
    public Projectile createProjectile(Monster target) {
        return new Projectile(pixelX, pixelY, target, damage, 4, false, 0, 0.4, Color.CYAN, getType());
    }
}
