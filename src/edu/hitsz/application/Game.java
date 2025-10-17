package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.FireProp;
import edu.hitsz.prop.RandomPropSpawner;
import edu.hitsz.prop.SuperFireProp;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import edu.hitsz.data.ScoreDao;
import edu.hitsz.data.ScoreDaoImpl;
import edu.hitsz.data.ScoreRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;
    private String TestPlayer = "player";
    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    // 工厂模式实例
    private final RandomEnemySpawner aircraftSpawner = new RandomEnemySpawner();
    private final RandomPropSpawner propSpawner = new RandomPropSpawner();

    private final BossEnemyFactory bossFactory = new BossEnemyFactory();

    // 单例模式实例
    private final HeroAircraft heroAircraft;

    // 游戏列表
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;

    /**
     * 屏幕中出现的敌机最大数量
     */
    private int enemyMaxNumber = 5;

    /**
     * 当前得分
     */
    private int score = 0;
    /**
     * 当前时刻
     */
    private int time = 0;

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    private int spawnDuration = 1040;
    private int shootDuration = 520;
    private int cycleTime = 0;

    private int bossScoreThreshold = 300;   // Boss 出现的分数阈值
    private final int bossadd = 200;
    private int bossSpawnCount = 0;         // 记录 Boss 出现次数

    private final Random random = new Random();

    /**
     * 游戏结束标志
     */
    private boolean gameOverFlag = false;

    // DAO 成员变量
    private ScoreDao scoreDao;

    // 构造函数不再接受列表参数
    public Game() {

        // 初始化列表成员变量
        this.enemyAircrafts = new LinkedList<>();
        this.heroBullets = new LinkedList<>();
        this.enemyBullets = new LinkedList<>();
        this.props = new LinkedList<>();

        // 单例模式 & 无参数方法
        this.heroAircraft = HeroAircraft.getInstance();

        this.scoreDao = new ScoreDaoImpl();

        /**
         * Scheduled 线程池，用于定时任务调度...
         */
        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {

        // **修正：删除多余的图片宽度变量 (已移至工厂)**

        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {

            time += timeInterval;

            // 1. 射击周期判断
            if (time % shootDuration == 0) { // 使用模运算判断是否到达射击周期
                // 飞机射出子弹
                shootAction();
            }

            // 2. 敌机生成周期判断
            if (time % spawnDuration == 0) {
                System.out.println(time);

                // 新敌机随机产生 (使用工厂模式)
                if (enemyAircrafts.size() < enemyMaxNumber) {
                    enemyAircrafts.add(aircraftSpawner.spawnEnemy());
                }

                // Boss 出现逻辑
                boolean hasBoss = enemyAircrafts.stream().anyMatch(e -> e instanceof BossEnemy);
                if (!hasBoss && score >= bossScoreThreshold) { // 分数达到设定阈值
                    // 没有Boss 且达到下一次Boss阈值，则生成Boss
                    bossSpawnCount++;
                    // Boss 总是从屏幕中央顶部生成
                    int bossX = Main.WINDOW_WIDTH / 2;

                    int bossY = ImageManager.BOSS_ENEMY_IMAGE.getHeight() / 2;
                    enemyAircrafts.add(bossFactory.createAircraft(bossX, bossY));

                    // 阈值增加，使下一次Boss出现更难
                    bossScoreThreshold += bossadd; // 调整下一次阈值
                }

            }

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            // 撞击检测
            crashCheckAction();

            // 道具移动
            propsMoveAction();

            // 后处理
            postProcessAction();

            //每个时刻重绘界面
            repaint();

            // 游戏结束检查英雄机是否存活
            if (heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over! 最终得分: " + this.score);
                // 1. 获取用户输入（本次实验无需实现交互，模拟输入）
                // enter玩家名
                String playerName = TestPlayer + (new Random().nextInt(100));

                // 2. 创建得分记录对象
                ScoreRecord newRecord = new ScoreRecord(playerName, this.score, LocalDateTime.now());

                // 3. 使用 DAO 添加新的得分记录
                scoreDao.addScore(newRecord);

                // 4. 使用 DAO 获取并打印排行榜
                List<ScoreRecord> allScores = scoreDao.getAllScores();
                scoreDao.printScores(allScores);
            }

        };

        /**
         * 以固定延迟时间进行执行
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    //***********************
    //      Action 各部分
    //***********************

//    private boolean timeCountAndNewCycleJudge() {
//        cycleTime += timeInterval;
//        if (cycleTime >= shootDuration) {
//            // 跨越到新的周期
//            cycleTime %= shootDuration;
//            return true;
//        } else {
//            return false;
//        }
//    }

    private void shootAction() {
        // TODO 敌机射击
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof EliteEnemy
                    || enemyAircraft instanceof SuperEliteEnemy
                    || enemyAircraft instanceof BossEnemy) {
                // 三种敌机能射击
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
        // 英雄射击 (使用成员变量的shoot方法)
        heroBullets.addAll(heroAircraft.shoot());
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        // 修正：移除 EliteEnemy 的生成逻辑（已移至工厂）
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void propsMoveAction() { // 道具移动
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // TODO 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }
        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // 获得分数，产生道具补给
                        score += 10;
                        // 道具掉落逻辑
                        int dropCount = 0;
                        if (enemyAircraft instanceof EliteEnemy) {
                            // EliteEnemy 掉落 <= 1 个道具
                            dropCount = random.nextDouble() < 0.7 ? 1 : 0;
                        } else if (enemyAircraft instanceof SuperEliteEnemy) {
                            // SuperEliteEnemy 随机掉落 <= 1 个道具
                            dropCount = random.nextDouble() < 0.9 ? 1 : 0;
                        } else if (enemyAircraft instanceof BossEnemy) {
                            // BossEnemy 随机掉落 <= 3 个道具
                            // 假设 100% 概率掉落 1-3 个
                            dropCount = random.nextInt(3) + 1;
                        }

                        // 生成道具
                        if (dropCount > 0) {
                            props.addAll(propSpawner.spawnMultipleProps(
                                    enemyAircraft.getLocationX(),
                                    enemyAircraft.getLocationY(),
                                    dropCount
                            ));
                        }
                    }
                }


                // 英雄机与敌机相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // 我方获得道具，道具生效
        for (AbstractProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (heroAircraft.crash(prop) || prop.crash(heroAircraft)) {
                prop.effect(heroAircraft);
                if (prop instanceof FireProp) {
                    this.shootDuration = 1040;
                } else if (prop instanceof SuperFireProp) {
                    this.shootDuration = 1480;
                }
                prop.vanish(); // 道具使用后必须消失
            }
        }

    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 删除无效的道具
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid); // <-- 清理无效道具
    }


    //***********************
    //      Paint 各部分
    //***********************

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param  g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);

        paintImageWithPositionRevised(g, enemyAircrafts);
        paintImageWithPositionRevised(g, props); // <-- 绘制道具

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        //绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }
}