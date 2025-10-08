package edu.hitsz.aircraft;

public class SuperEliteEnemyFactory implements AircraftFactory{
    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY) {
        // SuperEliteEnemy: HP=100, SpeedX=4 (左右移动), SpeedY=6 (向下)
        return new SuperEliteEnemy(locationX, locationY, 4, 6, 90);
    }
}
