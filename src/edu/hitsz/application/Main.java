package edu.hitsz.application;

import edu.hitsz.data.ScoreDao;
import edu.hitsz.data.ScoreDaoImpl;
import javax.swing.*;
import java.awt.*;
import edu.hitsz.application.SoundManager;
import edu.hitsz.panel.MenuPanel;
import edu.hitsz.panel.RankPanel;

/**
 * 程序入口，作为主框架 (JFrame) 管理 CardLayout 切换
 */
public class Main extends JFrame {

    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 768;

    // CardLayout 的卡片名称
    public static final String MENU_CARD = "MENU";
    public static final String GAME_CARD = "GAME";
    public static final String RANK_CARD = "RANK";

    private int selectedDifficulty = 1; // 默认简单 (1:EASY, 2:NORMAL, 3:HARD)
    private boolean isSoundOn = true;   // 默认开启

    // SoundManager 实例
    private SoundManager soundManager;

    private JPanel mainPanel;
    private CardLayout cardLayout;

    // 界面实例
    private MenuPanel menuPanel;
    private Game gamePanel; // 游戏界面
    private RankPanel rankPanel; // 排行榜界面

    // DAO 实例
    private final ScoreDao scoreDao = new ScoreDaoImpl();

    public Main() {
        setTitle("飞机大战");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        soundManager = new SoundManager();
        // 实例化界面
        menuPanel = new MenuPanel(this);
        // gamePanel 在切换到游戏时再实例化，方便重置游戏状态
        // rankPanel 实例化
        rankPanel = new RankPanel(this, scoreDao);

        // 添加卡片
        mainPanel.add(menuPanel, MENU_CARD);
        // 游戏面板在启动时不需要添加，因为它每次启动都需要重新创建
        mainPanel.add(rankPanel, RANK_CARD);

        this.add(mainPanel);

        // 初始显示菜单
        cardLayout.show(mainPanel, MENU_CARD);
        setVisible(true);
    }

    /**
     * 获取当前游戏实例
     * @return 当前游戏实例，如果游戏未启动则返回null
     */
    public Game getGame() {
        return gamePanel;
    }

    /**
     * 获取当前游戏是否正在运行
     * @return true如果游戏正在运行，否则false
     */
    public boolean isGameRunning() {
        return gamePanel != null && !gamePanel.isGameOver();
    }

    // 设置难度和音效的方法，供 MenuPanel 调用
    public void setGameConfig(int difficulty, boolean soundOn) {
        this.selectedDifficulty = difficulty;
        this.isSoundOn = soundOn;
    }

    /**
     * 公共方法：切换界面
     * @param cardName 目标卡片的名称 (MENU_CARD, GAME_CARD, RANK_CARD)
     */
    public void switchTo(String cardName) {
        if (GAME_CARD.equals(cardName)) {
            // 停止所有可能在播放的音乐，特别是 Boss 音乐和菜单音乐
            soundManager.stopAll();
            soundManager.setSoundOn(this.isSoundOn); // 设置音效开关

            // 如果要启动游戏，先移除旧的游戏面板（如果有），再创建并添加新的
            if (gamePanel != null) {
                mainPanel.remove(gamePanel);
            }
            // TODO: 在这里根据 MenuPanel 传递的难度和音效参数创建 Game 实例
            // 暂时使用默认构造函数
            gamePanel = new Game(this, this.selectedDifficulty); // 传入 MainFrame 引用
            mainPanel.add(gamePanel, GAME_CARD);

            // 切换到游戏卡片
            cardLayout.show(mainPanel, GAME_CARD);
            // 启动游戏逻辑
            gamePanel.action();

            // 游戏开始播放背景音乐 (如果音效开启)
            soundManager.playBgm();

        } else if (RANK_CARD.equals(cardName)) {
            // 切换到排行榜时，刷新数据
            soundManager.stopAll();
            rankPanel.loadRankingData();
            cardLayout.show(mainPanel, RANK_CARD);
        } else if (MENU_CARD.equals(cardName)) {
            // 切换回菜单：停止所有音乐
            soundManager.stopAll();
            cardLayout.show(mainPanel, MENU_CARD);
        }
    }

    public ScoreDao getScoreDao() {
        return scoreDao;
    }

    // SoundManager Getter
    public SoundManager getSoundManager() {
        return soundManager;
    }

    public static void main(String[] args) {
        // Swing GUI 线程安全启动
        SwingUtilities.invokeLater(Main::new);
    }
}