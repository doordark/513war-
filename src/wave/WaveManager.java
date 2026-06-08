package wave;

import entity.monster.*;
import main.GameSettings;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * 波次管理器 —— 控制怪物生成节奏。
 * 第一波：5 只基础僵尸，间隔 1.5 秒。
 * 后续波次按数值表递增，玩家手动点击"下一波"按钮触发。
 */
public class WaveManager {

    private int currentWave;
    private int totalWaves;
    private List<Wave> waves;
    private long waveStartTime;
    private int spawnedInWave;
    private boolean waveActive;
    private boolean allWavesComplete;
    private boolean nextWaveRequested;

    // 难度倍率
    private double hpMultiplier;
    private double speedMultiplier;

    public WaveManager(int level, GameSettings.Difficulty difficulty) {
        this.currentWave = 0;
        this.totalWaves = 5 + level * 2;
        this.waves = new ArrayList<>();
        this.waveActive = false;
        this.allWavesComplete = false;
        this.nextWaveRequested = false;
        this.spawnedInWave = 0;

        // 应用难度倍率
        this.hpMultiplier = difficulty.getMonsterHpMultiplier();
        this.speedMultiplier = difficulty.getMonsterSpeedMultiplier();

        generateWaves();
        System.out.println("[波次初始化] 总波次=" + totalWaves + " 关卡=" + level
            + " 难度=" + difficulty.getDisplayName()
            + " HP倍率=" + hpMultiplier + " 速度倍率=" + speedMultiplier);
    }

    /**
     * 根据关卡生成波次数据。
     * 第一波：5 只基础僵尸。
     * 后续波次按数值递增。
     */
    private void generateWaves() {
        for (int i = 0; i < totalWaves; i++) {
            Wave wave = new Wave();
            int waveNum = i + 1;

            if (waveNum == 1) {
                // 第一波：5 只基础僵尸
                wave.addEntry("NORMAL", 5);
            } else {
                int normalCount = 5 + (waveNum - 1) * 3;
                int fastCount = Math.max(0, (waveNum - 2) * 2);
                int tankCount = Math.max(0, waveNum - 4);
                int bossCount = (waveNum == totalWaves) ? 1 : 0;

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

    /**
     * 每帧更新：检查是否需要生成怪物。
     */
    public void update(List<Monster> monsters, Point2D startPoint) {
        if (allWavesComplete) return;

        // 波次进行中：按间隔生成怪物
        if (waveActive) {
            Wave wave = waves.get(currentWave - 1);
            if (spawnedInWave < wave.getTotalCount()) {
                long elapsed = System.currentTimeMillis() - waveStartTime;
                long spawnInterval = 1500; // 1.5 秒间隔

                if (elapsed >= spawnedInWave * spawnInterval) {
                    spawnMonster(monsters, startPoint, wave);
                }
            } else {
                // 本波怪物全部生成完毕，等待全部消灭
                waveActive = false;
                System.out.println("[波次生成完成] 第" + currentWave + "波已全部生成，等待怪物清理...");
            }
            return;
        }

        // 波次未进行中：检查是否全部消灭
        if (currentWave > 0 && monsters.isEmpty()) {
            System.out.println("[波次清理完成] 第" + currentWave + "波怪物已全部消灭！");

            if (currentWave >= totalWaves) {
                allWavesComplete = true;
                System.out.println("[所有波次完成] 游戏胜利！");
                return;
            }

            // 等待玩家点击"下一波"按钮
            if (nextWaveRequested) {
                nextWaveRequested = false;
                startNextWave(monsters, startPoint);
            }
        } else if (currentWave == 0) {
            // 游戏刚开始，等待玩家点击"开始第一波"
            if (nextWaveRequested) {
                nextWaveRequested = false;
                startNextWave(monsters, startPoint);
            }
        }
    }

    /**
     * 玩家请求开始下一波。
     */
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
        if (currentWave >= totalWaves) {
            allWavesComplete = true;
            System.out.println("[所有波次完成] 游戏胜利！");
            return;
        }

        currentWave++;
        spawnedInWave = 0;
        waveActive = true;
        waveStartTime = System.currentTimeMillis();

        Wave wave = waves.get(currentWave - 1);
        System.out.println("========== [开始波次] 第" + currentWave + "/" + totalWaves + "波 ==========");
        System.out.println("[波次详情] 怪物总数=" + wave.getTotalCount() + " 生成间隔=1500ms");
    }

    private void spawnMonster(List<Monster> monsters, Point2D startPoint, Wave wave) {
        SpawnEntry entry = wave.getEntry(spawnedInWave);
        if (entry == null) return;

        Monster m;
        switch (entry.getType()) {
            case "NORMAL":
                m = new NormalMonster(startPoint.getX(), startPoint.getY());
                break;
            case "FAST":
                m = new FastMonster(startPoint.getX(), startPoint.getY());
                break;
            case "TANK":
                m = new TankMonster(startPoint.getX(), startPoint.getY());
                break;
            case "BOSS":
                m = new BossMonster(startPoint.getX(), startPoint.getY());
                break;
            default:
                m = new NormalMonster(startPoint.getX(), startPoint.getY());
                break;
        }

        // 应用难度倍率
        m.setMaxHp((int) (m.getMaxHp() * hpMultiplier));
        m.setCurrentHp(m.getMaxHp());
        m.setSpeed(m.getSpeed() * speedMultiplier);

        // 应用波次成长公式
        applyWaveScaling(m, currentWave);

        monsters.add(m);
        spawnedInWave++;
        System.out.println("[怪物生成] 波次" + currentWave + " 第" + spawnedInWave + "/" + wave.getTotalCount()
            + " 个: " + entry.getType() + " HP=" + m.getMaxHp() + " 速度=" + String.format("%.2f", m.getSpeed()));
    }

    /**
     * 应用波次成长公式：
     * - 血量：HP_current = HP_initial * (1 + (Wave - 1) * 0.25)
     * - 速度：每 5 波提升 10%，封顶 1.5 倍
     * - 金币：Gold_current = Gold_initial * (1 + (Wave - 1) * 0.1)
     */
    private void applyWaveScaling(Monster monster, int wave) {
        if (wave <= 1) return; // 第一波不应用成长

        // 血量成长：每波 +25%
        double hpMultiplier = 1.0 + (wave - 1) * 0.25;
        int scaledHp = (int) (monster.getMaxHp() * hpMultiplier);
        monster.setMaxHp(scaledHp);
        monster.setCurrentHp(scaledHp);

        // 速度成长：每 5 波 +10%，封顶 1.5 倍
        int speedIncrements = (wave - 1) / 5;
        double speedMultiplier = Math.min(1.0 + speedIncrements * 0.1, 1.5);
        monster.setSpeed(monster.getSpeed() * speedMultiplier);

        // 金币奖励成长：每波 +10%
        double goldMultiplier = 1.0 + (wave - 1) * 0.1;
        monster.setGoldReward((int) (monster.getRewardGold() * goldMultiplier));
        monster.setXpReward((int) (monster.getXpReward() * goldMultiplier));
    }

    public boolean isWaveActive() { return waveActive; }
    public boolean isAllComplete() { return allWavesComplete; }
    public int getCurrentWave() { return currentWave; }
    public int getTotalWaves() { return totalWaves; }
    public boolean isNextWaveReady() { return !waveActive && currentWave < totalWaves && (currentWave == 0 || monsterListEmpty); }

    // 用于判断是否可以开始下一波
    private boolean monsterListEmpty = true;
    public void setMonsterListEmpty(boolean empty) { this.monsterListEmpty = empty; }
}
