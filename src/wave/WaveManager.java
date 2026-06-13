package wave;

import entity.monster.*;
import main.GameSettings;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 波次管理器 —— 控制怪物生成节奏，支持常规模式与无尽模式。
 *
 * 常规模式：按预设波次表生成怪物，波数有限。
 * 无尽模式：动态生成怪物阵容，波数无限，属性阶梯式增长。
 */
public class WaveManager {

    private int currentWave;
    private int totalWaves;           // 常规模式下有效，无尽模式下为 Integer.MAX_VALUE
    private boolean isEndless;        // 是否为无尽模式
    private List<Wave> waves;
    private long waveStartTime;
    private int spawnedInWave;
    private boolean waveActive;
    private boolean allWavesComplete;
    private boolean nextWaveRequested;

    // 难度倍率
    private double hpMultiplier;
    private double speedMultiplier;
    private GameSettings.Difficulty difficulty;

    // 无尽模式：当前波次的怪物类型序列（动态生成）
    private List<String> endlessWaveTypes;
    private static final Random RNG = new Random();

    // ==================== 构造器 ====================

    /**
     * 常规模式构造器（保持向后兼容）。
     */
    public WaveManager(int level, GameSettings.Difficulty difficulty) {
        this(level, difficulty, false);
    }

    /**
     * 统一构造器。
     * @param level      关卡编号（常规模式用）
     * @param difficulty 难度
     * @param isEndless  是否无尽模式
     */
    public WaveManager(int level, GameSettings.Difficulty difficulty, boolean isEndless) {
        this.currentWave = 0;
        this.isEndless = isEndless;
        this.difficulty = difficulty;
        this.waves = new ArrayList<>();
        this.waveActive = false;
        this.allWavesComplete = false;
        this.nextWaveRequested = false;
        this.spawnedInWave = 0;
        this.endlessWaveTypes = new ArrayList<>();

        this.hpMultiplier = difficulty.getMonsterHpMultiplier();
        this.speedMultiplier = difficulty.getMonsterSpeedMultiplier();

        if (isEndless) {
            this.totalWaves = Integer.MAX_VALUE;
            System.out.println("[波次初始化] 无尽模式 难度=" + difficulty.getDisplayName()
                + " HP倍率=" + hpMultiplier + " 速度倍率=" + speedMultiplier);
        } else {
            generateWaves();
            System.out.println("[波次初始化] 总波次=" + totalWaves
                + " 难度=" + difficulty.getDisplayName()
                + " HP倍率=" + hpMultiplier + " 速度倍率=" + speedMultiplier);
        }
    }

    /**
     * 根据关卡生成波次数据（常规模式）。
     * 根据难度调整波次数和怪物组合：
     *   EASY:      5 波，少量普通怪
     *   NORMAL:    7 波，普通 + 快速
     *   HARD:      10 波，普通 + 快速 + 坦克
     *   NIGHTMARE: 15 波，全类型 + Boss
     */
    private void generateWaves() {
        // 根据难度决定总波数
        int baseWaves;
        switch (difficulty) {
            case EASY:      baseWaves = 5; break;
            case HARD:      baseWaves = 10; break;
            case NIGHTMARE: baseWaves = 15; break;
            default:        baseWaves = 7; break; // NORMAL
        }
        totalWaves = baseWaves;

        for (int i = 0; i < totalWaves; i++) {
            Wave wave = new Wave();
            int waveNum = i + 1;

            if (waveNum == 1) {
                // 第一波：少量普通怪
                wave.addEntry("NORMAL", 3 + (difficulty == GameSettings.Difficulty.EASY ? 0 : 2));
            } else {
                // 根据难度和波数生成怪物组合
                int normalCount = 5 + (waveNum - 1) * 3;
                int fastCount = 0;
                int tankCount = 0;
                int bossCount = 0;

                if (difficulty == GameSettings.Difficulty.EASY) {
                    // 简单：只有普通怪，少量快速
                    fastCount = Math.max(0, (waveNum - 3) * 1);
                } else if (difficulty == GameSettings.Difficulty.NORMAL) {
                    // 普通：普通 + 快速
                    fastCount = Math.max(0, (waveNum - 2) * 2);
                } else if (difficulty == GameSettings.Difficulty.HARD) {
                    // 困难：普通 + 快速 + 坦克
                    fastCount = Math.max(0, (waveNum - 2) * 2);
                    tankCount = Math.max(0, waveNum - 4);
                } else {
                    // 噩梦：全类型 + Boss
                    fastCount = Math.max(0, (waveNum - 2) * 3);
                    tankCount = Math.max(0, waveNum - 3);
                    bossCount = (waveNum == totalWaves) ? 2 : (waveNum % 5 == 0 ? 1 : 0);
                }

                wave.addEntry("NORMAL", normalCount);
                if (fastCount > 0) wave.addEntry("FAST", fastCount);
                if (tankCount > 0) wave.addEntry("TANK", tankCount);
                if (bossCount > 0) wave.addEntry("BOSS", bossCount);
            }

            waves.add(wave);
            System.out.println("[波次配置] 波次" + waveNum + ": " + wave.getSummary()
                + " 总计=" + wave.getTotalCount());
        }
    }

