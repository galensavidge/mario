package mario;

import engine.objects.GameObject;

/**
 * A persistent object that keeps track of game variables and controls the high level flow of the game.
 *
 * @author Galen Savidge
 * @version 4/30/2020
 */
public class GameController extends GameObject {

    public GameController() {
        super(0, 0);
        this.persistent = true;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {

    }
}
