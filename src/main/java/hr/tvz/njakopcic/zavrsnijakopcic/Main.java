package hr.tvz.njakopcic.zavrsnijakopcic;

import hr.tvz.njakopcic.zavrsnijakopcic.engine.GameEngine;
import hr.tvz.njakopcic.zavrsnijakopcic.engine.IGameLogic;
import hr.tvz.njakopcic.zavrsnijakopcic.game.SpaceGame;

public class Main {

    public static void main(String[] args) {
        try {
            IGameLogic gameLogic = new SpaceGame();
            GameEngine gameEngine = new GameEngine("ZavrsniJakopcic", 1024, 576, true, gameLogic);
            gameEngine.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}