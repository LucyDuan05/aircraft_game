package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.RandomPropSpawner;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeroAircraftTest {

    private HeroAircraft hero;
    private static final int MAX_HP = 1000;

    private RandomPropSpawner propSpawner;

    // 假设飞机和道具的碰撞半径为 15 (需要根据实际游戏尺寸调整)
    private static final int CRASH_THRESHOLD = 30;
    // 每个测试前运行
    @BeforeEach
    void setUp() throws Exception{
        // 获取唯一实例
        hero = HeroAircraft.getInstance();
        // 重置状态标记
        hero.revalidateForTest();
        // 恢复血量
        hero.hp = MAX_HP;

        propSpawner = new RandomPropSpawner();
    }

    @AfterEach
    void tearDown() {
    }

    // 测试方法1：decreaseHp(int decrease) (继承自 AbstractAircraft)
    @DisplayName("T001: 边界值分析 - 治疗上限测试 (HP=MaxHp)")
    @Test
    void decreaseHp_HealingOverMaxHp() {
        hero.decreaseHp(500);
        int actualHp = hero.getHp();
        boolean actualAlive = hero.isAliveForTest();
        System.out.println("T001 实际输出 - HP: " + actualHp);
        System.out.println("T001 实际输出 - 存活状态: " + actualAlive);
        hero.decreaseHp(-1000);
        actualHp = hero.getHp();
        actualAlive = hero.isAliveForTest();
        System.out.println("T001 实际输出 - HP: " + actualHp);
        System.out.println("T001 实际输出 - 存活状态: " + actualAlive);
        assertEquals(MAX_HP, actualHp, "过量治疗后HP应被钳制在 MaxHp (1000)");
    }

    @DisplayName("T002: 功能测试 - 子弹数量和属性验证")
    @Test
    void shoot_VerifyBulletAttributes() {
        List<BaseBullet> bullets = hero.shoot();
        BaseBullet bullet = bullets.get(0);

        int actualSize = bullets.size();
        int actualPower = bullet.getPower();
        int actualSpeedY = bullet.getSpeedY();

        System.out.println("T002 实际输出 - 列表大小: " + actualSize);
        System.out.println("T002 实际输出 - Power: " + actualPower);
        System.out.println("T002 实际输出 - SpeedY: " + actualSpeedY);

        assertEquals(1, actualSize, "英雄机应只发射一颗子弹");
        assertEquals(30, actualPower, "子弹伤害应为 30");
        assertEquals(-5, actualSpeedY, "子弹Y轴速度应为 -5 (向上)");
    }

    @DisplayName("T003: 单例验证 - 两次获取实例是否为同一对象")
    @Test
    void getInstance_MustBeSameInstance() {
        HeroAircraft firstHero = HeroAircraft.getInstance();
        HeroAircraft secondHero = HeroAircraft.getInstance();
        assertSame(firstHero, secondHero, "两次调用 getInstance 必须返回同一个实例");
    }

    @DisplayName("T004: 碰撞检测 - 坐标完全重叠应检测到碰撞")
    @Test
    void crash_OverlappingCollision() {
        // 道具和英雄机坐标完全一致，必须相撞
        AbstractProp prop = propSpawner.spawnProp(
                hero.getLocationX(),
                hero.getLocationY()
        );
        // 调用继承自 AbstractFlyingObject 的公共 crash 方法
        boolean crashed = hero.crash(prop);

        System.out.println("T004 实际输出 - 碰撞检测结果: " + crashed);
        assertTrue(crashed, "坐标完全重叠的英雄机和道具应被检测到碰撞");
    }
}