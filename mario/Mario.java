package mario;

import engine.*;
import engine.graphics.GameGraphics;

/**
 * Main game class that runs the program.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class Mario {

    /* Game constants */

    private static final int grid_size = 16; // Size of a block and the grid on which most objects are placed
    private static final int scaling_ratio = 4; // The ratio (size of world grid)/grid_size
    public static final int window_scale = 1;
    public static final int fps = 60;

    public static final int hitpause_suspend_tier = 5;
    public static final int menu_suspend_tier = 10;
    public static final int transition_suspend_tier = 15;

    public static final int player_priority = 10;
    public static final int enemy_priority = 12;
    public static final int gizmo_priority = 15;
    public static final int block_priority = 0;

    public static final int transition_layer = 100;
    public static final int player_layer = 10;
    public static final int enemy_layer = 5;
    public static final int gizmo_layer = 2;
    public static final int block_layer = 0;
    public static final int bg_layer = -10;

    public static final String sprite_path = "./sprites/";
    public static final String level_path = "./levels/";

    /**
     * @return The size of the game grid in pixels.
     */
    public static int getGridScale() {
        return grid_size*scaling_ratio;
    }

    /**
     * @return The size, in world pixels, of one pixel in a sprite.
     */
    public static int getPixelSize() {
        return scaling_ratio;
    }

    public static void main(String[] args) {
        // Engine init
        Game.setTargetFPS(fps);
        Game.setUseFrameTime(true);
        GameGraphics.initWindow("Mario", 24*getGridScale(), 14*getGridScale(), window_scale);
        GameGraphics.setDrawScale(scaling_ratio);
        World.grid_scaling_factor = scaling_ratio;
        InputManager.init();

        // Game init
        new GameController();

        GameController.switchLevel("test.json");

        // Run game
        Game.run();

        // End game
        GameGraphics.closeWindow();
    }
}
