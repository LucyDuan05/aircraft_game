package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class SuperEliteEnemy extends AbstractAircraft{
    private int shootNum = 3;
    private int power = 20;
    private int direction = 1;
    private int shootInterval = 600;
    private int shootTimer = 0;

    public SuperEliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        // 调用父类的移动方法，满足边界处理
        super.forward();
        // 更新计时器
        shootTimer += 40;
    }

    @Override   // 重写子弹发射方法，更新超级精英机的射击逻辑
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        if (shootTimer >= shootInterval) {
            shootTimer = 0; // 重置计时器

            int x = this.getLocationX();
            int y = this.getLocationY() + this.direction * 2;
            int speedY = this.getSpeedY() + this.direction * 5;
            int deltaSpeedX = 5; // 子弹横向速度增量

            // 散射弹道：同时发射3颗子弹，呈扇形 (中，左，右)
            // 1. 中间子弹
            res.add(new EnemyBullet(x, y, 0, speedY, this.power));
            // 2. 左边子弹
            res.add(new EnemyBullet(x, y, -deltaSpeedX, speedY, this.power));
            // 3. 右边子弹
            res.add(new EnemyBullet(x, y, deltaSpeedX, speedY, this.power));
        }
        return res;
    }
}
