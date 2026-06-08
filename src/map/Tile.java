package map;

import entity.tower.Tower;
import enums.TileType;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

/**
 * 地图瓦片 —— 表示地图上的一个格子。
 */
public class Tile {
    private final int row, col;
    private final TileType type;
    private Tower occupiedTower;

    // 颜色方案 — 更丰富的色调
    private static final Color COLOR_PATH = Color.web("#d4b896");
    private static final Color COLOR_PATH_DARK = Color.web("#c4a87a");
    private static final Color COLOR_PATH_EDGE = Color.web("#a08060");
    private static final Color COLOR_BUILDABLE = Color.web("#4a7a32");
    private static final Color COLOR_BUILDABLE_LIGHT = Color.web("#5a8a42");
    private static final Color COLOR_BUILDABLE_DARK = Color.web("#3a6a22");
    private static final Color COLOR_BLOCKED = Color.web("#3a3a3a");
    private static final Color COLOR_START = Color.web("#40a040");
    private static final Color GRID_COLOR = Color.web("#00000012");

    // 终点门图片
    private static Image doorImage;
    private static boolean doorImageLoaded = false;

    private static void loadDoorImage() {
        if (!doorImageLoaded) {
            try {
                doorImage = new Image("file:image/door .jpg");
                doorImageLoaded = true;
            } catch (Exception e) {
                doorImage = null;
                doorImageLoaded = true;
            }
        }
    }

    public Tile(int row, int col, TileType type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.occupiedTower = null;
    }

    public boolean isWalkable() {
        return type == TileType.PATH || type == TileType.START || type == TileType.END;
    }

    public boolean isBuildable() {
        return type == TileType.BUILDABLE && occupiedTower == null;
    }

    public boolean hasTower() {
        return occupiedTower != null;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public TileType getType() { return type; }
    public Tower getOccupiedTower() { return occupiedTower; }
    public void setOccupiedTower(Tower tower) { this.occupiedTower = tower; }

    /**
     * 渲染瓦片。
     */
    public void render(GraphicsContext gc, int tileSize) {
        double x = col * tileSize;
        double y = row * tileSize;

        switch (type) {
            case PATH:
                renderPath(gc, x, y, tileSize);
                break;
            case BUILDABLE:
                renderGrass(gc, x, y, tileSize);
                break;
            case BLOCKED:
                renderBlocked(gc, x, y, tileSize);
                break;
            case START:
                renderStart(gc, x, y, tileSize);
                break;
            case END:
                renderEnd(gc, x, y, tileSize);
                break;
            default:
                renderGrass(gc, x, y, tileSize);
                break;
        }

        // 统一网格线
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, tileSize, tileSize);
    }

    /**
     * 渲染草地（可建造区域）
     */
    private void renderGrass(GraphicsContext gc, double x, double y, int size) {
        // 基础草地色 — 棋盘格交替
        boolean isEven = (row + col) % 2 == 0;
        gc.setFill(isEven ? COLOR_BUILDABLE : COLOR_BUILDABLE_DARK);
        gc.fillRect(x, y, size, size);

        // 草地纹理：随机小点 + 小草
        gc.setFill(COLOR_BUILDABLE_LIGHT);
        int seed = row * 1000 + col;
        for (int i = 0; i < 6; i++) {
            int px = (int) (x + ((seed * (i + 1) * 7) % (size - 4)) + 2);
            int py = (int) (y + ((seed * (i + 1) * 13) % (size - 4)) + 2);
            gc.fillOval(px, py, 2, 2);
        }
        // 小草叶
        gc.setFill(Color.web("#6aaa52"));
        for (int i = 0; i < 3; i++) {
            int px = (int) (x + ((seed * (i + 1) * 17) % (size - 6)) + 3);
            int py = (int) (y + ((seed * (i + 1) * 23) % (size - 6)) + 3);
            gc.fillRect(px, py, 1, 3);
        }
    }

    /**
     * 渲染路径
     */
    private void renderPath(GraphicsContext gc, double x, double y, int size) {
        // 路径底色
        gc.setFill(COLOR_PATH);
        gc.fillRect(x, y, size, size);

        // 路径边缘装饰
        gc.setFill(COLOR_PATH_EDGE);
        gc.fillRect(x, y, size, 1);
        gc.fillRect(x, y + size - 1, size, 1);
        gc.fillRect(x, y, 1, size);
        gc.fillRect(x + size - 1, y, 1, size);

        // 路径纹理：细沙效果
        gc.setFill(COLOR_PATH_DARK);
        int seed = row * 1000 + col;
        for (int i = 0; i < 8; i++) {
            int px = (int) (x + ((seed * (i + 1) * 11) % (size - 4)) + 2);
            int py = (int) (y + ((seed * (i + 1) * 17) % (size - 4)) + 2);
            gc.fillOval(px, py, 1, 1);
        }
    }

    /**
     * 渲染障碍物
     */
    private void renderBlocked(GraphicsContext gc, double x, double y, int size) {
        gc.setFill(COLOR_BLOCKED);
        gc.fillRect(x, y, size, size);

        // 岩石纹理
        gc.setFill(Color.web("#4a4a4a"));
        gc.fillRect(x + 4, y + 4, size - 8, size - 8);
        gc.setFill(Color.web("#2a2a2a"));
        gc.fillRect(x + 8, y + 8, size - 16, size - 16);
    }

    /**
     * 渲染起点
     */
    private void renderStart(GraphicsContext gc, double x, double y, int size) {
        // 绿色发光起点
        gc.setFill(COLOR_START);
        gc.fillRect(x, y, size, size);

        // 起点标记
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Microsoft YaHei", javafx.scene.text.FontWeight.BOLD, 14));
        gc.fillText("S", x + size / 2.0 - 6, y + size / 2.0 + 5);

        // 发光效果
        gc.setFill(Color.web("#40ff4022"));
        gc.fillOval(x + size / 2.0 - 16, y + size / 2.0 - 16, 32, 32);
    }

    /**
     * 渲染终点
     */
    private void renderEnd(GraphicsContext gc, double x, double y, int size) {
        loadDoorImage();

        if (doorImage != null) {
            // 使用门图片
            gc.drawImage(doorImage, x, y, size, size);
        } else {
            // 备用：红色发光终点
            gc.setFill(Color.web("#d04040"));
            gc.fillRect(x, y, size, size);

            // 终点标记
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Microsoft YaHei", javafx.scene.text.FontWeight.BOLD, 14));
            gc.fillText("E", x + size / 2.0 - 6, y + size / 2.0 + 5);

            // 发光效果
            gc.setFill(Color.web("#ff404022"));
            gc.fillOval(x + size / 2.0 - 16, y + size / 2.0 - 16, 32, 32);
        }
    }
}
