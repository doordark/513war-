package wave;

import java.util.ArrayList;
import java.util.List;

/**
 * 波次数据 —— 定义一波怪物的组成。
 */
public class Wave {

    private List<SpawnEntry> entries;

    public Wave() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(String type, int count) {
        entries.add(new SpawnEntry(type, count));
    }

    public int getTotalCount() {
        int total = 0;
        for (SpawnEntry e : entries) {
            total += e.getCount();
        }
        return total;
    }

    /**
     * 根据已生成数量获取对应的怪物类型。
     */
    public SpawnEntry getEntry(int spawnedCount) {
        int count = 0;
        for (SpawnEntry e : entries) {
            if (spawnedCount < count + e.getCount()) {
                return e;
            }
            count += e.getCount();
        }
        return null;
    }

    /**
     * 获取波次摘要字符串。
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        for (SpawnEntry e : entries) {
            if (sb.length() > 0) sb.append(" + ");
            sb.append(e.getType()).append("×").append(e.getCount());
        }
        return sb.toString();
    }
}
