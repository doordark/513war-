package main;

/**
 * 游戏设置管理器 —— 管理音量、难度等全局设置。
 */
public class GameSettings {

    // 音量设置 (0.0 - 1.0)
    private double bgmVolume;
    private double sfxVolume;

    // 难度设置
    private Difficulty difficulty;

    public enum Difficulty {
        EASY("简单", 1.0, 0.8),
        NORMAL("普通", 1.0, 1.0),
        HARD("困难", 1.5, 1.2),
        NIGHTMARE("噩梦", 2.0, 1.5);

        private final String displayName;
        private final double monsterHpMultiplier;
        private final double monsterSpeedMultiplier;

        Difficulty(String displayName, double hpMult, double speedMult) {
            this.displayName = displayName;
            this.monsterHpMultiplier = hpMult;
            this.monsterSpeedMultiplier = speedMult;
        }

        public String getDisplayName() { return displayName; }
        public double getMonsterHpMultiplier() { return monsterHpMultiplier; }
        public double getMonsterSpeedMultiplier() { return monsterSpeedMultiplier; }
    }

    public GameSettings() {
        this.bgmVolume = 0.5;
        this.sfxVolume = 0.7;
        this.difficulty = Difficulty.NORMAL;
    }

    // Getters & Setters
    public double getBgmVolume() { return bgmVolume; }
    public void setBgmVolume(double v) { this.bgmVolume = Math.max(0, Math.min(1, v)); }

    public double getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(double v) { this.sfxVolume = Math.max(0, Math.min(1, v)); }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { this.difficulty = d; }
}
