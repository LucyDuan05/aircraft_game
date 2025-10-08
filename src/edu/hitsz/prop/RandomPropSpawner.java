package edu.hitsz.prop;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomPropSpawner {

    // 存储所有具体工厂
    private final List<PropFactory> factoryList;
    private final Random random = new Random();

    public RandomPropSpawner() {
        // 初始化时，注册所有具体道具工厂实例
        this.factoryList = Arrays.asList(
                new BloodPropFactory(), // 可以显式向下转型
                new FirePropFactory(),
                new BombPropFactory()
        );
    }

    /**
     * 随机选择一个道具工厂来创建道具（假设等概率）
     * @param locationX 道具生成X坐标
     * @param locationY 道具生成Y坐标
     * @return AbstractProp
     */
    public AbstractProp spawnProp(int locationX, int locationY) {
        // 随机选择 0, 1, 或 2
        int index = random.nextInt(factoryList.size());

        // 增加偏移逻辑，为多道具掉落增加随机偏移，防止堆叠
        int offsetX = random.nextInt(40) - 20; // -20 到 19
        int offsetY = random.nextInt(40) - 20;

        // 调用选中的工厂来创建道具
        return factoryList.get(index).createProp(locationX + offsetX, locationY + offsetY);
    }
}