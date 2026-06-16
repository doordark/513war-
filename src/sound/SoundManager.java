package sound;

import javafx.scene.media.AudioClip;

/**
 * 音效管理类（单例）—— 管理 BGM 和 SFX。
 * 音效文件放在 resources/sounds/ 目录下。
 */
public class SoundManager {

    private static SoundManager instance;

    // BGM（用 AudioClip + 手动循环，避免 MediaPlayer mp3 兼容性问题）
    private AudioClip bgmSound;
    private boolean bgmLooping = false;

    // 怪物出场音效
    private AudioClip normalMonsterSound; // 蚊子声（NormalMonster 专用）
    private AudioClip otherMonsterSound;  // 爬虫声（其他怪物通用）

    // 防御塔音效
    private AudioClip arrowTowerSound;    // 滴水声（箭塔）
    private AudioClip cannonTowerSound;   // 吹风声（炮塔+减速塔）
    private AudioClip lightningTowerSound; // 电击声（闪电塔）
    private AudioClip slowTowerSound;     // 吹风声（减速塔，与炮塔共用）
    private AudioClip magicTowerSound;    // 预留
    private AudioClip nukeTowerSound;     // 预留

    // 音量 (0.0 - 1.0)
    private double bgmVolume = 0.5;
    private double sfxVolume = 0.7;

    private SoundManager() {
        // 直接在 JavaFX 应用线程上加载（GameMain.start() 已在该线程）
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /** 重新加载（用于重置单例） */
    public static void reset() {
        if (instance != null && instance.bgmSound != null) {
            instance.bgmSound.stop();
        }
        instance = null;
    }

    private void loadSounds() {
        // ===== BGM =====
        try {
            String bgmPath = getClass().getResource("/sounds/bgm.mp3").toExternalForm();
            bgmSound = new AudioClip(bgmPath);
            System.out.println("[音效] BGM 已加载: " + bgmPath);
        } catch (Exception e) {
            System.out.println("[音效] BGM 加载失败: " + e.getMessage());
            bgmSound = null;
        }

        // ===== 怪物出场音效 =====
        try {
            normalMonsterSound = new AudioClip(getClass().getResource("/sounds/蚊子声.mp3").toExternalForm());
        } catch (Exception e) {
            System.out.println("[音效] 蚊子声加载失败: " + e.getMessage());
            normalMonsterSound = null;
        }

        try {
            otherMonsterSound = new AudioClip(getClass().getResource("/sounds/爬虫声.mp3").toExternalForm());
        } catch (Exception e) {
            System.out.println("[音效] 爬虫声加载失败: " + e.getMessage());
            otherMonsterSound = null;
        }

        // ===== 防御塔音效 =====
        try {
            arrowTowerSound = new AudioClip(getClass().getResource("/sounds/滴水声.mp3").toExternalForm());
        } catch (Exception e) {
            System.out.println("[音效] 滴水声加载失败: " + e.getMessage());
            arrowTowerSound = null;
        }

        try {
            cannonTowerSound = new AudioClip(getClass().getResource("/sounds/吹风声.mp3").toExternalForm());
            slowTowerSound = cannonTowerSound; // 炮塔和减速塔共用吹风声
        } catch (Exception e) {
            System.out.println("[音效] 吹风声加载失败: " + e.getMessage());
            cannonTowerSound = null;
            slowTowerSound = null;
        }

        try {
            lightningTowerSound = new AudioClip(getClass().getResource("/sounds/电击声.mp3").toExternalForm());
        } catch (Exception e) {
            System.out.println("[音效] 电击声加载失败: " + e.getMessage());
            lightningTowerSound = null;
        }

        // magicTowerSound 和 nukeTowerSound 暂时留空
    }

    // ==================== BGM 控制 ====================

    public void playBGM() {
        if (bgmSound != null) {
            bgmSound.setVolume(bgmVolume);
            bgmLooping = true;
            bgmSound.play();
            System.out.println("[音效] BGM 开始播放");
        } else {
            System.out.println("[音效] BGM 音效未加载");
        }
    }

    public void stopBGM() {
        if (bgmSound != null) {
            bgmSound.stop();
            bgmLooping = false;
            System.out.println("[音效] BGM 已停止");
        }
    }

    public void pauseBGM() {
        if (bgmSound != null) {
            bgmSound.stop();
        }
    }

    public void resumeBGM() {
        if (bgmSound != null) {
            bgmSound.setVolume(bgmVolume);
            bgmSound.play();
        }
    }

    public boolean isBGMPlaying() {
        return bgmSound != null && bgmSound.isPlaying();
    }

    /** 检查 BGM 是否需要循环重启（由 GameEngine 每帧调用） */
    public void updateBGM() {
        if (bgmLooping && bgmSound != null && !bgmSound.isPlaying()) {
            bgmSound.play();
        }
    }

    // ==================== 怪物出场音效 ====================

    /** 播放怪物出场音效（根据类型自动选择） */
    public void playMonsterSpawn(String monsterType) {
        if ("NORMAL".equals(monsterType)) {
            playNormalMonsterSpawn();
        } else {
            playOtherMonsterSpawn();
        }
    }

    public void playNormalMonsterSpawn() {
        if (normalMonsterSound != null) {
            normalMonsterSound.setVolume(sfxVolume);
            normalMonsterSound.play();
        }
    }

    public void playOtherMonsterSpawn() {
        if (otherMonsterSound != null) {
            otherMonsterSound.setVolume(sfxVolume);
            otherMonsterSound.play();
        }
    }

    // ==================== 防御塔音效 ====================

    /** 根据塔类型播放开火音效 */
    public void playTowerFire(String towerType) {
        switch (towerType) {
            case "ARROW":     playArrowTower(); break;
            case "CANNON":    playCannonTower(); break;
            case "SLOW":      playSlowTower(); break;
            case "LIGHTNING": playLightningTower(); break;
            case "MAGIC":     playMagicTower(); break;
            case "NUKE":      playNukeTower(); break;
        }
    }

    public void playArrowTower() {
        if (arrowTowerSound != null) {
            arrowTowerSound.setVolume(sfxVolume);
            arrowTowerSound.play();
        }
    }

    public void playCannonTower() {
        if (cannonTowerSound != null) {
            cannonTowerSound.setVolume(sfxVolume);
            cannonTowerSound.play();
        }
    }

    public void playMagicTower() {
        // TODO: 预留魔法塔音效
        if (magicTowerSound != null) {
            magicTowerSound.setVolume(sfxVolume);
            magicTowerSound.play();
        }
    }

    public void playSlowTower() {
        if (slowTowerSound != null) {
            slowTowerSound.setVolume(sfxVolume);
            slowTowerSound.play();
        }
    }

    public void playLightningTower() {
        if (lightningTowerSound != null) {
            lightningTowerSound.setVolume(sfxVolume);
            lightningTowerSound.play();
        }
    }

    public void playNukeTower() {
        // TODO: 预留核弹塔音效
        if (nukeTowerSound != null) {
            nukeTowerSound.setVolume(sfxVolume);
            nukeTowerSound.play();
        }
    }

    // ==================== 音量控制 ====================

    public void setBgmVolume(double v) {
        this.bgmVolume = Math.max(0.0, Math.min(1.0, v));
        if (bgmSound != null) {
            bgmSound.setVolume(this.bgmVolume);
        }
    }

    public void setSfxVolume(double v) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, v));
    }

    public double getBgmVolume() { return bgmVolume; }
    public double getSfxVolume() { return sfxVolume; }

    /** 停止所有音效 */
    public void stopAll() {
        stopBGM();
    }
}
