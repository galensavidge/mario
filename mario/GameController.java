package mario;

import engine.GameGraphics;
import engine.objects.GameObject;

import java.awt.*;

/**
 * A persistent object that keeps track of game variables and controls the high level flow of the game.
 *
 * @author Galen Savidge
 * @version 4/30/2020
 */
public class GameController extends GameObject {

    public static int coins = 0;

    public GameController() {
        super(0, 20);
        this.persistent = true;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        GameGraphics.drawText(64, 64, true, Integer.toString(coins), Color.BLACK);
    }
}
