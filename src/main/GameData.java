package main;

/**
 * 全局游戏数据单例 — 所有界面共享同一份数据。
 * 主菜单、战场、商城、排行榜都从这里读写，确保数据完全同步。
 */
public class GameData {
    // 核心属性
    public static int hp = 20;
    public static int exp = 0;
    public static int level = 1;
    public static int gold = 150;
    public static int score = 0;
    public static int currentWave = 0;
    public static int maxWave = 7;
    public static boolean isEndless = false;

    /** 重置为初始状态（新游戏/重新开始） */
    public static void reset() {
        resetToDefault();
    }

    /** 新游戏彻底清空数据的标准 */
    public static void resetToDefault() {
        hp = 20;
        exp = 0;
        level = 1;
        gold = 150;
        score = 0;
        currentWave = 0;
        maxWave = 7;
        isEndless = false;
        System.out.println("[GameData] 已彻底重置，新游戏数据就绪！");
    }

    /** 获取波次显示文本 */
    public static String getWaveText() {
        if (isEndless) {
            return "无尽 " + currentWave;
        }
        return currentWave + "/" + maxWave;
    }
}
