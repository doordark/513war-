package entity.tower;

import entity.monster.Monster;
import entity.Projectile;

import javafx.scene.paint.Color;

/**
 * 箭塔 —— 高速低伤，单体攻击。
 * 平衡数值：伤害12，射速0.8秒，范围160像素
 */
public class ArrowTower extends Tower {

    public ArrowTower(int row, int col, int pixelX, int pixelY) {
        super(row, col, pixelX, pixelY, "arrow");
        this.name = "箭塔";
        this.buyCost = 50;
        this.upgradeCosts = new int[]{40, 60};
        this.range = 2.67;    // 160像素 / 60像素每格
        this.fireRate = 800;  // 0.8秒
        this.damage = 12;
        this.color = Color.web("#64c864");
    }

    @Override
    public String getType() {
        return "ARROW";
    }

    @Override
    public Projectile createProjectile(Monster target) {
        return new Projectile(pixelX, pixelY, target, damage, 5, false, 0, 0, Color.YELLOW, getType());
    }
}
