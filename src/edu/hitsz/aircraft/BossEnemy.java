package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends AbstractAircraft{
    private int shootNum = 20;
    private int power = 10;
    private int shootInterval = 4000;
    private int shootTimer = 0;
    private int bulletSpeed = 10;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        // 调用父类方法，实现边界反转左右移动
        super.forward();

        // 运动轨迹：悬浮于界面上方左右移动
        // 限制 Boss 的 Y 轴位置，避免它下移太多
        if (getLocationY() > Main.WINDOW_HEIGHT * 0.15) {
            setLocation(getLocationX(), (int)(Main.WINDOW_HEIGHT * 0.15));
        }

        // 更新计时器，假设 timeInterval = 40ms
        shootTimer += 40;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        if (shootTimer >= shootInterval) {
            shootTimer = 0; // 重置计时器

            int x = this.getLocationX();
            int y = this.getLocationY();

            int speedMagnitude = this.bulletSpeed;
            // 环形弹道：同时发射12颗子弹
            double deltaAngle = 360.0 / shootNum; // 每颗子弹的角度间隔

            for (int i = 0; i < shootNum; i++) {
                double angle = i * deltaAngle;
                // 计算子弹的 x, y 速度分量
                // 使用 Math.toRadians(angle) 将角度转换为弧度
                int speedX = (int) (speedMagnitude * Math.cos(Math.toRadians(angle)));
                int speedY = (int) (speedMagnitude * Math.sin(Math.toRadians(angle)));

                // 子弹初始位置在 Boss 中心
                res.add(new EnemyBullet(x, y, speedX, speedY, this.power));
            }
        }
        return res;
    }

}
