package edu.hitsz.application;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
// 导入 ConcurrentHashMap
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 音效管理器：使用 Clip 接口实现单线程逻辑的异步播放控制。
 * Clip 适合用于较短的音效和BGM的循环播放。
 *
 */
public class SoundManager {

    private boolean soundOn = true;

    // 音效文件路径
    public static final String BGM_PATH = "videos/bgm.wav";
    public static final String BOSS_BGM_PATH = "videos/bgm_boss.wav";
    public static final String BOMB_PATH = "videos/bomb_explosion.wav";
    public static final String PROP_PATH = "videos/get_supply.wav";
    public static final String GAME_OVER_PATH = "videos/game_over.wav";
    public static final String BULLET_HIT_PATH = "videos/bullet_hit.wav";
    public static final String BULLET_PATH = "videos/bullet.wav";

    // 存储所有预加载的 Clip，以避免每次播放都进行 IO
    // 1.使用 ConcurrentHashMap 保证线程安全
    private final Map<String, Clip> audioClips = new ConcurrentHashMap<>();

    private Clip bgmClip;
    private Clip bossBgmClip;

    public SoundManager() {
        // 预加载所有循环音乐和常用单次音效
        bgmClip = loadClip(BGM_PATH);
        bossBgmClip = loadClip(BOSS_BGM_PATH);
        // 预加载单次音效（可选，但推荐）
//        audioClips.put(BOMB_PATH, loadClip(BOMB_PATH));
//        audioClips.put(PROP_PATH, loadClip(PROP_PATH));
        audioClips.put(GAME_OVER_PATH, loadClip(GAME_OVER_PATH));
        // audioClips.put(BULLET_HIT_PATH, loadClip(BULLET_HIT_PATH)); // 击中音效频繁，可以选择不预加载以节省内存
    }

    /**
     * 核心方法：加载音频文件到 Clip
     */
    private Clip loadClip(String filename) {
        try (InputStream inputStream = SoundManager.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                System.err.println("Error: Music file not found in resources: " + filename);
                return null;
            }

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                return clip;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading audio file: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    // 2. 添加 synchronized
    public synchronized void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
        if (!soundOn) {
            stopAll();
        }
    }

    /** 播放游戏背景音乐 (循环) */
    // 3. 添加 synchronized
    public synchronized void playBgm() {
        if (!soundOn || bgmClip == null) return;
        stopBgm(); // 确保旧的停止
        bgmClip.setFramePosition(0); // 从头开始播放
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // 循环播放
    }

    /** 播放 Boss 背景音乐 (循环) */
    // 4. 添加 synchronized
    public synchronized void playBossBgm() {
        if (!soundOn || bossBgmClip == null) return;
        stopBossBgm(); // 确保旧的停止
        bossBgmClip.setFramePosition(0);
        bossBgmClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /** 播放单次音效 */
    // 5. 添加 synchronized
    public synchronized void playSound(String soundPath) {
        if (!soundOn) return;

        Clip clip;
        // 【修改点: 仅对不需要叠加的音效使用预加载的 Clip】
        // 将需要叠加的音效（PROP_PATH, BOMB_PATH）排除在外
        if (soundPath.equals(PROP_PATH) || soundPath.equals(BOMB_PATH)) {
            // 对于需要叠加的音效，每次都临时加载一个新的 Clip
            clip = loadClip(soundPath);
        } else if (audioClips.containsKey(soundPath)) {
            // 对于预加载的、不需要叠加的音效（如 GAME_OVER_PATH），使用缓存的 Clip
            clip = audioClips.get(soundPath);
        } else {
            // 对于不常用的音效（例如击中），临时加载并播放
            clip = loadClip(soundPath);
        }

        if (clip != null) {
            // 确保从头开始播放
            clip.setFramePosition(0);
            clip.start();

            // 只有临时加载的 Clip（即：不在 audioClips 中的）才需要监听器来关闭
            // 【修改点 C: 重新定义“临时 Clip”的判断条件】
            if (!audioClips.containsKey(soundPath) || soundPath.equals(PROP_PATH) || soundPath.equals(BOMB_PATH)) {

                // 确保 clip 在播放完毕后关闭并释放资源
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        // 只有当播放头达到末尾（即自然播放完毕）时才关闭
                        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {
                            clip.close();
                        }
                    }
                });
            }
        }
    }

    /** 停止游戏背景音乐 */
    // 6. 添加 synchronized
    public synchronized void stopBgm() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    /** 停止 Boss 背景音乐 */
    // 7. 添加 synchronized
    public synchronized void stopBossBgm() {
        if (bossBgmClip != null && bossBgmClip.isRunning()) {
            bossBgmClip.stop();
        }
    }

    /** 停止所有循环音乐 */
    // 修复：添加 synchronized
    public synchronized void stopAll() {
        stopBgm();
        stopBossBgm();
        // 不影响单次音效（它们播完自己会停止）
    }
}