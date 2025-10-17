// ScoreDaoImpl.java

package edu.hitsz.data;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 得分数据访问对象实现类（DAO Implementation）
 * 使用文件序列化存储数据
 */
public class ScoreDaoImpl implements ScoreDao {
    private final String dataFilePath = "score_records.csv";
    private List<ScoreRecord> scoreRecords;
    // 用于解析和格式化时间的格式
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public ScoreDaoImpl() {
        // 初始化时，从文件中加载数据
        this.scoreRecords = loadFromFile();
    }

    @Override
    public List<ScoreRecord> getAllScores() {
        // 返回一个排序后的副本
        List<ScoreRecord> sortedScores = new LinkedList<>(this.scoreRecords);
        Collections.sort(sortedScores); // 默认按 ScoreRecord 的 compareTo 方法降序排序
        return sortedScores;
    }

    @Override
    public void addScore(ScoreRecord record) {
        this.scoreRecords.add(record);
        saveToFile(); // 添加后保存到文件
    }

    @Override
    public void clearScores() {
        this.scoreRecords.clear();
        saveToFile(); // 清空后保存到文件
    }

    @Override
    public void printScores(List<ScoreRecord> scoreRecords) {
        System.out.println("**************************************************");
        System.out.println("********************** 得分排行榜 **********************");
        System.out.println("**************************************************");

        for (int i = 0; i < scoreRecords.size(); i++) {
            ScoreRecord record = scoreRecords.get(i);
            System.out.printf("第%d名: %s\n", i + 1, record.toString());
        }
        System.out.println("**************************************************");
    }

    /**
     * 将当前得分记录列表写入 CSV 文件
     */
    private void saveToFile() {
        // 使用 FileWriter 和 BufferedWriter 写入文本文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFilePath))) {
            for (ScoreRecord record : this.scoreRecords) {
                // 格式：UserName,Score,LocalDateTime
                String line = record.getUserName() + "," +
                        record.getScore() + "," +
                        record.getRecordTime().format(formatter); // 使用固定格式时间
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存得分记录失败: " + e.getMessage());
        }
    }

    /**
     * 从 CSV 文件加载得分记录列表
     */
    private List<ScoreRecord> loadFromFile() {
        List<ScoreRecord> loadedList = new LinkedList<>();
        File file = new File(dataFilePath);

        if (file.exists() && file.length() > 0) {
            // 使用 FileReader 和 BufferedReader 读取文本文件
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue; // 跳过空行
                    }
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        try {
                            String userName = parts[0];
                            int score = Integer.parseInt(parts[1].trim());
                            // 注意：这里需要使用固定的格式解析时间
                            LocalDateTime recordTime = LocalDateTime.parse(parts[2].trim(), formatter);

                            loadedList.add(new ScoreRecord(userName, score, recordTime));
                        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                            System.err.println("解析行数据失败，跳过: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("加载得分记录失败，创建新的列表: " + e.getMessage());
            }
        }
        return loadedList;
    }
}
