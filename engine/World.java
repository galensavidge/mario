package engine;

import engine.objects.Collider;
import engine.objects.PhysicsObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A class used to construct the game world from templates.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public class World {
    // Size of the world in pixels
    private static int width;
    private static int height;
    private static int grid_scale;

    private static PhysicsObject[][] grid;

    public static void init(int width, int height) {
        World.width = width;
        World.height = height;
        gridInit(1, 1);

        Game.clearNonPersistentObjects();
    }


    /* Accessor functions */

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getGridScale() {
        return grid_scale;
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
        catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static PhysicsObject gridGet(int x, int y) {
        return grid[x][y];
    }


    /* World generation */

    /**
     * Need information about file formatting here...
     *
     * @param file_name The file to load.
     * @return True if successful.
     */
    public static boolean loadFromFile(String file_name, int grid_size, LineFormat[] line_formats) {
        // Open file
        File file = new File(file_name);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        }
        catch(FileNotFoundException e) {
            return false;
        }

        // Set world grid scale
        grid_scale = grid_size;

        // Get world size
        int grid_width = 0, grid_height = 0;
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] words = line.split(" ");
            switch (words[0]) {
                case "Width:":
                    try {
                        grid_width = Integer.parseInt(words[1]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                case "Height:":
                    try {
                        grid_height = Integer.parseInt(words[1]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
            }
        }

        if(grid_size <= 0 || grid_width <= 0 || grid_height <= 0) {
            return false;
        }

        World.width = grid_width*grid_size;
        World.height = grid_height*grid_size;
        gridInit(grid_width, grid_height);
        Collider.initColliders(3*grid_size);

        // Reset scanner
        try {
            scanner = new Scanner(file);
        }
        catch(FileNotFoundException e) {
            return false;
        }

        // Spawn objects
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] words = line.split(" ");

            // Get object name
            String name = words[0];

            // Get args
            ArrayList<Integer> args = new ArrayList<>();
            for(int i = 1;i < words.length;i++) {
                String word = words[i];
                String[] comma_split = word.split(",");
                for(String value : comma_split) {
                    try {
                        args.add(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Pass args to the correct LineFormat
            for(LineFormat f : line_formats) {
                if(f.getName().equals(name)) {
                    f.handleLine(args);
                    break;
                }
            }
        }

        return true;
    }


    public static abstract class LineFormat {

        public abstract String getName();

        public abstract void handleLine(ArrayList<Integer> args);
    }
}
