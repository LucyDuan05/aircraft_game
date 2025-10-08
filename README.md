# AircraftWar

实验二作业：
    
- 代码在src/edu/hitsz文件夹内，共进行两个改变：
    
    - 1、将英雄机修改为单例模式：
      - 修改了HeroAircraft
      - 修改了Game类中创建英雄机的逻辑  
    
    - 2、将敌机和道具修改为工厂模式
      - 添加了工厂实体EliteEnemyFactory、MobEnemyFactory、BloodPropFactory、FirePropFactory、BombPropFactory
      - 添加了抽象工厂接口AircraftFactory、PropFactory
      - 添加了构造器RandomEnemySpawner、RandomPropSpawner，负责处理敌机的创建逻辑
      - 修改了Game类中创建敌机的逻辑
    
- 类图的代码及图片在uml文件夹内，命名为HeroAircraft单例模式、EnemyFactory、PropFactory类图。