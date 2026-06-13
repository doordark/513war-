package entity.tower;

import entity.monster.Monster;
import entity.Projectile;

import javafx.scene.paint.Color;

/**
 * 炮塔 —— 低速高伤，范围溅射。
 * 平衡数值：伤害35，射速2.0秒，范围130像素，爆炸半径50像素
 */
public class CannonTower extends Tower {

    public CannonTower(int row, int col, int pixelX, int pixelY) {
        super(row, col, pixelX, pixelY, "cannon");
        this.name = "炮塔";
        this.buyCost = 100;
        this.upgradeCosts = new int[]{80, 120};
        this.range = 2.17;    // 130像素 / 60像素每格
        this.fireRate = 2000; // 2.0秒
        this.damage = 35;
        this.color = Color.web("#c86432");
    }

    @Override
    public String getType() {
        return "CANNON";
    }

    @Override
    public Projectile createProjectile(Monster target) {
        return new Projectile(pixelX, pixelY, target, damage, 3, true, 50, 0, Color.ORANGE, getType());
    }
}
