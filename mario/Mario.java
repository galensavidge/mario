package mario;

import engine.*;

/**
 * Main game class that runs the program.
 *
 * @author Galen Savidge
 * @version 5/6/2020
 */
public class Mario {

    /* Game constants */

    private static final int grid_scale = 16; // Size of a block and the grid on which most objects are placed
    private static final int scaling_ratio = 4;
    public static final int window_scale = 1;
    public static final int fps = 60;

    public static final int pause_suspend_tier = 5;
    public static final int menu_suspend_tier = 10;

    public static final int player_priority = 10;
    public static final int enemy_priority = 12;
    public static final int gizmo_priority = 15;
    public static final int block_priority = 0;

    public static final int player_layer = 10;
    public static final int enemy_layer = 5;
    public static final int gizmo_layer = 2;
    public static final int block_layer = 0;
    public static final int bg_layer = -10;

    public static int getGridScale() {
        return grid_scale *scaling_ratio;
    }

    public static void main(String[] args) {
        // Engine init
        Game.setTargetFPS(fps);
        Game.setUseFrameTime(true);
        GameGraphics.initWindow("Mario",24* getGridScale(),14* getGridScale(), window_scale);
        GameGraphics.setDrawScale(scaling_ratio);
        InputManager.init();

        // Game init
        GameController gc = new GameController();

        // Create game world
        GameController.switchLevel("./levels/level0");

        // Instantiate objects
        Background b = new mario.Background();
        Galoomba g = new Galoomba(38*getGridScale(), 8*getGridScale());

        // Run game
        Game.setSuspendTier(0);
        Game.run();

        // End game
        GameGraphics.closeWindow();
    }
}