    // ==================== 每帧更新 ====================

    /**
     * 每帧更新：检查是否需要生成怪物。
     */
    public void update(List<Monster> monsters, Point2D startPoint) {
        if (allWavesComplete) return;

        // 波次进行中：按间隔生成怪物
        if (waveActive) {
            int totalForWave = isEndless ? getEndlessWaveCount() : waves.get(currentWave - 1).getTotalCount();
            if (spawnedInWave < totalForWave) {
                long elapsed = System.currentTimeMillis() - waveStartTime;
                long spawnInterval = 1500;

                if (elapsed >= spawnedInWave * spawnInterval) {
                    spawnMonster(monsters, startPoint);
                }
            } else {
                waveActive = false;
                System.out.println("[波次生成完成] 第" + currentWave + "波已全部生成，等待怪物清理...");
            }
            return;
        }

        // 波次未进行中：检查是否全部消灭
        if (currentWave > 0 && monsters.isEmpty()) {
            System.out.println("[波次清理完成] 第" + currentWave + "波怪物已全部消灭！");

            if (!isEndless && currentWave >= totalWaves) {
                allWavesComplete = true;
                System.out.println("[所有波次完成] 游戏胜利！");
                return;
            }

            // 无尽模式永远继续
            if (nextWaveRequested) {
                nextWaveRequested = false;
                startNextWave(monsters, startPoint);
            }
        } else if (currentWave == 0) {
            if (nextWaveRequested) {
                nextWaveRequested = false;
                startNextWave(monsters, startPoint);
            }
        }
    }

    // ==================== 波次控制 ====================

    /** 玩家请求开始下一波。 */
    public void requestNextWave() {
        if (allWavesComplete) return;
        if (waveActive) {
            System.out.println("[下一波] 当前波次正在进行中，无法开始下一波！");
            return;
        }
        nextWaveRequested = true;
        System.out.println("[下一波] 已请求开始第" + (currentWave + 1) + "波");
    }

    private void startNextWave(List<Monster> monsters, Point2D startPoint) {
        if (!isEndless && currentWave >= totalWaves) {
            allWavesComplete = true;
            System.out.println("[所有波次完成] 游戏胜利！");
            return;
        }

        currentWave++;
        spawnedInWave = 0;
        waveActive = true;
        waveStartTime = System.currentTimeMillis();

        int total = isEndless ? getEndlessWaveCount() : waves.get(currentWave - 1).getTotalCount();
        System.out.println("========== [开始波次] 第" + currentWave
            + (isEndless ? "" : "/" + totalWaves) + "波 ["
            + (isEndless ? "无尽" : "常规") + "] ==========");
        System.out.println("[波次详情] 怪物总数=" + total + " 生成间隔=1500ms");
    }

    // ==================== 怪物生成 ====================

    private void spawnMonster(List<Monster> monsters, Point2D startPoint) {
        if (isEndless) {
            spawnEndlessMonster(monsters, startPoint);
        } else {
            spawnNormalMonster(monsters, startPoint);
        }
    }

    /** 常规模式：从预设 Wave 中取怪物类型。 */
    private void spawnNormalMonster(List<Monster> monsters, Point2D startPoint) {
        Wave wave = waves.get(currentWave - 1);
        SpawnEntry entry = wave.getEntry(spawnedInWave);
        if (entry == null) return;

        Monster m = createMonsterByType(entry.getType(), startPoint.getX(), startPoint.getY());
        if (m == null) return;

        m.setMaxHp((int) (m.getMaxHp() * hpMultiplier));
        m.setCurrentHp(m.getMaxHp());
        m.setSpeed(m.getSpeed() * speedMultiplier);
        applyWaveScaling(m, currentWave);

        monsters.add(m);
        spawnedInWave++;
        System.out.println("[怪物生成] 波次" + currentWave + " 第" + spawnedInWave + "/" + wave.getTotalCount()
            + " 个: " + entry.getType() + " HP=" + m.getMaxHp() + " 速度=" + String.format("%.2f", m.getSpeed()));
    }

    /** 无尽模式：动态决定怪物类型并生成。 */
    private void spawnEndlessMonster(List<Monster> monsters, Point2D startPoint) {
        String type = getEndlessMonsterType();
        Monster m = createMonsterByType(type, startPoint.getX(), startPoint.getY());
        if (m == null) return;

        // 应用难度倍率
        m.setMaxHp((int) (m.getMaxHp() * hpMultiplier));
        m.setCurrentHp(m.getMaxHp());
        m.setSpeed(m.getSpeed() * speedMultiplier);

        // 应用无尽模式成长公式
        applyEndlessScaling(m, currentWave);

        monsters.add(m);
        spawnedInWave++;
        System.out.println("[怪物生成(无尽)] 波次" + currentWave + " 第" + spawnedInWave + "/" + getEndlessWaveCount()
            + " 个: " + type + " HP=" + m.getMaxHp() + " 速度=" + String.format("%.2f", m.getSpeed())
            + " 金币=" + m.getRewardGold());
    }

