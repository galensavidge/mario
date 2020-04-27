/**
 * A class used to construct the game world from templates.
 *
 * @author Galen Savidge
 * @version 4/25/2020
 */
public class World {
    // Size of the world in pixels
    private static int width;
    private static int height;

    public static void init(int width, int height) {
        World.width = width;
        World.height = height;

        Game.clearNonPersistentObjects();
    }

    /* Accessor functions */
    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}
