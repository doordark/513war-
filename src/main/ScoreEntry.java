package main;

/**
 * 分数记录 —— 存储单条排行榜数据。
 */
public class ScoreEntry {

    private final String name;
    private final int score;
    private final int wave;
    private final String difficulty;

    public ScoreEntry(String name, int score, int wave, String difficulty) {
        this.name = name;
        this.score = score;
        this.wave = wave;
        this.difficulty = difficulty;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public int getWave() { return wave; }
    public String getDifficulty() { return difficulty; }
}