    // ==================== 无尽模式怪物组合算法 ====================

    /**
     * 获取无尽模式下当前波次怪物总数。
     * Count = 5 + (Wave - 1) * 2
     */
    private int getEndlessWaveCount() {
        return 5 + (currentWave - 1) * 2;
    }

    /**
     * 无尽模式怪物类型决策。
     *
     * 第 1 - 5 波：   100% NORMAL
     * 第 6 - 10 波：  70% NORMAL, 30% FAST
     * 第 11 - 20 波： 50% NORMAL, 50% FAST，每 5 波最后 2 只为 BOSS
     * 第 21 波以后：  NORMAL / FAST / TANK 各 1/3
     */
    private String getEndlessMonsterType() {
        int wave = currentWave;
        int total = getEndlessWaveCount();
        int remaining = total - spawnedInWave; // 还需生成多少只

        // 每 5 波的 Boss：第 11-20 波，每 5 波的最后一两只替换为 Boss
        if (wave >= 11 && wave <= 20 && remaining <= 2) {
            return "BOSS";
        }

        double roll = RNG.nextDouble();
        if (wave <= 5) {
            return "NORMAL";
        } else if (wave <= 10) {
            return roll < 0.30 ? "FAST" : "NORMAL";
        } else if (wave <= 20) {
            return roll < 0.50 ? "FAST" : "NORMAL";
        } else {
            // 21波以后：各 1/3
            if (roll < 0.333) return "NORMAL";
            else if (roll < 0.666) return "FAST";
            else return "TANK";
        }
    }

    // ==================== 属性成长 ====================

    /**
     * 常规模式波次成长。
     */
    private void applyWaveScaling(Monster monster, int wave) {
        if (wave <= 1) return;

        double hpMult = 1.0 + (wave - 1) * 0.25;
        int scaledHp = (int) (monster.getMaxHp() * hpMult);
        monster.setMaxHp(scaledHp);
        monster.setCurrentHp(scaledHp);

        int speedIncrements = (wave - 1) / 5;
        double speedMult = Math.min(1.0 + speedIncrements * 0.1, 1.5);
        monster.setSpeed(monster.getSpeed() * speedMult);

        double goldMult = 1.0 + (wave - 1) * 0.1;
        monster.setGoldReward((int) (monster.getRewardGold() * goldMult));
        monster.setXpReward((int) (monster.getXpReward() * goldMult));
    }

    /**
     * 无尽模式属性成长。
     *
     * HP：HP = HP_initial * (1 + (Wave - 1) * 0.3)
     * 速度：每 3 波 +5%，上限 1.6 倍
     * 金币衰减：Gold = max(3, round(Gold_initial * (1 - (Wave - 1) * 0.02)))
     * Boss 特殊：血量为普通怪的 5 倍
     */
    private void applyEndlessScaling(Monster monster, int wave) {
        if (wave <= 1) return;

        // 血量成长
        double hpMult = 1.0 + (wave - 1) * 0.3;
        // Boss 额外 5 倍血量
        if (monster instanceof BossMonster) {
            hpMult *= 5.0;
        }
        int scaledHp = (int) (monster.getMaxHp() * hpMult);
        monster.setMaxHp(scaledHp);
        monster.setCurrentHp(scaledHp);

        // 速度成长：每 3 波 +5%，上限 1.6 倍
        int speedIncrements = (wave - 1) / 3;
        double speedMult = Math.min(1.0 + speedIncrements * 0.05, 1.6);
        monster.setSpeed(monster.getSpeed() * speedMult);

        // 金币衰减：每波 -2%，最低 3 金币
        double goldMult = 1.0 - (wave - 1) * 0.02;
        int scaledGold = (int) Math.round(monster.getRewardGold() * Math.max(0, goldMult));
        monster.setGoldReward(Math.max(3, scaledGold));
        // 经验同比例
        int scaledXp = (int) Math.round(monster.getXpReward() * Math.max(0, goldMult));
        monster.setXpReward(Math.max(3, scaledXp));
    }

    // ==================== 工厂方法 ====================

    private Monster createMonsterByType(String type, double x, double y) {
        switch (type) {
            case "NORMAL": return new NormalMonster(x, y);
            case "FAST":   return new FastMonster(x, y);
            case "TANK":   return new TankMonster(x, y);
            case "BOSS":   return new BossMonster(x, y);
            default:       return new NormalMonster(x, y);
        }
    }

    // ==================== Getters ====================

    public boolean isWaveActive() { return waveActive; }
    public boolean isAllComplete() { return allWavesComplete; }
    public int getCurrentWave() { return currentWave; }
    public int getTotalWaves() { return totalWaves; }
    public boolean isEndless() { return isEndless; }
    public boolean isNextWaveReady() { return !waveActive && (isEndless || currentWave < totalWaves) && (currentWave == 0 || monsterListEmpty); }

    private boolean monsterListEmpty = true;
    public void setMonsterListEmpty(boolean empty) { this.monsterListEmpty = empty; }
}