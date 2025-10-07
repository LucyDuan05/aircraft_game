package edu.hitsz.aircraft;

public class MobEnemyFactory implements AircraftFactory {
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY) {
        // 传入的位置参数直接用于实例化 MobEnemy
        return new MobEnemy(locationX,
                locationY,
                0, 10, 30);
    }
}