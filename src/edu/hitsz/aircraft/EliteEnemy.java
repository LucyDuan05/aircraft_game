package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 精英敌机
 * 拥有射击能力，能周期性产生敌机，坠毁后能掉落道具
 * @author hitsz
 */
public class EliteEnemy extends AbstractAircraft {

//    private int shootNum = 1;
    private int power = 10;
    private int direction = 1; // 精英机子弹向下飞行

    // 射击和生成敌机的周期计时器
    private int shootInterval = 600; // 射击周期，毫秒
    // private int spawnInterval = 10000; // 生成敌机周期，毫秒
    private int shootTimer = 0;
    // private int spawnTimer = 0;
    private Random random = new Random();

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        // 更新计时器
        shootTimer += 40;
        // spawnTimer += 40;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        // 检查射击计时器是否归零
        if (shootTimer >= shootInterval) {
            shootTimer = 0; // 重置计时器
            int x = this.getLocationX();
            int y = this.getLocationY() + this.direction * 2;
            int speedY = this.getSpeedY() + this.direction * 5;
            BaseBullet bullet = new EnemyBullet(x, y, 0, speedY, this.power);
            res.add(bullet);
        }
        return res;
    }
}