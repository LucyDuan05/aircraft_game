package edu.hitsz.aircraft;

public class EliteEnemyFactory implements AircraftFactory {
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY) {
        // 传入的位置参数直接用于实例化 EliteEnemy
        return new EliteEnemy(locationX,
                locationY,
                0, 10, 60);
    }
}