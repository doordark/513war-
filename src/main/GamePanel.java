package main;

import map.GameMap;
import map.Tile;
import entity.tower.Tower;
import entity.tower.ArrowTower;
import entity.tower.CannonTower;
import entity.tower.MagicTower;
import entity.tower.SlowTower;
import entity.tower.LightningTower;
import entity.tower.NukeTower;
import entity.monster.Monster;
import entity.Projectile;
import entity.LightningEffect;
import entity.ExplosionEffect;
import entity.FloatingText;
import wave.WaveManager;
import sound.SoundManager;
import enums.GameState;
import enums.TowerType;
import enums.TileType;
import enums.WeaponData;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏主画布 —— JavaFX Canvas 渲染 + 游戏世界顶层管理器。
 * 由 GameEngine 每帧驱动 updateGame() 和 render()。
 */
public class GamePanel {

    // ==================== 常量 ====================
    private static final int WIDTH = 960;
    private static final int HEIGHT = 600;

    // ==================== JavaFX 组件 ====================
    private final Canvas canvas;
    private final GraphicsContext gc;

    // ==================== 游戏世界实体 ====================
    private GameMap gameMap;
    private final List<Tower> towerList;
    private final List<Monster> monsterList;
    private final List<Projectile> projectileList;
    private WaveManager waveManager;

    // ==================== 视觉效果 ====================
    private final List<LightningEffect> lightningEffects;
    private final List<ExplosionEffect> explosionEffects;
    private final List<FloatingText> floatingTexts;

    // ==================== 游戏状态 ====================
    private GameState gameState;
    private GameState previousGameState; // 用于从设置面板返回时恢复状态

    // ==================== 玩家资源 ====================
    private int playerGold;
    private int playerLives;
    private int score;

    // ==================== 经验值/等级系统 ====================
    private int playerXP;
    private int playerLevel;
    private int xpToNextLevel;

    // ==================== 已购买的塔类型索引 ====================
    private PurchasedItems purchasedItems;

    // ==================== 排行榜 ====================
    private ScoreManager scoreManager;
    private LeaderboardOverlay leaderboardOverlay;

    // ==================== UI 交互状态 ====================
    private TowerType selectedTowerType;
    private Tower selectedTower;
    private boolean showRange;
    private double mouseX, mouseY;

    // ==================== 动画效果 ====================
    private Starfield starfield;

    // ==================== 游戏引擎引用 ====================
    private GameEngine gameEngine;

    public void setGameEngine(GameEngine engine) { this.gameEngine = engine; }

    // ==================== 设置 ====================
    private GameSettings settings;

    // ==================== 无尽模式 ====================
    private boolean isEndlessMode = false;

    // ==================== 放置模式 ====================
    private boolean placementMode = false;
    private WeaponData pendingWeapon = null;
    private int pendingWeaponIndex = -1;

    // ==================== 菜单按钮区域 ====================
    private static class MenuButton {
        String text;
        double x, y, w, h;
        boolean hovered;
        MenuButton(String text, double x, double y, double w, double h) {
            this.text = text; this.x = x; this.y = y; this.w = w; this.h = h;
        }
        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
    // ==================== HUD 根节点 ====================
    private HudPanel hudPanel;

    // ==================== 暂停弹窗 ====================
    private PauseOverlay pauseOverlay;

    // ==================== 主菜单 ====================
    private MainMenu mainMenu;

    // ==================== 难度选择 ====================
    private DifficultySelectOverlay difficultyOverlay;

    // ==================== 设置面板 ====================
    private SettingsOverlay settingsOverlay;

    // ==================== 商城面板 ====================
    private ShopOverlay shopOverlay;

    // ==================== 构造 ====================
    public GamePanel() {
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.towerList = new ArrayList<>();
        this.monsterList = new ArrayList<>();
        this.projectileList = new ArrayList<>();
        this.lightningEffects = new ArrayList<>();
        this.explosionEffects = new ArrayList<>();
        this.floatingTexts = new ArrayList<>();
        this.gameState = GameState.MENU;
        this.playerGold = 150;
        this.playerLives = 20;
        this.score = 0;
        this.playerXP = 0;
        this.playerLevel = 1;
        this.xpToNextLevel = 100;
        this.purchasedItems = new PurchasedItems();
        this.selectedTowerType = null;
        this.selectedTower = null;
        this.showRange = false;
        this.mouseX = 0;
        this.mouseY = 0;

        // 创建设置
        this.settings = new GameSettings();

        // 创建 HUD 面板（JavaFX 控件）
        this.hudPanel = new HudPanel(this);

        // 创建暂停弹窗
        this.pauseOverlay = new PauseOverlay(this);

        // 创建主菜单
        this.mainMenu = new MainMenu(this);

        // 创建难度选择面板
        this.difficultyOverlay = new DifficultySelectOverlay(this);

        // 创建设置面板
        this.settingsOverlay = new SettingsOverlay(this);

        // 创建商城面板
        this.shopOverlay = new ShopOverlay(this);

        // 创建排行榜管理器
        this.scoreManager = new ScoreManager();
        this.leaderboardOverlay = new LeaderboardOverlay(scoreManager, () -> {
            gameState = GameState.MENU;
            mainMenu.refreshUI();
            mainMenu.show();
        });

        // 创建星空背景
        this.starfield = new Starfield(WIDTH, HEIGHT);

        initInput();
    }

