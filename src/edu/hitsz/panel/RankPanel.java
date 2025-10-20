package edu.hitsz.panel;

import edu.hitsz.application.Main;
import edu.hitsz.data.ScoreDao;
import edu.hitsz.data.ScoreRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.*; // 引入 AWT 包以使用布局和流布局

public class RankPanel extends JPanel {
    private JPanel mainPanel;
    private JTable rankTable;
    private JButton clearButton;
    private JButton returnButton;
    private JScrollPane tableContainer;

    private Main mainFrame;
    private ScoreDao scoreDao;
    private DefaultTableModel tableModel;
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    // === 新增：用于存储当前表格对应的数据列表 ===
    private List<ScoreRecord> currentScoreRecords;

    public RankPanel(Main mainFrame, ScoreDao scoreDao) {
        this.mainFrame = mainFrame;
        this.scoreDao = scoreDao;

        // --- 1. 初始化表格模型（仿照您的参考逻辑）---
        String[] columnNames = {"排名", "玩家姓名", "得分", "记录时间"};

        // 创建表格模型，并重写 isCellEditable 使其不可编辑
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 将手动创建的模型设置给图形化工具生成的 JTable 组件
        if (rankTable != null) {
            rankTable.setModel(tableModel);
            // 确保 JTable 在 JScrollPane 中显示，防止图形化工具配置遗漏
            if (tableContainer != null) {
                tableContainer.setViewportView(rankTable);
            }
        } else {
            System.err.println("RankPanel: JTable component 'rankTable' was not initialized by the GUI builder!");
        }

        // --- 2. 返回按钮监听 ---
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.switchTo(Main.MENU_CARD); // 切换回菜单
            }
        });

        // --- 3. 清除记录按钮监听 ---
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = rankTable.getSelectedRow();

                if (selectedRow == -1) {
                    // ... (提示代码)
                    return;
                }

                int result = JOptionPane.showConfirmDialog(mainPanel,
                        "是否确定删除选中的记录？", "确认删除", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    // 核心修改：直接将 UI 索引传递给 DAO
                    // 优势：避免了从 currentScoreRecords 中取对象后再传给 DAO 造成的引用/精度匹配问题。
                    scoreDao.removeScoreByIndex(selectedRow);

                    // 刷新表格显示
                    loadRankingData();

                    // 修复：删除后重置选中状态，避免多次删除同一行时选中索引不变
                    rankTable.clearSelection();
                }
            }
        });

        // 4. 将主面板添加到此 JPanel 中（确保布局切换正常）
        // 假设图形化工具创建的主容器是 mainPanel，并且其内部包含了 titleLabel, tableContainer, 和两个按钮
        this.setLayout(new BorderLayout()); // 假设 RankPanel 自身使用 BorderLayout
        this.add(mainPanel, BorderLayout.CENTER);

        this.setLayout(new BorderLayout()); // 设置 MenuPanel 自身的布局
        this.add(this.mainPanel, BorderLayout.CENTER); // 将所有内容添加到 MenuPanel 中
    }

    /**
     * 核心方法：加载和更新排行榜数据
     */
    public void loadRankingData() {
        if (tableModel == null) {
            // 如果模型为空，可能是在初始化时出错了
            return;
        }

        // 1. 清空旧数据
        tableModel.setRowCount(0);

        // 2. 从 DAO 获取排序后的数据
        List<ScoreRecord> scores = scoreDao.getAllScores();

        // 3. 填充表格模型
        int rank = 1;
        for (ScoreRecord record : scores) {
            tableModel.addRow(new Object[]{
                    rank++,
                    record.getUserName(),
                    record.getScore(),
                    record.getRecordTime().format(displayFormatter)
            });
        }

        // 4. 通知表格刷新
        tableModel.fireTableDataChanged();
    }
}
