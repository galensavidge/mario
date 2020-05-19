package engine;

import engine.objects.PhysicsObject;

/**
 * A class which contains the physical properties of the game world.
 *
 * @author Galen Savidge
 * @version 5/12/2020
 */
public class World {
    // Size of the world in pixels
    static int width;
    static int height;
    static int grid_size;

    // Ratio between world coordinates and map coordinates (e.g. when using LevelParser)
    static int grid_scaling_factor = 1;

    private static PhysicsObject[][] grid;

    public static void setGridScale(int grid_scaling_factor) {
        World.grid_scaling_factor = grid_scaling_factor;
    }


    /* Accessor functions */

    public static int getWidth() {
        return World.width;
    }

    public static int getHeight() {
        return World.height;
    }

    public static int getGridSize() {
        return World.grid_size;
    }


    /* World grid */

    private static void gridInit(int width, int height) {
        grid = new PhysicsObject[width][height];
    }

    public static boolean gridSet(int x, int y, PhysicsObject object) {
        try {
            if(grid[x][y] == null) {
                grid[x][y] = object;
                return true;
            }
            else {
                return false;
            }
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static PhysicsObject gridGet(int x, int y) {
        return grid[x][y];
    }
}
