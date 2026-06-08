package wave;

/**
 * 生成条目 —— 定义一种怪物的生成数量。
 */
public class SpawnEntry {
    private String type;
    private int count;

    public SpawnEntry(String type, int count) {
        this.type = type;
        this.count = count;
    }

    public String getType() { return type; }
    public int getCount() { return count; }
}
