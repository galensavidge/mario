package mario;

import engine.*;
import mario.objects.*;

/**
 * Main game class that runs the program.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Mario
{
    /* Game constants */

    private static final int grid_scale = 16; // Size of a block and the grid on which most objects are placed
    private static final int scaling_ratio = 4;
    public static final int window_scale = 1;
    public static final int fps = 60;

    public static int getGridScale() {
        return grid_scale *scaling_ratio;
    }

    public static void main(String[] args)
    {
        // Engine init
        Game.setTargetFPS(fps);
        Game.setUseFrameTime(true);
        GameGraphics.initWindow("Mario",24* getGridScale(),14* getGridScale(), window_scale);
        GameGraphics.setDrawScale(scaling_ratio);
        InputManager.init();

        // Game init
        GameController gc = new GameController();

        // Create game world
        Game.clearNonPersistentObjects();
        WorldLoader.loadFromFile("./levels/level0");

        // Instantiate objects
        Background b = new mario.Background();
        Player player = new Player(3* getGridScale(),11.5* getGridScale());
        new Camera(player);

        // Run game
        Game.run();

        // End game
        GameGraphics.closeWindow();
    }
}
