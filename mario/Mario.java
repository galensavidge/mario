package mario;

import engine.*;
import engine.objects.*;

/**
 * Main game class that runs the program.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Mario
{
    public static void main(String[] args)
    {
        Game.setTargetFPS(143);
        GameGraphics.initWindow("Mario",360,240, 4);
        Background b = new Background();
        World.init(4000,300);
        GameController gc = new GameController();
        InputManager.init();

        Game.clearNonPersistentObjects();
        World.loadFromFile("./levels/level0");
        new Player(64,48);
        new Slope(3*16,6*16);
        Game.run();
        GameGraphics.closeWindow();
    }
}
