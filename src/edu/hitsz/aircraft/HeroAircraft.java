package edu.hitsz.aircraft;

import edu.hitsz.aircraft.shoot.ShootStrategy;
import edu.hitsz.aircraft.shoot.StraightShoot;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    // 1. 定义初始常量
    private static final int INIT_X = Main.WINDOW_WIDTH / 2;
    private static final int INIT_Y = Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight();
    private static final int INIT_HP = 1000;

    // 2. 静态实例，在类加载时立即创建 (饿汉式实现)
    private static HeroAircraft instance = new HeroAircraft(
            INIT_X, INIT_Y, 0, 0, INIT_HP
    );

    /**
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */
    // 2. 私有构造函数
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp){
        // 调用父类构造函数进行初始化
        super(locationX, locationY, speedX, speedY, hp);
        // 设置默认策略：直射
        this.shootStrategy = new StraightShoot();
        this.shootDirection = this.direction;
    }

    // 3. 公有静态方法：全局访问点，无参数,解决 API 歧义问题
    // 第一次调用时负责创建实例并初始化，之后调用只返回已创建的实例
    public static HeroAircraft getInstance() {
        return instance;
    }

    /**攻击方式 */

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 1;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：1，向下发射：-1)
     */
    private int direction = -1;

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    @Override
    /**
     * 通过射击产生子弹
     * @return 射击出的子弹List
     */
    public List<BaseBullet> shoot() {
        return this.shootStrategy.executeShoot(this, this.shootDirection, this.power);
    }
    /**
     * 英雄机切换射击策略的方法
     * @param newStrategy 新的射击策略
     */
    public void changeShootStrategy(ShootStrategy newStrategy) {
        this.shootStrategy = newStrategy;
    }
}
