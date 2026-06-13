package sound;

import javafx.scene.media.AudioClip;

/**
 * 音效管理类，负责加载和播放游戏中的各种音效。
 * 音效文件应放在 resources/audio/ 目录下。
 */
public class SoundManager {

    // 音效文件路径（相对于 resources 目录）
    private static final String AUDIO_PATH = "/audio/";

    // 音效实例
    private AudioClip placeTowerSound;
    private AudioClip shootSound;
    private AudioClip explosionSound;
    private AudioClip gameOverSound;

    // 音量控制 (0.0 - 1.0)
    private double volume = 1.0;

    public SoundManager() {
        loadSounds();
    }

    /**
     * 加载所有音效文件。
     * 支持 wav 和 mp3 格式。
     */
    private void loadSounds() {
        placeTowerSound = new AudioClip(getClass().getResource(AUDIO_PATH + "place_tower.wav").toExternalForm());
        shootSound = new AudioClip(getClass().getResource(AUDIO_PATH + "shoot.wav").toExternalForm());
        explosionSound = new AudioClip(getClass().getResource(AUDIO_PATH + "explosion.wav").toExternalForm());
        gameOverSound = new AudioClip(getClass().getResource(AUDIO_PATH + "game_over.mp3").toExternalForm());
    }

    /**
     * 播放建造防御塔时的音效。
     */
    public void playPlaceTower() {
        if (placeTowerSound != null) {
            placeTowerSound.setVolume(volume);
            placeTowerSound.play();
        }
    }

    /**
     * 播放箭塔或炮塔开火的音效。
     */
    public void playShoot() {
        if (shootSound != null) {
            shootSound.setVolume(volume);
            shootSound.play();
        }
    }

    /**
     * 播放炮塔子弹爆炸的音效。
     */
    public void playExplosion() {
        if (explosionSound != null) {
            explosionSound.setVolume(volume);
            explosionSound.play();
        }
    }

    /**
     * 播放游戏失败或胜利的音效。
     */
    public void playGameOver() {
        if (gameOverSound != null) {
            gameOverSound.setVolume(volume);
            gameOverSound.play();
        }
    }

    /**
     * 设置全局音量。
     * @param volume 音量值，范围 0.0（静音）到 1.0（最大）
     */
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    /**
     * 获取当前音量。
     * @return 当前音量值
     */
    public double getVolume() {
        return volume;
    }

    /**
     * 停止所有正在播放的音效。
     */
    public void stopAll() {
        if (placeTowerSound != null) placeTowerSound.stop();
        if (shootSound != null) shootSound.stop();
        if (explosionSound != null) explosionSound.stop();
        if (gameOverSound != null) gameOverSound.stop();
    }
}
