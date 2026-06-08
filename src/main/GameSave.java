package main;

import entity.tower.Tower;
import entity.monster.Monster;
import entity.Projectile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏存档管理器 —— 保存/加载游戏进度。
 */
public class GameSave {

    private static final String SAVE_FILE = "save.dat";

    public static class SaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        public int level;
        public int playerGold;
        public int playerLives;
        public int score;
        public int currentWave;
        public int totalWaves;
        public String difficulty;

        // 塔数据
        public List<TowerSave> towers = new ArrayList<>();

        // 怪物数据
        public List<MonsterSave> monsters = new ArrayList<>();

        public SaveData() {}
    }

    public static class TowerSave implements Serializable {
        private static final long serialVersionUID = 1L;
        public String type;
        public int row, col;
        public int level;
        public int damage;
        public double range;
    }

    public static class MonsterSave implements Serializable {
        private static final long serialVersionUID = 1L;
        public String type;
        public double x, y;
        public int currentHp, maxHp;
        public int waypointIndex;
        public double slowFactor;
        public long slowEndTime;
    }

    /**
     * 保存游戏进度。
     */
    public static void save(GamePanel gamePanel) {
        try {
            SaveData data = new SaveData();

            data.level = 1;
            data.playerGold = gamePanel.getPlayerGold();
            data.playerLives = gamePanel.getPlayerLives();
            data.score = gamePanel.getScore();
            data.currentWave = gamePanel.getCurrentWave();
            data.totalWaves = gamePanel.getTotalWaves();
            data.difficulty = gamePanel.getSettings().getDifficulty().name();

            // 保存塔
            for (Tower t : gamePanel.getTowerList()) {
                TowerSave ts = new TowerSave();
                ts.type = t.getType();
                ts.row = t.getRow();
                ts.col = t.getCol();
                ts.level = t.getLevel();
                ts.damage = t.getDamage();
                ts.range = t.getRange();
                data.towers.add(ts);
            }

            // 保存怪物
            for (Monster m : gamePanel.getMonsterList()) {
                if (!m.isAlive()) continue;
                MonsterSave ms = new MonsterSave();
                ms.type = m.getType();
                ms.x = m.getX();
                ms.y = m.getY();
                ms.currentHp = m.getCurrentHp();
                ms.maxHp = m.getMaxHp();
                ms.waypointIndex = m.getWaypointIndex();
                ms.slowFactor = m.getSlowFactor();
                ms.slowEndTime = m.getSlowEndTime();
                data.monsters.add(ms);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
                oos.writeObject(data);
            }

            System.out.println("[存档] 游戏进度已保存");
        } catch (Exception e) {
            System.err.println("[存档] 保存失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否有存档。
     */
    public static boolean hasSave() {
        return new File(SAVE_FILE).exists();
    }

    /**
     * 加载游戏进度。
     */
    public static SaveData load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            SaveData data = (SaveData) ois.readObject();
            System.out.println("[读档] 游戏进度已加载 - 波次:" + data.currentWave + "/" + data.totalWaves
                + " 金币:" + data.playerGold + " 生命:" + data.playerLives);
            return data;
        } catch (Exception e) {
            System.err.println("[读档] 加载失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除存档。
     */
    public static void deleteSave() {
        File f = new File(SAVE_FILE);
        if (f.exists()) {
            f.delete();
            System.out.println("[存档] 存档已删除");
        }
    }
}
