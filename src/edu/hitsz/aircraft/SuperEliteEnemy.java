package edu.hitsz.aircraft;

import edu.hitsz.aircraft.shoot.StraightShoot;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class SuperEliteEnemy extends AbstractAircraft{
    // private int shootNum = 3;
    private int power = 10;
    private int direction = 1;
    private int shootInterval = 1000;
    private int shootTimer = 0;

    public SuperEliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 设置 SuperEliteEnemy 的策略：散射
        this.shootStrategy = new StraightShoot();
        this.shootDirection = this.direction;
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
        // 检查射击计时器是否归零
        if (shootTimer >= shootInterval) {
            shootTimer = 0; // 重置计时器
            // 使用策略模式执行射击
            res.addAll(this.shootStrategy.executeShoot(this, this.shootDirection, this.power));
        }
        return res;
    }
}
