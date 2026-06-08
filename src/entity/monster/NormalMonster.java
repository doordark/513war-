package entity.monster;

import javafx.scene.paint.Color;

/**
 * 普通怪物 —— 血量和速度均衡。
 */
public class NormalMonster extends Monster {

    public NormalMonster(double x, double y) {
        super(x, y, 50, 1.2, 0, 10, 15, 1, Color.RED);
        this.name = "普通怪";
    }

    @Override
    public String getType() {
        return "NORMAL";
    }
}
