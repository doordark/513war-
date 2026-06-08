package map;

import enums.TileType;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏地图 —— 管理瓦片网格、路径点和关卡加载。
 */
public class GameMap {

    // 地图尺寸 — 更细密的网格
    public static final int ROWS = 12;
    public static final int COLS = 20;
    public static final int TILE_SIZE = 48;

    private Tile[][] grid;
    private List<Point2D> waypoints;

    // 地图数据 (0=BUILDABLE, 1=PATH, 2=BLOCKED, 3=START, 4=END)
    // 起点左上 → 终点左下
    private static final int[][] LEVEL_1 = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
        {4, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    public GameMap() {
        this.grid = new Tile[ROWS][COLS];
        this.waypoints = new ArrayList<>();
    }

    /**
     * 加载关卡地图。
     */
    public void loadMap(int level) {
        waypoints.clear();

        int[][] data = LEVEL_1;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                TileType type;
                switch (data[r][c]) {
                    case 0: type = TileType.BUILDABLE; break;
                    case 1: type = TileType.PATH; break;
                    case 2: type = TileType.BLOCKED; break;
                    case 3: type = TileType.START; break;
                    case 4: type = TileType.END; break;
                    default: type = TileType.BUILDABLE; break;
                }
                grid[r][c] = new Tile(r, c, type);
            }
        }

        // 从起点追踪到终点，生成正确的路径点
        waypoints = tracePath();
        System.out.println("[地图加载] 关卡=" + level + " 路径点数量=" + waypoints.size());
        for (int i = 0; i < waypoints.size(); i++) {
            Point2D p = waypoints.get(i);
            System.out.println("  路径点[" + i + "]: (" + String.format("%.0f", p.getX()) + ", " + String.format("%.0f", p.getY()) + ")");
        }
    }

    /**
     * 从起点开始追踪路径到终点，返回正确的路径点序列。
     */
    private List<Point2D> tracePath() {
        List<Point2D> path = new ArrayList<>();

        // 找到起点
        int startRow = -1, startCol = -1;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].getType() == TileType.START) {
                    startRow = r;
                    startCol = c;
                    break;
                }
            }
            if (startRow >= 0) break;
        }

        if (startRow < 0) return path;

        // 从起点开始追踪
        int curRow = startRow;
        int curCol = startCol;
        int prevRow = -1;
        int prevCol = -1;

        while (true) {
            // 添加当前格子中心点
            path.add(new Point2D(curCol * TILE_SIZE + TILE_SIZE / 2.0, curRow * TILE_SIZE + TILE_SIZE / 2.0));

            // 检查是否到达终点
            if (grid[curRow][curCol].getType() == TileType.END) {
                break;
            }

            // 找下一个相邻的路径格子（排除来时的方向）
            int nextRow = -1, nextCol = -1;

            // 四个方向：上、下、左、右
            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};

            for (int i = 0; i < 4; i++) {
                int nr = curRow + dr[i];
                int nc = curCol + dc[i];

                // 跳过边界外
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                // 跳过不是路径的格子
                TileType t = grid[nr][nc].getType();
                if (t != TileType.PATH && t != TileType.END) continue;
                // 跳回来时的方向
                if (nr == prevRow && nc == prevCol) continue;

                nextRow = nr;
                nextCol = nc;
                break;
            }

            // 找不到下一个格子，路径断了
            if (nextRow < 0) break;

            prevRow = curRow;
            prevCol = curCol;
            curRow = nextRow;
            curCol = nextCol;
        }

        return path;
    }

    public Tile getTile(int row, int col) {
        if (!isValidTile(row, col)) return null;
        return grid[row][col];
    }

    public boolean isValidTile(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public Point2D getStartPoint() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].getType() == TileType.START) {
                    return new Point2D(c * TILE_SIZE + TILE_SIZE / 2.0, r * TILE_SIZE + TILE_SIZE / 2.0);
                }
            }
        }
        return new Point2D(0, 0);
    }

    public Point2D getEndPoint() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].getType() == TileType.END) {
                    return new Point2D(c * TILE_SIZE + TILE_SIZE / 2.0, r * TILE_SIZE + TILE_SIZE / 2.0);
                }
            }
        }
        return new Point2D(COLS * TILE_SIZE, ROWS * TILE_SIZE);
    }

    public List<Point2D> getWaypoints() {
        return waypoints;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public int getRows() { return ROWS; }
    public int getCols() { return COLS; }

    /**
     * 渲染地图。
     */
    public void render(GraphicsContext gc) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c].render(gc, TILE_SIZE);
            }
        }
    }
}
