package entity.monster;

import javafx.scene.paint.Color;

/**
 * 快速怪物 —— 低血量高速度。
 */
public class FastMonster extends Monster {

    public FastMonster(double x, double y) {
        super(x, y, 25, 2.4, 0, 8, 20, 1, Color.YELLOW);
        this.name = "速跑兵";
    }

    @Override
    public String getType() {
        return "FAST";
    }
}