    // ==================== Getter ====================
    public Canvas getCanvas() { return canvas; }
    public HudPanel getHudRoot() { return hudPanel; }
    public GameState getGameState() { return gameState; }
    public int getPlayerGold() { return playerGold; }
    public int getPlayerLives() { return playerLives; }
    public int getScore() { return score; }
    public int getPlayerXP() { return playerXP; }
    public int getPlayerLevel() { return playerLevel; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public ShopOverlay getShopOverlay() { return shopOverlay; }
    public boolean isPlacementMode() { return placementMode; }
    public LeaderboardOverlay getLeaderboardOverlay() { return leaderboardOverlay; }
    public ScoreManager getScoreManager() { return scoreManager; }

    // 主菜单顶部数据同步用
    public int getHp() { return playerLives; }
    public int getGold() { return playerGold; }
    public int getXp() { return playerXP; }
    public int getXpMax() { return xpToNextLevel; }
    public int getLevel() { return playerLevel; }

    /** 获取已购买的塔索引列表 */
    public java.util.List<Integer> getPurchasedTowerIndices() {
        return purchasedItems.getAll();
    }
    public int getCurrentWave() { return waveManager != null ? waveManager.getCurrentWave() : 0; }
    public int getTotalWaves() { return waveManager != null ? waveManager.getTotalWaves() : 0; }
    public Tower getSelectedTower() { return selectedTower; }
    public TowerType getSelectedTowerType() { return selectedTowerType; }
    public GameMap getGameMap() { return gameMap; }
    public List<Monster> getMonsterList() { return monsterList; }
    public List<Tower> getTowerList() { return towerList; }
    public boolean isWaveActive() { return waveManager != null && waveManager.isWaveActive(); }
    public boolean isAllWavesComplete() { return waveManager != null && waveManager.isAllComplete(); }
    public boolean isEndlessMode() { return isEndlessMode; }
    public void startNextWave() { if (waveManager != null) waveManager.requestNextWave(); }
    public PauseOverlay getPauseOverlay() { return pauseOverlay; }
    public GameSettings getSettings() { return settings; }
    public MainMenu getMainMenu() { return mainMenu; }
    public DifficultySelectOverlay getDifficultyOverlay() { return difficultyOverlay; }
    public SettingsOverlay getSettingsOverlay() { return settingsOverlay; }

    // ==================== 初始化 ====================

    public void startGame(int level) {
        gameMap = new GameMap();
        gameMap.loadMap(settings.getDifficulty());

        towerList.clear();
        monsterList.clear();
        projectileList.clear();

        isEndlessMode = false;
        waveManager = new WaveManager(level, settings.getDifficulty());

        // 从全局数据同步到游戏面板
        playerGold = GameData.gold;
        playerLives = GameData.hp;
        score = GameData.score;
        playerXP = GameData.exp;
        playerLevel = GameData.level;
        selectedTowerType = null;
        selectedTower = null;
        showRange = false;

        gameState = GameState.PLAYING;

        // 显示 HUD
        hudPanel.setVisible(true);
        hudPanel.setManaged(true);

        // 启用商城按钮
        mainMenu.enableShopButton();

        // 播放 BGM
        SoundManager.getInstance().playBGM();

        System.out.println("[游戏开始] 关卡=" + level + " 金币=" + playerGold + " 生命=" + playerLives);

        // 更新 HUD
        hudPanel.updateState();
    }

    /**
     * 继续游戏：从存档恢复，不重置任何数据。
     */
    public void continueGame() {
        GameSave.SaveData savedData = GameSave.load();
        if (savedData == null) {
            System.out.println("[继续游戏] 未找到存档，无法继续");
            return;
        }

        gameMap = new GameMap();
        gameMap.loadMap(settings.getDifficulty());

        towerList.clear();
        monsterList.clear();
        projectileList.clear();

        // 从存档恢复数据
        playerGold = savedData.playerGold;
        playerLives = savedData.playerLives;
        score = savedData.score;
        playerXP = 0;
        playerLevel = 1;

        // 同步到 GameData
        GameData.gold = playerGold;
        GameData.hp = playerLives;
        GameData.score = score;

        isEndlessMode = false;
        waveManager = new WaveManager(1, settings.getDifficulty());

        // 恢复塔
        for (GameSave.TowerSave ts : savedData.towers) {
            Tower t = createTowerFromSave(ts);
            if (t != null) {
                towerList.add(t);
            }
        }

        // 恢复怪物
        for (GameSave.MonsterSave ms : savedData.monsters) {
            Monster m = createMonsterFromSave(ms);
            if (m != null) {
                monsterList.add(m);
            }
        }

        selectedTowerType = null;
        selectedTower = null;
        showRange = false;

        gameState = GameState.PLAYING;

        hudPanel.setVisible(true);
        hudPanel.setManaged(true);
        mainMenu.enableShopButton();

        // 播放 BGM
        SoundManager.getInstance().playBGM();

        System.out.println("[继续游戏] 已恢复存档 - 金币=" + playerGold + " 生命=" + playerLives + " 波次=" + savedData.currentWave);
        hudPanel.updateState();
    }

    /**
     * 从存档数据创建防御塔。
     */
    private Tower createTowerFromSave(GameSave.TowerSave ts) {
        try {
            // 使用现有的 createTower 工厂方法
            TowerType type = TowerType.valueOf(ts.type.toUpperCase());
            Tower t = createTower(type, ts.row, ts.col);
            if (t == null) return null;
            // 恢复等级和属性
            for (int i = 1; i < ts.level; i++) {
                t.upgrade();
            }
            return t;
        } catch (Exception e) {
            System.err.println("[继续游戏] 恢复塔失败: " + ts.type + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 从存档数据创建怪物。
     */
    private Monster createMonsterFromSave(GameSave.MonsterSave ms) {
        try {
            Monster m;
            switch (ms.type.toUpperCase()) {
                case "NORMAL": m = new entity.monster.NormalMonster(ms.x, ms.y); break;
                case "FAST":   m = new entity.monster.FastMonster(ms.x, ms.y); break;
                case "TANK":   m = new entity.monster.TankMonster(ms.x, ms.y); break;
                case "BOSS":   m = new entity.monster.BossMonster(ms.x, ms.y); break;
                default:       m = new entity.monster.NormalMonster(ms.x, ms.y); break;
            }
            m.setCurrentHp(ms.currentHp);
            m.setMaxHp(ms.maxHp);
            return m;
        } catch (Exception e) {
            System.err.println("[继续游戏] 恢复怪物失败: " + ms.type + " - " + e.getMessage());
            return null;
        }
    }

    /** 启动无尽模式。 */
    public void startEndlessGame() {
        gameMap = new GameMap();
        gameMap.loadMap(settings.getDifficulty());

        towerList.clear();
        monsterList.clear();
        projectileList.clear();

        isEndlessMode = true;
        waveManager = new WaveManager(1, settings.getDifficulty(), true);

        // 从全局数据同步到游戏面板
        playerGold = GameData.gold;
        playerLives = GameData.hp;
        score = GameData.score;
        playerXP = GameData.exp;
        playerLevel = GameData.level;
        selectedTowerType = null;
        selectedTower = null;
        showRange = false;

        gameState = GameState.PLAYING;

        hudPanel.setVisible(true);
        hudPanel.setManaged(true);
        mainMenu.enableShopButton();

        // 播放 BGM
        SoundManager.getInstance().playBGM();

        System.out.println("[无尽模式开始] 金币=" + playerGold + " 生命=" + playerLives);
        hudPanel.updateState();
    }

    private void initInput() {
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClick);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDrag);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        canvas.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            mouseX = -1;
            mouseY = -1;
        });
        // 键盘事件绑定到 Canvas（需要 Canvas 获取焦点）
        canvas.setFocusTraversable(true);
        canvas.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            handleKeyPress(e.getCode());
        });
    }

    private void handleMouseDrag(MouseEvent e) {
        if (gameState != GameState.SETTINGS) return;

        // BGM 滑块拖动
        if (mouseX >= 400 && mouseX <= 700 && mouseY >= 170 && mouseY <= 194) {
            double relX = (e.getX() - 400) / 300;
            settings.setBgmVolume(Math.max(0, Math.min(1, relX)));
        }
        // 音效滑块拖动
        if (mouseX >= 400 && mouseX <= 700 && mouseY >= 240 && mouseY <= 264) {
            double relX = (e.getX() - 400) / 300;
            settings.setSfxVolume(Math.max(0, Math.min(1, relX)));
        }
    }

    public void handleKeyPress(KeyCode code) {
        switch (code) {
            case ESCAPE:
                if (placementMode) {
                    cancelPlacement();
                } else if (gameState == GameState.PLAYING) {
                    togglePause();
                } else if (gameState == GameState.PAUSED) {
                    resumeGame();
                } else if (gameState == GameState.SETTINGS) {
                    settingsOverlay.setVisible(false);
                    settingsOverlay.setManaged(false);
                    gameState = GameState.MENU;
                    mainMenu.refreshUI();
                    mainMenu.show();
                } else if (gameState == GameState.DIFFICULTY_SELECT) {
                    difficultyOverlay.setVisible(false);
                    difficultyOverlay.setManaged(false);
                    gameState = GameState.MENU;
                    mainMenu.refreshUI();
                    mainMenu.show();
                } else if (gameState == GameState.SHOP) {
                    shopOverlay.setVisible(false);
                    shopOverlay.setManaged(false);
                    // 根据商城打开来源决定返回状态
                    if (shopOverlay.getOpenSource() == 2) {
                        gameState = GameState.PLAYING;
                    } else {
                        gameState = GameState.PAUSED;
                        pauseOverlay.show();
                    }
                }
                break;
            case P:
            case SPACE:
                if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
                    togglePause();
                }
                break;
            case DIGIT1:
            case NUMPAD1:
                quickSelectTower(0);
                break;
            case DIGIT2:
            case NUMPAD2:
                quickSelectTower(1);
                break;
            case DIGIT3:
            case NUMPAD3:
                quickSelectTower(2);
                break;
            case DIGIT4:
            case NUMPAD4:
                quickSelectTower(3);
                break;
        }
    }

    // ==================== 每帧更新 ====================

    public void updateGame() {
        // 星空背景始终更新
        long elapsedMs = System.currentTimeMillis();
        starfield.update(elapsedMs);

        if (gameState != GameState.PLAYING) {
            return;
        }

        updateWave();
        updateMonsters();
        updateTowers();
        updateProjectiles();
        checkCollisions();
        updateEffects();
        checkGameOver();
        checkVictory();
    }

    private void updateWave() {
        if (waveManager != null) {
            waveManager.setMonsterListEmpty(monsterList.isEmpty());
            waveManager.update(monsterList, gameMap.getStartPoint());
        }
    }

    private void updateMonsters() {
        List<javafx.geometry.Point2D> waypoints = gameMap.getWaypoints();
        List<Monster> toRemove = new ArrayList<>();
        for (Monster m : monsterList) {
            m.update(waypoints);

            if (m.isReachedEnd()) {
                System.out.println("[怪物逃脱] ID=" + m.getId() + " 类型=" + m.getType()
                    + " 坐标=(" + String.format("%.1f", m.getX()) + ", " + String.format("%.1f", m.getY()) + ")"
                    + " 扣血=" + m.getDamageToPlayer());
                playerLives -= m.getDamageToPlayer();
                GameData.hp = playerLives;
                toRemove.add(m);
                continue;
            }
            if (!m.isAlive()) {
                System.out.println("[怪物死亡] ID=" + m.getId() + " 类型=" + m.getType()
                    + " 坐标=(" + String.format("%.1f", m.getX()) + ", " + String.format("%.1f", m.getY()) + ")"
                    + " 击杀塔=" + (m.getLastHitByTowerType() != null ? m.getLastHitByTowerType() : "未知")
                    + " 奖励金币=" + m.getRewardGold());
                playerGold += m.getRewardGold();
                GameData.gold = playerGold;

                // 无尽模式高风险高回报积分：波数越高，杀一只怪给的分数越夸张
                if (isEndlessMode) {
                    int baseKillScore = 10;
                    int endlessScore = baseKillScore * (1 + getCurrentWave());
                    score += endlessScore;
                    System.out.println("[无尽积分] 波次" + getCurrentWave() + " 击杀+" + endlessScore + " 总分=" + score);
                } else {
                    score += m.getRewardGold();
                }
                GameData.score = score;
                addXP(m.getXpReward());
                toRemove.add(m);
            }
        }
        monsterList.removeAll(toRemove);
        hudPanel.updateState();
    }

    private void updateTowers() {
        for (Tower t : towerList) {
            t.update(monsterList, projectileList);
        }
    }

    private void updateProjectiles() {
        for (Projectile p : projectileList) {
            p.update();
        }
    }

    private void checkCollisions() {
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : projectileList) {
            if (p.hasHit()) {
                // 结算伤害
                boolean needExplosion = p.onHit(monsterList);
                if (needExplosion) {
                    explosionEffects.add(new ExplosionEffect(p.getX(), p.getY(), Color.ORANGE));
                }

                // 伤害飘字
                if (p.getTarget() != null && p.getTarget().isAlive()) {
                    Monster target = p.getTarget();
                    String towerType = p.getTowerType();
                    Color textColor = Color.RED;

                    // 根据塔类型设置飘字颜色
                    if (towerType != null) {
                        switch (towerType) {
                            case "ARROW":     textColor = Color.YELLOW; break;
                            case "CANNON":    textColor = Color.ORANGE; break;
                            case "SLOW":      textColor = Color.CYAN; break;
                            case "LIGHTNING": textColor = Color.web("#9664ff"); break;
                            case "NUKE":      textColor = Color.RED; break;
                            default:          textColor = Color.RED; break;
                        }
                    }

                    // 减速塔显示特殊文字
                    if (towerType != null && towerType.equals("SLOW")) {
                        floatingTexts.add(new FloatingText(target.getX(), target.getY() - 20, "减速!", Color.CYAN));
                    }

                    floatingTexts.add(new FloatingText(target.getX(), target.getY() - 10, "-" + p.getDamage(), textColor));
                }

                toRemove.add(p);
            } else if (isOutOfBounds(p)) {
                toRemove.add(p);
            }
        }
        projectileList.removeAll(toRemove);
    }

    /**
     * 更新所有视觉特效（闪电、爆炸、飘字）。
     */
    private void updateEffects() {
        // 更新爆炸效果
        for (ExplosionEffect e : explosionEffects) {
            e.update();
        }
        explosionEffects.removeIf(ExplosionEffect::isExpired);

        // 更新飘字
        for (FloatingText t : floatingTexts) {
            t.update();
        }
        floatingTexts.removeIf(FloatingText::isExpired);

        // 闪电效果不需要每帧更新，只需移除过期的
        lightningEffects.removeIf(LightningEffect::isExpired);
    }

    private boolean isOutOfBounds(Projectile p) {
        return p.getX() < -50 || p.getX() > WIDTH + 50
            || p.getY() < -50 || p.getY() > HEIGHT + 50;
    }

    private void checkGameOver() {
        if (playerLives <= 0 && gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            gameEngine.stop();
            hudPanel.updateState();
            handleGameEnd(false);
        }
    }

    private void checkVictory() {
        // 无尽模式永不胜利，只有死亡
        if (isEndlessMode) return;
        if (waveManager != null && waveManager.isAllComplete() && monsterList.isEmpty() && gameState == GameState.PLAYING) {
            gameState = GameState.VICTORY;
            gameEngine.stop();
            hudPanel.updateState();
            handleGameEnd(true);
        }
    }

    /**
     * 游戏结束处理：弹出名字输入框，保存分数。
     * 无尽模式下，记录坚持的最高波数。
     * 普通模式不计入排行榜。
     */
    private void handleGameEnd(boolean victory) {
        int finalScore;
        int wave = waveManager != null ? waveManager.getCurrentWave() : 0;
        String difficulty = settings.getDifficulty().getDisplayName();

        if (isEndlessMode) {
            // 无尽模式：记录坚持的最高波数
            finalScore = wave;
        } else {
            finalScore = score + playerLives * 100 + wave * 50;
        }

        // 弹出名字输入框
        NameInputDialog dialog = new NameInputDialog();
        String title = isEndlessMode ? "无尽模式结束"
                       : (victory ? "胜利！" : "游戏结束");
        String message = isEndlessMode ? "你坚持了 " + wave + " 波！请输入你的名字保存成绩。"
                       : (victory ? "恭喜通关！请输入你的名字保存成绩。" : "挑战失败！请输入你的名字保存成绩。");
        String playerName = dialog.show(title, message, finalScore);

        // 排行榜准入条件：仅无尽模式可写入
        if (playerName != null) {
            if (isEndlessMode) {
                scoreManager.tryAddScore(playerName, finalScore, wave, difficulty, true);
                System.out.println("[排行榜] 无尽模式成绩已保存: " + playerName + " - " + finalScore + "分");
            } else {
                System.out.println("[排行榜] 普通模式不计入排行榜，快去挑战无尽模式吧！");
                // 普通模式不保存分数到排行榜
            }
        }
    }

    /**
     * 无尽模式主动结算流程：停止游戏 → 时间命名 → 写入排行榜 → 返回主菜单。
     */
    public void triggerEndlessSettlement() {
        System.out.println("[无尽结算] 开始结算流程...");

        // 1. 停止游戏引擎
        if (gameEngine != null) {
            gameEngine.stop();
        }
        gameState = GameState.MENU;

        // 停止 BGM
        SoundManager.getInstance().stopBGM();

        // 2. 获取当前时间作为默认名字
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String defaultName = java.time.LocalDateTime.now().format(dtf);

        // 3. 将当前积分与时间名字写入无尽排行榜
        int wave = waveManager != null ? waveManager.getCurrentWave() : 0;
        String difficulty = settings.getDifficulty().getDisplayName();
        scoreManager.tryAddScore(defaultName, score, wave, difficulty, true);
        System.out.println("[排行榜] 无尽模式成绩已保存: " + defaultName + " - " + score + "分");

        // 4. 弹出结算成功提示
        javafx.scene.control.Alert infoAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        infoAlert.setTitle("结算成功");
        infoAlert.setHeaderText("你的成绩已载入 513 荣誉墙！");
        infoAlert.setContentText("得分: " + score + "\n记录名: " + defaultName);
        infoAlert.showAndWait();

        // 5. 返回主菜单
        GameData.hp = playerLives;
        GameData.gold = playerGold;
        GameData.exp = playerXP;
        GameData.level = playerLevel;
        GameData.score = score;
        GameData.isEndless = false;

        isEndlessMode = false;
        selectedTower = null;
        selectedTowerType = null;
        showRange = false;

        hudPanel.setVisible(false);
        hudPanel.setManaged(false);
        mainMenu.disableShopButton();
        mainMenu.refreshUI();
        mainMenu.show();
        mainMenu.toFront();

        System.out.println("[无尽结算] 完成，已返回主菜单");
    }

    /**
     * 添加经验值，自动处理升级。
     */
    public void addXP(int amount) {
        playerXP += amount;
        GameData.exp = playerXP;
        System.out.println("[获得经验] +" + amount + " 总经验=" + playerXP + " 等级=" + playerLevel);

        while (playerXP >= xpToNextLevel) {
            playerXP -= xpToNextLevel;
            GameData.exp = playerXP;
            playerLevel++;
            GameData.level = playerLevel;
            xpToNextLevel = (int) (xpToNextLevel * 1.5);
            System.out.println("[升级] 恭喜！当前等级 LV." + playerLevel + " 下一级需要 " + xpToNextLevel + " 经验");
        }
        hudPanel.updateState();
    }

    // ==================== 游戏控制 ====================

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            pauseOverlay.show();
            System.out.println("[暂停] 游戏已暂停");
        } else if (gameState == GameState.PAUSED) {
            resumeGame();
        }
    }

    public void resumeGame() {
        gameState = GameState.PLAYING;
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        System.out.println("[继续] 游戏已继续");
    }

    public void resumeFromOverlay() {
        gameState = previousGameState; // 恢复到打开设置前的状态
        if (gameState == GameState.MENU) {
            // 隐藏 HUD
            hudPanel.setVisible(false);
            hudPanel.setManaged(false);
            // 显示主菜单
            mainMenu.refreshUI();
            mainMenu.show();
            mainMenu.toFront();
        } else if (gameState == GameState.PLAYING) {
            // 从游戏中返回，确保 HUD 可见
            hudPanel.setVisible(true);
            hudPanel.setManaged(true);
        }
        System.out.println("[返回] 从设置面板返回，状态=" + gameState);
    }

    public void pauseGame() {
        gameState = GameState.PAUSED;
        pauseOverlay.show();
    }

    public void saveGame() {
        GameSave.save(this);
    }

    public void returnToMenu() {
        // 停止 BGM
        SoundManager.getInstance().stopBGM();

        // 将游戏面板数据同步回全局 GameData
        GameData.hp = playerLives;
        GameData.gold = playerGold;
        GameData.exp = playerXP;
        GameData.level = playerLevel;
        GameData.score = score;
        // 保留无尽模式状态，不要强制设为 false
        GameData.isEndless = isEndlessMode;

        gameState = GameState.MENU;
        selectedTower = null;
        selectedTowerType = null;
        showRange = false;
        hudPanel.updateState();
        mainMenu.disableShopButton();
        mainMenu.refreshUI();
        mainMenu.show();
        mainMenu.toFront(); // 强制将主菜单提升到 StackPane 最顶层
        GameSave.save(this);
        System.out.println("[返回] 已返回主菜单，游戏进度已保存");
    }

    /** 从难度选择/设置面板返回主菜单（不保存游戏进度） */
    public void returnToMenuFromOverlay() {
        // 同步数据
        GameData.hp = playerLives;
        GameData.gold = playerGold;
        GameData.exp = playerXP;
        GameData.level = playerLevel;
        GameData.score = score;

        gameState = GameState.MENU;
        mainMenu.disableShopButton();
        mainMenu.refreshUI();
        mainMenu.show();
        mainMenu.toFront(); // 强制将主菜单提升到 StackPane 最顶层
        System.out.println("[返回] 已返回主菜单");
    }

    public void showSettingsFromPause() {
        previousGameState = gameState; // 保存当前状态（PAUSED）
        gameState = GameState.SETTINGS;
        settingsOverlay.show();
    }

    public void showSettingsFromMenu() {
        previousGameState = gameState; // 保存当前状态（MENU）
        gameState = GameState.SETTINGS;
        settingsOverlay.show();
    }

    public void showDifficultySelect() {
        gameState = GameState.DIFFICULTY_SELECT;
        difficultyOverlay.show();
    }

    public void showShopFromPause() {
        gameState = GameState.SHOP;
        shopOverlay.show(true);
    }

    public void showShopFromMenu() {
        gameState = GameState.SHOP;
        shopOverlay.show(false);
    }

    /** 从游戏中直接进入商城（不暂停游戏） */
    public void showShopFromPlaying() {
        gameState = GameState.SHOP;
        shopOverlay.showFromGame();
    }

    public void showLeaderboard() {
        gameState = GameState.LEADERBOARD;
        leaderboardOverlay.show();
    }

    /**
     * 从商城购买并建造防御塔。
     * @param index 商城商品索引 (0-4)
     */
    public void purchaseAndBuildTower(int index) {
        WeaponData weapon = WeaponData.values()[index];
        int currentLevel = getPlayerLevel();
        int currentGold = getPlayerGold();

        // 检查等级是否满足
        if (currentLevel < weapon.getUnlockLevel()) {
            shopOverlay.showToast("等级不足，无法购买！", true);
            return;
        }

        // 检查金币是否足够
        if (currentGold < weapon.getPrice()) {
            shopOverlay.showToast("金币不足！", true);
            return;
        }

        // 扣款
        playerGold -= weapon.getPrice();
        GameData.gold = playerGold;

        // 添加到已购买列表
        int hudIndex = weapon.getHudIndex();
        purchasedItems.add(hudIndex);
        System.out.println("[商城] 解锁塔类型索引=" + hudIndex + " 已解锁=" + purchasedItems.getAll());

        // 更新 HUD 按钮
        hudPanel.updateTowerButtons();

        // 进入放置模式，恢复游戏渲染和逻辑更新
        gameState = GameState.PLAYING;
        placementMode = true;
        pendingWeapon = weapon;
        pendingWeaponIndex = index;
        shopOverlay.setVisible(false);
        shopOverlay.setManaged(false);

        // 显示提示（用 toastOverlay 或直接在 HUD 上显示）
        hudPanel.showToast("点击地图空白区域放置 " + weapon.getName(), false);

        System.out.println("[商城] 进入放置模式: " + weapon.getName() + " 剩余金币=" + playerGold);
        hudPanel.updateState();
    }

    private boolean isTowerAt(int row, int col) {
        for (Tower t : towerList) {
            if (t.getRow() == row && t.getCol() == col) return true;
        }
        return false;
    }

    // ==================== 视觉效果 API ====================

    /** 添加闪电效果 */
    public void addLightningEffect(double startX, double startY, double endX, double endY, Color color) {
        lightningEffects.add(new LightningEffect(startX, startY, endX, endY, color));
    }

    /** 添加爆炸效果 */
    public void addExplosionEffect(double x, double y, Color color) {
        explosionEffects.add(new ExplosionEffect(x, y, color));
    }

    /** 添加伤害飘字 */
    public void addFloatingText(double x, double y, String text, Color color) {
        floatingTexts.add(new FloatingText(x, y, text, color));
    }

    public void loadGame() {
        GameSave.SaveData data = GameSave.load();
        if (data == null) return;

        startGame(1);
        playerGold = data.playerGold;
        playerLives = data.playerLives;
        score = data.score;

        // 同步到 GameData
        GameData.gold = playerGold;
        GameData.hp = playerLives;
        GameData.score = score;

        // 恢复塔
        for (GameSave.TowerSave ts : data.towers) {
            TowerType type = TowerType.valueOf(ts.type);
            Tower tower = createTower(type, ts.row, ts.col);
            if (tower != null) {
                towerList.add(tower);
                Tile tile = gameMap.getTile(ts.row, ts.col);
                if (tile != null) tile.setOccupiedTower(tower);
            }
        }

        hudPanel.updateState();
        System.out.println("[读档] 游戏已加载");
    }

    // ==================== 渲染 ====================

    public void render() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        switch (gameState) {
            case MENU:
            case DIFFICULTY_SELECT:
            case SETTINGS:
            case SHOP:
            case LEADERBOARD:
                // 菜单/难度/设置/商城/排行榜由 JavaFX 组件渲染，Canvas 渲染星空背景
                starfield.render(gc, WIDTH, HEIGHT);
                break;
            case PLAYING:
            case PAUSED:
                renderGame();
                break;
            case GAME_OVER:
                renderGame();
                renderGameOverOverlay();
                break;
            case VICTORY:
                renderGame();
                renderVictoryOverlay();
                break;
        }
    }

    private void renderGame() {
        // 0. 星空背景
        starfield.render(gc, WIDTH, HEIGHT);

        // 1. 地图
        if (gameMap != null) {
            gameMap.render(gc);
        }

        // 2. 投射物
        for (Projectile p : projectileList) {
            p.render(gc);
        }

        // 3. 怪物
        for (Monster m : monsterList) {
            m.render(gc);
        }

        // 4. 防御塔
        for (Tower t : towerList) {
            t.render(gc);
        }

        // 5. 闪电效果
        for (LightningEffect e : lightningEffects) {
            e.render(gc);
        }

        // 6. 爆炸效果
        for (ExplosionEffect e : explosionEffects) {
            e.render(gc);
        }

        // 7. 选中塔的范围圈
        if (showRange && selectedTower != null) {
            selectedTower.renderRangeCircle(gc);
        }

        // 8. 放置预览
        renderPlacementPreview();

        // 9. 伤害飘字
        for (FloatingText t : floatingTexts) {
            t.render(gc);
        }
    }

    private void renderPlacementPreview() {
        // 放置模式预览
        if (placementMode && pendingWeapon != null && gameMap != null) {
            int tileSize = gameMap.getTileSize();
            int col = (int) mouseX / tileSize;
            int row = (int) mouseY / tileSize;

            if (gameMap.isValidTile(row, col)) {
                Tile tile = gameMap.getTile(row, col);
                if (tile != null) {
                    boolean canPlace = tile.getType() == TileType.BUILDABLE && !tile.hasTower();

                    // 检查是否靠近路径
                    boolean nearPath = false;
                    if (canPlace) {
                        for (int dr = -1; dr <= 1 && !nearPath; dr++) {
                            for (int dc = -1; dc <= 1 && !nearPath; dc++) {
                                int nr = row + dr, nc = col + dc;
                                if (nr >= 0 && nr < gameMap.getRows() && nc >= 0 && nc < gameMap.getCols()) {
                                    if (gameMap.getTile(nr, nc).getType() == TileType.PATH) {
                                        nearPath = true;
                                    }
                                }
                            }
                        }
                    }

                    boolean valid = canPlace && nearPath;

                    // 半透明高亮格子
                    Color highlight = valid ? Color.color(0, 1, 0, 0.3) : Color.color(1, 0, 0, 0.3);
                    gc.setFill(highlight);
                    gc.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);

                    // 塔预览圆圈
                    double cx = col * tileSize + tileSize / 2.0;
                    double cy = row * tileSize + tileSize / 2.0;
                    double r = pendingWeapon.getRange() * tileSize;

                    gc.setFill(Color.web(pendingWeapon.getColor(), 0.4));
                    gc.fillOval(cx - 12, cy - 12, 24, 24);

                    gc.setStroke(Color.web(pendingWeapon.getColor(), 0.3));
                    gc.setLineWidth(2);
                    gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
                }
            }
        }

        // 普通选塔模式预览
        if (selectedTowerType != null && gameMap != null && mouseX >= 0) {
            int tileSize = gameMap.getTileSize();
            int col = (int) mouseX / tileSize;
            int row = (int) mouseY / tileSize;

            if (!gameMap.isValidTile(row, col)) return;
            Tile tile = gameMap.getTile(row, col);
            if (tile == null || tile.getType() != TileType.BUILDABLE || tile.hasTower()) return;

            // 半透明绿色高亮格子
            gc.setFill(Color.color(0, 1, 0, 0.25));
            gc.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);

            // 范围预览圈
            int range = getTowerRange(selectedTowerType);
            double cx = col * tileSize + tileSize / 2.0;
            double cy = row * tileSize + tileSize / 2.0;
            double r = range * tileSize;

            gc.setStroke(Color.color(1, 1, 1, 0.3));
            gc.setLineWidth(2);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    private void renderGameOverOverlay() {
        gc.setFill(Color.color(0, 0, 0, 0.85));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.web("#ff6b6b"));
        gc.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 48));
        String msg = "游戏结束";
        double w = gc.getFont().getSize() * msg.length() * 0.55;
        gc.fillText(msg, (WIDTH - w) / 2, HEIGHT / 2 - 20);

        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Microsoft YaHei", 20));
        String scoreMsg = "最终得分: " + score;
        double sw = gc.getFont().getSize() * scoreMsg.length() * 0.55;
        gc.fillText(scoreMsg, (WIDTH - sw) / 2, HEIGHT / 2 + 30);
    }

    private void renderVictoryOverlay() {
        gc.setFill(Color.color(0, 0, 0, 0.85));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.web("#69db7c"));
        gc.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 48));
        String msg = "胜利！";
        double w = gc.getFont().getSize() * msg.length() * 0.55;
        gc.fillText(msg, (WIDTH - w) / 2, HEIGHT / 2 - 20);

        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Microsoft YaHei", 20));
        String scoreMsg = "最终得分: " + score;
        double sw = gc.getFont().getSize() * scoreMsg.length() * 0.55;
        gc.fillText(scoreMsg, (WIDTH - sw) / 2, HEIGHT / 2 + 30);
    }

    // ==================== 鼠标交互 ====================

    private void handleClick(MouseEvent e) {
        // DIFFICULTY_SELECT 和 SETTINGS 由 JavaFX 组件处理点击，无需 Canvas 点击检测

        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            // 同步数据到 GameData
            GameData.hp = playerLives;
            GameData.gold = playerGold;
            GameData.exp = playerXP;
            GameData.level = playerLevel;
            GameData.score = score;

            gameState = GameState.MENU;
            mainMenu.refreshUI();
            mainMenu.show();
            // 隐藏 HUD
            hudPanel.setVisible(false);
            hudPanel.setManaged(false);
            hudPanel.updateState();
            return;
        }

        double x = e.getX();
        double y = e.getY();

        // HUD 区域点击由 HudPanel 处理
        if (y > HEIGHT) return;

        // 地图点击
        handleMapClick(x, y);
    }

    private void handleMapClick(double x, double y) {
        if (gameMap == null) return;

        int tileSize = gameMap.getTileSize();
        int col = (int) x / tileSize;
        int row = (int) y / tileSize;

        if (!gameMap.isValidTile(row, col)) return;

        Tile tile = gameMap.getTile(row, col);
        if (tile == null) return;

        // 放置模式：点击空白区域放置已购买的塔
        if (placementMode && pendingWeapon != null) {
            if (tile.getType() == TileType.BUILDABLE && !tile.hasTower()) {
                // 检查是否靠近路径
                boolean nearPath = false;
                for (int dr = -1; dr <= 1 && !nearPath; dr++) {
                    for (int dc = -1; dc <= 1 && !nearPath; dc++) {
                        int nr = row + dr, nc = col + dc;
                        if (nr >= 0 && nr < gameMap.getRows() && nc >= 0 && nc < gameMap.getCols()) {
                            if (gameMap.getTile(nr, nc).getType() == TileType.PATH) {
                                nearPath = true;
                            }
                        }
                    }
                }
                if (nearPath) {
                    buildPendingTower(row, col);
                    return;
                } else {
                    shopOverlay.showToast("必须靠近路径放置！", true);
                    return;
                }
            } else if (tile.hasTower()) {
                shopOverlay.showToast("该位置已有防御塔！", true);
                return;
            }
            return;
        }

        // 尝试建造塔
        if (selectedTowerType != null
                && tile.getType() == TileType.BUILDABLE
                && !tile.hasTower()) {
            placeTower(selectedTowerType, row, col);
            return;
        }

        // 尝试选中已有塔
        if (tile.hasTower()) {
            selectedTower = tile.getOccupiedTower();
            showRange = true;
            selectedTowerType = null;
            hudPanel.updateState();
        } else {
            selectedTower = null;
            showRange = false;
            hudPanel.updateState();
        }
    }

    // ==================== 塔操作 ====================

    private void placeTower(TowerType type, int row, int col) {
        int cost = getTowerCost(type);
        if (playerGold < cost) {
            System.out.println("[建造失败] 金币不足: 需要=" + cost + " 当前=" + playerGold);
            return;
        }

        Tile tile = gameMap.getTile(row, col);
        if (tile == null || tile.getType() != TileType.BUILDABLE || tile.hasTower()) {
            System.out.println("[建造失败] 格子不可建造: row=" + row + " col=" + col);
            return;
        }

        Tower tower = createTower(type, row, col);
        if (tower != null) {
            playerGold -= cost;
            GameData.gold = playerGold;
            tile.setOccupiedTower(tower);
            towerList.add(tower);
            System.out.println("[建造成功] 类型=" + type + " 位置=(" + row + "," + col + ")"
                + " 花费=" + cost + " 剩余金币=" + playerGold);
            hudPanel.updateState();
        }
    }

    /** 在放置模式下建造已购买的塔 */
    private void buildPendingTower(int row, int col) {
        if (pendingWeapon == null) return;

        Tile tile = gameMap.getTile(row, col);
        if (tile == null || tile.getType() != TileType.BUILDABLE || tile.hasTower()) {
            return;
        }

        Tower tower = createTower(pendingWeapon.toTowerType(), row, col);
        if (tower != null) {
            tile.setOccupiedTower(tower);
            towerList.add(tower);
            System.out.println("[商城放置] 建造: " + pendingWeapon.getName() + " 位置=(" + row + "," + col + ")");
        }

        // 退出放置模式
        placementMode = false;
        pendingWeapon = null;
        pendingWeaponIndex = -1;
        hudPanel.updateState();
        hudPanel.showToast("防御塔放置成功！", false);
    }

    /** 取消放置模式 */
    public void cancelPlacement() {
        if (placementMode && pendingWeapon != null) {
            // 退还金币
            playerGold += pendingWeapon.getPrice();
            GameData.gold = playerGold;
            System.out.println("[商城] 取消放置，退还金币: " + pendingWeapon.getPrice());
            placementMode = false;
            pendingWeapon = null;
            pendingWeaponIndex = -1;
            hudPanel.updateState();
            hudPanel.showToast("已取消购买，金币已退还", false);
        }
    }

    public void sellTower(Tower tower) {
        int sellValue = tower.getSellValue();
        playerGold += sellValue;
        GameData.gold = playerGold;
        Tile tile = gameMap.getTile(tower.getRow(), tower.getCol());
        if (tile != null) {
            tile.setOccupiedTower(null);
        }
        towerList.remove(tower);
        selectedTower = null;
        showRange = false;
        System.out.println("[出售塔] 类型=" + tower.getType() + " Lv." + tower.getLevel()
            + " 获得金币=" + sellValue + " 当前金币=" + playerGold);
        hudPanel.updateState();
    }

    public void upgradeTower(Tower tower) {
        int upgradeCost = tower.getUpgradeCost();
        if (playerGold < upgradeCost) {
            System.out.println("[升级失败] 金币不足: 需要=" + upgradeCost + " 当前=" + playerGold);
            return;
        }
        if (!tower.canUpgrade()) {
            System.out.println("[升级失败] 已达最高等级: " + tower.getType() + " Lv." + tower.getLevel());
            return;
        }

        playerGold -= upgradeCost;
        GameData.gold = playerGold;
        int oldLevel = tower.getLevel();
        tower.upgrade();
        System.out.println("[升级成功] " + tower.getType() + " Lv." + oldLevel + " -> Lv." + tower.getLevel()
            + " 花费=" + upgradeCost + " 剩余金币=" + playerGold);
        hudPanel.updateState();
    }

    public void selectTowerType(TowerType type) {
        // 检查等级是否解锁
        int[] unlockLevels = {1, 2, 5, 3, 5, 8}; // 箭塔、炮塔、魔法塔、减速塔、闪电塔、核弹塔
        int typeIndex = type.ordinal();
        if (playerLevel < unlockLevels[typeIndex]) {
            System.out.println("[解锁] " + type + " 需要 LV." + unlockLevels[typeIndex] + "，当前 LV." + playerLevel);
            return;
        }

        selectedTowerType = (selectedTowerType == type) ? null : type;
        selectedTower = null;
        showRange = false;
        hudPanel.updateState();
    }

    // ==================== 工厂方法 ====================

    private Tower createTower(TowerType type, int row, int col) {
        int tileSize = gameMap.getTileSize();
        int pixelX = col * tileSize + tileSize / 2;
        int pixelY = row * tileSize + tileSize / 2;

        Tower tower = null;
        switch (type) {
            case ARROW:     tower = new ArrowTower(row, col, pixelX, pixelY); break;
            case CANNON:    tower = new CannonTower(row, col, pixelX, pixelY); break;
            case MAGIC:     tower = new MagicTower(row, col, pixelX, pixelY); break;
            case SLOW:      tower = new SlowTower(row, col, pixelX, pixelY); break;
            case LIGHTNING: tower = new LightningTower(row, col, pixelX, pixelY); break;
            case NUKE:      tower = new NukeTower(row, col, pixelX, pixelY); break;
            default:        return null;
        }

        // 设置 GamePanel 引用，用于触发视觉效果
        if (tower != null) {
            tower.setGamePanel(this);
        }
        return tower;
    }

    private int getTowerCost(TowerType type) {
        switch (type) {
            case ARROW:     return 50;
            case CANNON:    return 100;
            case MAGIC:     return 150;
            case SLOW:      return 150;
            case LIGHTNING: return 260;
            case NUKE:      return 500;
            default:        return Integer.MAX_VALUE;
        }
    }

    private int getTowerRange(TowerType type) {
        switch (type) {
            case ARROW:     return 3;
            case CANNON:    return 2;
            case MAGIC:     return 3;
            case SLOW:      return 2;
            case LIGHTNING: return 3;
            case NUKE:      return 5;
            default:        return 2;
        }
    }

    // ==================== 键盘操作 ====================

    public void cancelSelection() {
        selectedTowerType = null;
        selectedTower = null;
        showRange = false;
        hudPanel.updateState();
    }

    public void quickSelectTower(int index) {
        TowerType[] types = TowerType.values();
        if (index >= 0 && index < types.length) {
            selectTowerType(types[index]);
        }
    }

    /** 获取塔的解锁等级 */
    public int getTowerUnlockLevel(int towerIndex) {
        int[] unlockLevels = {1, 2, 5, 3, 5, 8}; // 箭塔、炮塔、魔法塔、减速塔、闪电塔、核弹塔
        if (towerIndex >= 0 && towerIndex < unlockLevels.length) {
            return unlockLevels[towerIndex];
        }
        return 99;
    }
}
