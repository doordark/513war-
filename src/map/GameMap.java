package map;

import enums.TileType;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏地图 —— 管理瓦片网格、路径点和关卡加载。
 * 4 种难度对应 4 张不同复杂度的地图：
 *   EASY      → 简单 S 形，路径短，怪物少
 *   NORMAL    → 双弯折，路径中等
 *   HARD      → 多弯折蛇形，路径长
 *   NIGHTMARE → 复杂多回路，路径最长，怪物最多
 */
public class GameMap {

    public static final int ROWS = 12;
    public static final int COLS = 20;
    public static final int TILE_SIZE = 48;

    private Tile[][] grid;
    private List<Point2D> waypoints;

    // 地图数据 (0=BUILDABLE, 1=PATH, 2=BLOCKED, 3=START, 4=END)

    // ===== 简单模式：S 形短路径 =====
    private static final int[][] MAP_EASY = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    // ===== 普通模式：双弯折 =====
    private static final int[][] MAP_NORMAL = {
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

    // ===== 困难模式：蛇形多弯折 =====
    private static final int[][] MAP_HARD = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 4},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    // ===== 噩梦模式：超长蛇形路径，来回折返 =====
    // 路径：(0,1)→右→(18,1)→下→(18,4)→左→(0,4)→下→(0,7)→右→(18,7)→下→(18,10)→终点
    private static final int[][] MAP_NIGHTMARE = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    public GameMap() {
        this.grid = new Tile[ROWS][COLS];
        this.waypoints = new ArrayList<>();
    }

    /**
     * 根据难度加载对应地图。
     */
    public void loadMap(main.GameSettings.Difficulty difficulty) {
        waypoints.clear();

        int[][] data;
        String mapName;
        switch (difficulty) {
            case EASY:
                data = MAP_EASY;
                mapName = "简单";
                break;
            case HARD:
                data = MAP_HARD;
                mapName = "困难";
                break;
            case NIGHTMARE:
                data = MAP_NIGHTMARE;
                mapName = "噩梦";
                break;
            default: // NORMAL
                data = MAP_NORMAL;
                mapName = "普通";
                break;
        }

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

        waypoints = tracePath();
        System.out.println("[地图加载] 难度=" + mapName + " 路径点数量=" + waypoints.size());
        for (int i = 0; i < waypoints.size(); i++) {
            Point2D p = waypoints.get(i);
            System.out.println("  路径点[" + i + "]: (" + String.format("%.0f", p.getX()) + ", " + String.format("%.0f", p.getY()) + ")");
        }
    }

    /**
     * 兼容旧接口：按关卡编号加载（默认使用普通地图）。
     */
    public void loadMap(int level) {
        loadMap(main.GameSettings.Difficulty.NORMAL);
    }

    private List<Point2D> tracePath() {
        List<Point2D> path = new ArrayList<>();

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

        // 使用栈式回溯 DFS 寻路，处理 T 形路口和死路
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        // 栈：存储 [row, col, 下一个要尝试的方向索引]
        java.util.Stack<int[]> stack = new java.util.Stack<>();
        boolean[][] visited = new boolean[ROWS][COLS];

        stack.push(new int[]{startRow, startCol, 0});
        visited[startRow][startCol] = true;

        while (!stack.isEmpty()) {
            int[] top = stack.peek();
            int curRow = top[0];
            int curCol = top[1];
            int dirIdx = top[2];

            // 到达终点
            if (grid[curRow][curCol].getType() == TileType.END) {
                // 构建最终路径
                for (int[] cell : stack) {
                    path.add(new Point2D(
                        cell[1] * TILE_SIZE + TILE_SIZE / 2.0,
                        cell[0] * TILE_SIZE + TILE_SIZE / 2.0));
                }
                return path;
            }

            // 尝试下一个方向
            boolean found = false;
            while (dirIdx < 4) {
                int nr = curRow + dr[dirIdx];
                int nc = curCol + dc[dirIdx];
                dirIdx++;
                top[2] = dirIdx; // 更新方向索引

                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                if (visited[nr][nc]) continue;
                TileType t = grid[nr][nc].getType();
                if (t != TileType.PATH && t != TileType.END) continue;

                visited[nr][nc] = true;
                stack.push(new int[]{nr, nc, 0});
                found = true;
                break;
            }

            if (!found) {
                // 死路，回溯
                stack.pop();
            }
        }

        System.err.println("[地图加载] 警告：未找到从起点到终点的路径！");
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

    public void render(GraphicsContext gc) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c].render(gc, TILE_SIZE);
            }
        }
    }
}
