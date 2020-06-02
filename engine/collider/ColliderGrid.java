package engine.collider;

import engine.World;
import engine.util.Vector2;

import java.util.ArrayList;

/**
 * @author Galen Savidge
 * @version 6/1/2020
 */
public class ColliderGrid {

    private static final int zone_size_in_grid = 2; // Zone size in world grid squares
    private static int zone_size; // Zone size in world pixels
    private static ArrayList<Collider>[][] colliders; // A list of all colliders that exist as a 2D array of zones

    /**
     * Initializes the data structure used to check which {@link Collider} instances lie within a specific area. Should
     * be called before any {@link Collider} objects are created.
     *
     * @throws ExceptionInInitializerError Throws if {@link engine.World} has not yet been initialized.
     */
    public static void init() throws ExceptionInInitializerError {
        if(World.getGridSize() == 0) {
            throw new ExceptionInInitializerError("World not initialized!");
        }

        zone_size = zone_size_in_grid*World.getGridSize();
        int grid_width = World.getWidth()/zone_size + 1;
        int grid_height = World.getHeight()/zone_size + 1;
        colliders = new ArrayList[grid_width][grid_height];
        for(int i = 0;i < grid_width;i++) {
            for(int j = 0;j < grid_height;j++) {
                colliders[i][j] = new ArrayList<>();
            }
        }
    }


    /* Accessors */

    public static int getZoneSize() {
        return zone_size;
    }


    /* Collider zone functions */

    /**
     * Adds {@code c} to the collider grid.
     *
     * @see #inZone
     */
    public static void add(Collider c) {
        for(Vector2 p : c.zone_check_points) {
            int x = (int)((p.x + c.getPosition().x)/zone_size);
            int y = (int)((p.y + c.getPosition().y)/zone_size);
            int zone_x = Math.min(Math.max(0, x), colliders.length - 1);
            int zone_y = Math.min(Math.max(0, y), colliders[0].length - 1);
            if(!colliders[zone_x][zone_y].contains(c)) {
                colliders[zone_x][zone_y].add(c);
            }
        }
    }

    /**
     * Removes {@code c} from the collider grid.
     *
     * @see #inZone
     */
    public static void remove(Collider c) {
        for(Vector2 p : c.zone_check_points) {
            int x = (int)((p.x + c.getPosition().x)/zone_size);
            int y = (int)((p.y + c.getPosition().y)/zone_size);
            int zone_x = Math.min(Math.max(0, x), colliders.length - 1);
            int zone_y = Math.min(Math.max(0, y), colliders[0].length - 1);
            colliders[zone_x][zone_y].remove(c);
        }
    }

    /**
     * @return The list of all {@link Collider} objects in the grid.
     */
    public static ArrayList<Collider> all() {
        ArrayList<Collider> all = new ArrayList<>();
        for(ArrayList<Collider>[] collider : colliders) {
            for(ArrayList<Collider> colliderArrayList : collider) {
                all.addAll(colliderArrayList);
            }
        }
        return all;
    }

    /**
     * @return The colliders in zone {@code (x, y)} of the collider grid.
     */
    public static ArrayList<Collider> inZone(int x, int y) {
        if(x >= 0 && x < colliders.length && y >= 0 && y < colliders[0].length) {
            return new ArrayList<>(colliders[x][y]);
        }
        else {
            return new ArrayList<>();
        }
    }

    /**
     * Returns a list of {@link Collider} objects near {@code position}. Objects returned lie in the same zone as or
     * neighboring zones to {@code position}.
     *
     * @param distance The number of zones away in every direction (including diagonals) to check. The zones checked
     *                 form a square with {@code 2*distance + 1} grid squares on each side, e.g. with {@code distance =
     *                 1} the area checked will be a 3x3 area of grid squares with {@code position} in its center
     *                 square.
     */
    public static ArrayList<Collider> inNeighboringZones(Vector2 position, int distance) {

        int zone_x = (int)(position.x/ColliderGrid.getZoneSize());
        int zone_y = (int)(position.y/ColliderGrid.getZoneSize());

        ArrayList<Collider> colliders = new ArrayList<>();

        for(int i = -distance;i <= distance;i++) {
            for(int j = -distance;j <= distance;j++) {
                ArrayList<Collider> zone = ColliderGrid.inZone(zone_x + i, zone_y + j);
                zone.removeAll(colliders);
                colliders.addAll(zone);
            }
        }

        return colliders;
    }

    /**
     * Returns a list of {@link Collider} objects near {@code position}. Checks a 3x3 area of grid squares with {@code
     * position} in its center square.
     */
    public static ArrayList<Collider> inNeighboringZones(Vector2 position) {
        return inNeighboringZones(position, 1);
    }

}
