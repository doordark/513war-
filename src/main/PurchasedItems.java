package main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 已购道具管理器 —— 持久化商城购买的道具数据。
 * 使用 JSON 格式保存到本地文件。
 */
public class PurchasedItems {

    private static final String FILE_NAME = "purchased_items.json";

    private Set<Integer> purchasedIndices;

    public PurchasedItems() {
        this.purchasedIndices = new HashSet<>();
        load();
    }

    /**
     * 从本地文件加载已购道具。
     */
    public void load() {
        purchasedIndices.clear();

        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("[已购道具] 未找到存档，使用默认配置");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                // 解析 JSON 数组: [0,1,2,3,4]
                line = line.trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    String content = line.substring(1, line.length() - 1);
                    if (!content.isEmpty()) {
                        String[] parts = content.split(",");
                        for (String part : parts) {
                            try {
                                int idx = Integer.parseInt(part.trim());
                                purchasedIndices.add(idx);
                            } catch (NumberFormatException e) {
                                // 忽略无效数据
                            }
                        }
                    }
                }
            }
            System.out.println("[已购道具] 加载 " + purchasedIndices.size() + " 个已购道具");
        } catch (IOException e) {
            System.err.println("[已购道具] 加载失败: " + e.getMessage());
        }
    }

    /**
     * 保存已购道具到本地文件。
     */
    public void save() {
        List<Integer> sorted = new ArrayList<>(purchasedIndices);
        sorted.sort(Integer::compareTo);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {
            writer.write("[");
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) writer.write(",");
                writer.write(String.valueOf(sorted.get(i)));
            }
            writer.write("]");
            System.out.println("[已购道具] 保存 " + sorted.size() + " 个已购道具");
        } catch (IOException e) {
            System.err.println("[已购道具] 保存失败: " + e.getMessage());
        }
    }

    /**
     * 添加已购道具索引。
     */
    public void add(int index) {
        if (purchasedIndices.add(index)) {
            save();
        }
    }

    /**
     * 检查道具是否已购买。
     */
    public boolean isPurchased(int index) {
        return purchasedIndices.contains(index);
    }

    /**
     * 获取所有已购道具索引列表（排序后）。
     */
    public List<Integer> getAll() {
        List<Integer> list = new ArrayList<>(purchasedIndices);
        list.sort(Integer::compareTo);
        return list;
    }

    /**
     * 获取已购道具数量。
     */
    public int getCount() {
        return purchasedIndices.size();
    }
}
