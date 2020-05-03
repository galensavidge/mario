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
    /* Game constants */

    private static final int grid_size = 16; // Size of a block and the grid on which most objects are placed
    private static final int scaling_ratio = 4;
    public static final int window_scale = 1;

    public static int getGridSize() {
        return grid_size*scaling_ratio;
    }

    public static void main(String[] args)
    {
        // Engine init
        Game.setTargetFPS(60);
        Game.setUseFrameTime(true);
        GameGraphics.initWindow("Mario",24*getGridSize(),14*getGridSize(), window_scale);
        GameGraphics.setDrawScale(scaling_ratio);
        InputManager.init();

        // Game init
        GameController gc = new GameController();

        // Load level
        Game.clearNonPersistentObjects();
        World.loadFromFile("./levels/level0");
        Background b = new mario.Background();
        Player player = new Player(3*getGridSize(),11.5*getGridSize());
        new Camera(player);
        new Coin(8*getGridSize(),11*getGridSize());

        // Run game
        Game.run();

        // End game
        GameGraphics.closeWindow();
    }
}
