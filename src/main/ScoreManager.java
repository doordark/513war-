package main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 分数管理器 —— 负责排行榜数据的读写与持久化。
 * 使用 JSON 格式保存到本地文件。
 */
public class ScoreManager {

    private static final String SCORES_FILE = "scores.json";
    private static final int MAX_SCORES = 10; // 最多保存 10 条记录

    private List<ScoreEntry> scores;

    public ScoreManager() {
        this.scores = new ArrayList<>();
        loadScores();
    }

    /**
     * 从本地文件加载排行榜数据。
     */
    public void loadScores() {
        scores.clear();
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            System.out.println("[排行榜] 未找到存档文件，创建新排行榜");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                ScoreEntry entry = parseJsonLine(line);
                if (entry != null) {
                    scores.add(entry);
                }
            }
            // 按分数降序排序
            scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            System.out.println("[排行榜] 加载 " + scores.size() + " 条记录");
        } catch (IOException e) {
            System.err.println("[排行榜] 加载失败: " + e.getMessage());
        }
    }

    /**
     * 保存排行榜数据到本地文件。
     */
    public void saveScores() {
        // 只保留前 MAX_SCORES 条
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(SCORES_FILE), StandardCharsets.UTF_8))) {
            for (ScoreEntry entry : scores) {
                writer.write(toJsonLine(entry));
                writer.newLine();
            }
            System.out.println("[排行榜] 保存 " + scores.size() + " 条记录");
        } catch (IOException e) {
            System.err.println("[排行榜] 保存失败: " + e.getMessage());
        }
    }

    /**
     * 尝试添加新分数。如果进入前 MAX_SCORES 名，返回 true。
     */
    public boolean tryAddScore(String playerName, int score, int wave, String difficulty, boolean isEndless) {
        String mode = isEndless ? "endless" : "normal";
        ScoreEntry newEntry = new ScoreEntry(playerName, score, wave, difficulty, mode);
        scores.add(newEntry);
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        // 检查是否在前 MAX_SCORES 名
        boolean inTop = scores.indexOf(newEntry) < MAX_SCORES;
        if (!inTop) {
            scores.remove(newEntry);
        } else {
            // 截断到 MAX_SCORES
            if (scores.size() > MAX_SCORES) {
                scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
            }
            saveScores();
        }
        return inTop;
    }

    /**
     * 获取排行榜数据（只读副本）。
     */
    public List<ScoreEntry> getTopScores() {
        return new ArrayList<>(scores);
    }

    /**
     * 获取排行榜大小。
     */
    public int getScoreCount() {
        return scores.size();
    }

    // ==================== JSON 序列化 ====================

    private String toJsonLine(ScoreEntry entry) {
        return String.format("{\"name\":\"%s\",\"score\":%d,\"wave\":%d,\"difficulty\":\"%s\",\"mode\":\"%s\"}",
            escapeJson(entry.getName()), entry.getScore(), entry.getWave(),
            escapeJson(entry.getDifficulty()), escapeJson(entry.getMode()));
    }

    private ScoreEntry parseJsonLine(String line) {
        try {
            String name = extractJsonValue(line, "name");
            int score = Integer.parseInt(extractJsonValue(line, "score"));
            int wave = Integer.parseInt(extractJsonValue(line, "wave"));
            String difficulty = extractJsonValue(line, "difficulty");
            String mode = extractJsonValue(line, "mode");
            if (mode.isEmpty()) mode = "normal"; // 兼容旧格式
            return new ScoreEntry(name, score, wave, difficulty, mode);
        } catch (Exception e) {
            System.err.println("[排行榜] 解析失败: " + line);
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();

        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end > start ? json.substring(start, end) : "";
        } else {
            int end = json.indexOf(',', start);
            if (end < 0) end = json.indexOf('}', start);
            return end > start ? json.substring(start, end).trim() : "";
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
