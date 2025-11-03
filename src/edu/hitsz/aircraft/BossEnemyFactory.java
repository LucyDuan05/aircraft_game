package edu.hitsz.aircraft;

import edu.hitsz.application.Game;
import edu.hitsz.application.Main;

public class BossEnemyFactory implements AircraftFactory {
    private final Game game;

    public BossEnemyFactory(Game game) {
        this.game = game;
    }

    @Override
    public AbstractAircraft createAircraft(int locationX, int locationY) {
        int bossSpawnCount = game.getBossSpawnCount();
        int hp = game.getBossEnemyHp(bossSpawnCount);
        return new BossEnemy(locationX, locationY, 2, 0, hp);
    }
}
