package hr.tvz.njakopcic.zavrsnijakopcic;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameEngine;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.IGameLogic;
import hr.tvz.njakopcic.zavrsnijakopcic.game.DummyGame;

public class Main {

    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGameLogic gameLogic = new DummyGame();
            GameEngine gameEngine = new GameEngine("ZavrsniJakopcic", 800, 600, vSync, gameLogic);
            gameEngine.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}