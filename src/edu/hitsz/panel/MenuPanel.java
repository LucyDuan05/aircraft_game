package edu.hitsz.panel;

import edu.hitsz.application.Main;
import edu.hitsz.data.ScoreDao;
import edu.hitsz.data.ScoreDaoImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * 菜单界面：只包含由图形化工具生成的组件和事件监听器
 */
public class MenuPanel extends JPanel { // 继承自 JPanel 作为 CardLayout 的一张卡片

    // =========================================================
    // !!! 这些组件由图形化工具自动生成和初始化，我们不手动实例化 !!!
    // =========================================================
    private JPanel mainPanel;
    private JButton easyButton;
    private JButton normalButton;
    private JButton hardButton;
    private JComboBox soundComboBox;
    private JLabel titleLabel; // 假设有一个标题
    private JLabel soundLabel; // 假设有一个音效文字标签
    // =========================================================

    private Main mainFrame;
    private boolean isSoundOn = true; // 用于跟踪当前选择的状态

    /**
     * MenuPanel 构造函数
     * @param mainFrame 主框架引用，用于切换界面和传递配置
     */
    public MenuPanel(Main mainFrame) {
        this.mainFrame = mainFrame;

        // 确保主面板被添加到这个 JPanel 中（图形化工具可能已完成这步）
        // add(mainPanel);

        // 默认状态初始化
        isSoundOn = true;

        // --- 核心逻辑：难度选择按钮事件监听 ---
        ActionListener difficultyListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int difficulty = 0;
                String command = e.getActionCommand(); // 使用 ActionCommand 区分按钮

                if (command.equals("EASY")) {
                    difficulty = 1;
                } else if (command.equals("NORMAL")) {
                    difficulty = 2;
                } else if (command.equals("HARD")) {
                    difficulty = 3;
                }

                // 1. 存储配置
                mainFrame.setGameConfig(difficulty, isSoundOn);
                System.out.println("TEST: 配置设置 -> 难度:" + difficulty + ", 音效:" + isSoundOn);
                // 2. 切换到游戏界面
                mainFrame.switchTo(Main.GAME_CARD);
            }
        };

        // 设置 ActionCommand 并添加监听器 (假设按钮文字就是 EASY/NORMAL/HARD)
        easyButton.setActionCommand("EASY");
        normalButton.setActionCommand("NORMAL");
        hardButton.setActionCommand("HARD");

        easyButton.addActionListener(difficultyListener);
        normalButton.addActionListener(difficultyListener);
        hardButton.addActionListener(difficultyListener);

        // --- 核心逻辑：音效开关事件监听 ---


        // --- 确保 Panel 的切换功能正常 ---
        this.add(mainPanel); // 确保主面板被添加
        soundComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 使用 getSelectedItem() 来获取用户选择的值
                String selected = (String) soundComboBox.getSelectedItem();

                // 根据选择的内容设置音效开关状态
                if ("开".equals(selected)) {
                    isSoundOn = true;
                } else if ("关".equals(selected)) {
                    isSoundOn = false;
                }

                // 注意：由于 MenuPanel 的难度按钮才是触发游戏启动的地方，
                // 这里只需要更新 isSoundOn 成员变量即可。
                // 在启动游戏时，难度按钮的监听器会使用这个 isSoundOn 的最新值。
                System.out.println("音效状态已更新为: " + (isSoundOn ? "开" : "关"));
            }
        });

        this.setLayout(new BorderLayout()); // 设置 MenuPanel 自身的布局
        this.add(this.mainPanel, BorderLayout.CENTER); // 将所有内容添加到 MenuPanel 中
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

//    public static void main(String[] args) {
//        // -------------------------------------------------------------------
//        // 1. 模拟 MainFrame 的依赖：创建一个用于测试的 MockMainFrame
//        // -------------------------------------------------------------------
//
//        // 由于 MenuPanel 的构造函数需要一个 Main 实例，我们创建一个匿名内部类来模拟它
//        Main mockMain = new Main() {
//            // 重写 MenuPanel 依赖的三个方法，只打印日志，不执行实际的游戏逻辑
//            @Override
//            public void setGameConfig(int difficulty, boolean soundOn) {
//                System.out.println("TEST: 配置设置 -> 难度:" + difficulty + ", 音效:" + soundOn);
//            }
//            @Override
//            public void switchTo(String cardName) {
//                System.out.println("TEST: 请求切换到 -> " + cardName);
//                // 弹出窗口确认
//                JOptionPane.showMessageDialog(this, "成功触发切换逻辑到 " + cardName, "测试成功", JOptionPane.INFORMATION_MESSAGE);
//            }
//            @Override
//            public ScoreDao getScoreDao() {
//                return new ScoreDaoImpl(); // 返回一个实际的 DAO 模拟
//            }
//        };
//
//        // -------------------------------------------------------------------
//        // 2. 启动 JFrame
//        // -------------------------------------------------------------------
//        JFrame frame = new JFrame("菜单面板图形化测试");
//
//        // 实例化 MenuPanel，传入模拟的 Main 实例
//        MenuPanel menuPanel = new MenuPanel(mockMain);
//
//        // 将 MenuPanel (它是一个 JPanel) 的主内容添加到 JFrame 中
//        // 注意：如果是 RankPanel，您可能需要获取它内部的 mainPanel，但 MenuPanel 应该直接继承 JPanel。
//        frame.setContentPane(menuPanel); // MenuPanel 继承 JPanel，可以直接作为内容面板
//
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(512, 768); // 设置合适的大小
//        frame.setLocationRelativeTo(null); // 居中
//        frame.setVisible(true);
//    }
}