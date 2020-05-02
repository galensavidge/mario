package mario;

import engine.*;

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
        Game.setTargetFPS(60);
        Game.setUseFrameTime(true);
        GameGraphics.initWindow("Mario",24*16,14*16, 4);
        new engine.Background();
        World.init(4000,300);
        GameController gc = new GameController();
        InputManager.init();

        Game.clearNonPersistentObjects();
        World.loadFromFile("./levels/level0");
        Background b = new mario.Background();
        Player player = new Player(64,48);
        new Camera(player);
        new Coin(8*16,12*16);
        Game.run();
        GameGraphics.closeWindow();
    }
}
