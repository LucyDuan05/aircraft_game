package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.aircraft.shoot.StraightShoot;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.*;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import edu.hitsz.data.ScoreDao;
import edu.hitsz.data.ScoreDaoImpl;
import edu.hitsz.data.ScoreRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
// 修复：移除 LinkedList，导入 CopyOnWriteArrayList
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
    // 修复：List 接口声明不变，实现改为 CopyOnWriteArrayList (见构造函数)
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
    // 修复：添加 volatile 保证多线程可见性
    private volatile int score = 0;
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
    // 修复：添加 volatile 保证多线程可见性
    private volatile boolean gameOverFlag = false;

    // DAO 成员变量
    private ScoreDao scoreDao;
    // MainFrame 引用
    private Main mainFrame;
    private SoundManager soundManager;
    private int difficulty;

    private final int propDuration = 3000; // 道具效果持续时间，例如 8 秒 (8000ms)
    private ScheduledFuture<?> firePropTimer; // 用于追踪当前道具计时器的 Future
    private final int originalShootDuration = 520; // 记录英雄机默认射击间隔 (从您的初始化值获取)

    // 修复：添加 volatile 保证多线程可见性
    private volatile long propEndTime = 0;

    // 构造函数接受 MainFrame 引用
    public Game(Main mainFrame, int difficulty) {
        this.mainFrame = mainFrame;
        this.difficulty = difficulty;
        this.soundManager = mainFrame.getSoundManager(); // 获取 SoundManager
        this.scoreDao = mainFrame.getScoreDao();         // 从 MainFrame 获取 DAO

        // 初始化列表成员变量
        // 修复：使用 CopyOnWriteArrayList 替代 LinkedList，防止并发修改异常
        this.enemyAircrafts = new CopyOnWriteArrayList<>();
        this.heroBullets = new CopyOnWriteArrayList<>();
        this.enemyBullets = new CopyOnWriteArrayList<>();
        this.props = new CopyOnWriteArrayList<>();

        // 单例模式 & 无参数方法
        this.heroAircraft = HeroAircraft.getInstance();
        this.heroAircraft.reset();
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

            // 修复：检查 gameOverFlag，如果为 true，则任务不再执行任何操作
            if (gameOverFlag) {
                return;
            }

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

                    // **音效：Boss 出场**
                    soundManager.stopBgm();
                    soundManager.playBossBgm();

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
                executorService.shutdownNow();
                gameOverFlag = true;

                // **音效：游戏结束**
                soundManager.stopAll();
                soundManager.playSound(SoundManager.GAME_OVER_PATH);

                System.out.println("Game Over! 最终得分: " + this.score);

                // 修复：将所有 UI 交互（JOptionPane）和后续逻辑（DAO、界面切换）
                // 放入 SwingUtilities.invokeLater，确保它们在 EDT (事件分发线程) 上执行
                SwingUtilities.invokeLater(() -> {
                    // 1. 获取用户输入（现在在 EDT 上执行，是安全的）
                    String playerName = JOptionPane.showInputDialog(this, "Game Over! 最终得分: " + this.score + "\n请输入您的名字:");

                    if (playerName == null || playerName.trim().isEmpty()) {
                        playerName = "未知玩家";
                    }

                    // 2. 创建得分记录对象
                    ScoreRecord newRecord = new ScoreRecord(playerName, this.score, LocalDateTime.now());

                    // 3. 使用 DAO 添加新的得分记录
                    scoreDao.addScore(newRecord);

                    // 4. 通知主框架切换到排行榜页面
                    mainFrame.switchTo(Main.RANK_CARD);
                });
            }

        };

        /**
         * 以固定延迟时间进行执行
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    // ... (shootAction, bulletsMoveAction, aircraftsMoveAction, propsMoveAction 保持不变) ...

    // ... (crashCheckAction 保持不变) ...

    // ... (postProcessAction 保持不变，CopyOnWriteArrayList 支持 removeIf) ...

    // ... (paint, paintImageWithPositionRevised, paintScoreAndLife 保持不变) ...
    // (paintScoreAndLife 读取 volatile 的 score 和 propEndTime 是安全的)

    // 以下是 action() 中未修改的几个方法，为保持完整性而附上
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
        soundManager.playSound(SoundManager.BULLET_PATH);
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
                    soundManager.playSound(SoundManager.BULLET_HIT_PATH);
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
                            soundManager.stopBossBgm();
                            soundManager.playBgm();
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
                // **音效：道具生效音效**
                soundManager.playSound(SoundManager.PROP_PATH);

                // 策略修改：由 FireProp/SuperFireProp 的 effect 方法执行
                prop.effect(heroAircraft);

                // 1. 取消任何现有的计时器，确保新道具覆盖旧道具
                if (firePropTimer != null && !firePropTimer.isDone()) {
                    firePropTimer.cancel(false);
                }

                // 2. 【新增/修复】设置频率，并调度还原任务
                if (prop instanceof FireProp || prop instanceof SuperFireProp) {
                    propEndTime = System.currentTimeMillis() + propDuration;
                    // a. 设置新的射击频率（难度调整）
//                    if (prop instanceof FireProp) {
//                        this.shootDuration = 520;
//                    } else if (prop instanceof SuperFireProp) {
//                        this.shootDuration = 520;
//                    }

                    // b. 调度还原任务：【关键】同时还原策略和频率
                    Runnable revertTask = () -> {
                        // this.shootDuration = originalShootDuration; // 还原频率 (520ms)
                        // 还原射击策略为 StraightShoot (默认策略)
                        heroAircraft.setShootStrategy(new StraightShoot());
                        propEndTime = 0;
                    };

                    // c. 启动 8 秒计时器
                    firePropTimer = executorService.schedule(revertTask, propDuration, TimeUnit.MILLISECONDS);

                }
                else if (prop instanceof BombProp) {
                    // 炸弹道具：只播放音效，不影响计时器
                    // mainFrame.getSoundManager().playSound(SoundManager.BOMB_PATH);
                    soundManager.playSound(SoundManager.PROP_PATH);
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
        switch (difficulty) {
            case 1: {
                g.drawImage(ImageManager.SINGLE_BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
                g.drawImage(ImageManager.SINGLE_BACKGROUND_IMAGE, 0, this.backGroundTop, null);
                break;
            }
            case 2: {
                g.drawImage(ImageManager.NORMAL_BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
                g.drawImage(ImageManager.NORMAL_BACKGROUND_IMAGE, 0, this.backGroundTop, null);
                break;
            }
            case 3: {
                g.drawImage(ImageManager.HARD_BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
                g.drawImage(ImageManager.HARD_BACKGROUND_IMAGE, 0, this.backGroundTop, null);
                break;
            }

        }

        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 修复：此处迭代 CopyOnWriteArrayList 是线程安全的
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
        // 修复：读取 volatile 的 score 是安全的
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);

        // 修复：读取 volatile 的 propEndTime 是安全的
        if (propEndTime > 0) {
            // 计算剩余毫秒数
            long timeLeftMs = propEndTime - System.currentTimeMillis();
            // 转换为秒，保留一位小数
            double timeLeftSec = Math.max(0, timeLeftMs / 1000.0);

            // 在屏幕上绘制
            y = y + 20;
            g.setColor(new Color(65535)); // 蓝色
            g.drawString(String.format("FIRE TIME: %.1f s", timeLeftSec), x, y);
        }
    }
}