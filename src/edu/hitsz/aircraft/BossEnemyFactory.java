package edu.hitsz.aircraft;

public class BossEnemyFactory implements AircraftFactory {
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY) {
        // BossEnemy: HP=510 (高血量), SpeedX=2 (左右悬浮移动), SpeedY=0
        return new BossEnemy(locationX, locationY, 2, 0, 510);
    }
}
