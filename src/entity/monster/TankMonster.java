package entity.monster;

import javafx.scene.paint.Color;

/**
 * 坦克怪物 —— 高血量高护甲低速。
 */
public class TankMonster extends Monster {

    public TankMonster(double x, double y) {
        super(x, y, 150, 0.8, 5, 20, 30, 2, Color.DARKGRAY, "tank");
        this.name = "坦克怪";
    }

    @Override
    public String getType() {
        return "TANK";
    }
}
