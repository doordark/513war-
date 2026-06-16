package entity.tower;

import entity.monster.Monster;
import entity.Projectile;

import javafx.scene.paint.Color;

/**
 * 魔法塔 —— 中等速度，无视护甲。
 */
public class MagicTower extends Tower {

    public MagicTower(int row, int col, int pixelX, int pixelY) {
        super(row, col, pixelX, pixelY, "magic");
        this.name = "魔法塔";
        this.buyCost = 150; 
        this.upgradeCosts = new int[]{100, 150};
        this.range = 3.0;
        this.fireRate = 800;
        this.damage = 20;
        this.color = Color.web("#9664ff");
    }

    @Override
    public String getType() {
        return "MAGIC";
    }

    @Override
    public Projectile createProjectile(Monster target) {
        return new Projectile(pixelX, pixelY, target, damage, 4, false, 0, 0, Color.web("#b482ff"), getType());
    }
}
