package main;

/**
 * 分数记录 —— 存储单条排行榜数据。
 */
public class ScoreEntry {

    private final String name;
    private final int score;
    private final int wave;
    private final String difficulty;
    private final String mode;       // "endless" 或 "normal"

    public ScoreEntry(String name, int score, int wave, String difficulty, String mode) {
        this.name = name;
        this.score = score;
        this.wave = wave;
        this.difficulty = difficulty;
        this.mode = mode;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public int getWave() { return wave; }
    public String getDifficulty() { return difficulty; }
    public String getMode() { return mode; }
    public boolean isEndless() { return "endless".equals(mode); }
}