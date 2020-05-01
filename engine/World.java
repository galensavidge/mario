package engine;

import engine.objects.*;
import java.io.File;
import java.io.FileNotFoundException;
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


    /**
     * Need information about file formatting here...
     *
     * @param file_name The file to load.
     * @return True if successful.
     */
    public static boolean loadFromFile(String file_name) {
        File file = new File(file_name);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        }
        catch(FileNotFoundException e) {
            return false;
        }
        int grid_scale = 0, grid_width = 0, grid_height = 0;
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] words = line.split(" ");
            switch (words[0]) {
                case "Gridscale:":
                    try {
                        grid_scale = Integer.parseInt(words[1]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
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
                case "Block":
                    int x1, y1, x2, y2;
                    if (words.length >= 2) {
                        String[] c = words[1].split(",");
                        x1 = Integer.parseInt(c[0]);
                        y1 = Integer.parseInt(c[1]);

                        if (words.length >= 3) {
                            c = words[2].split(",");
                            x2 = Integer.parseInt(c[0]);
                            y2 = Integer.parseInt(c[1]);

                            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                                for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                                    new Block(x * grid_scale, y * grid_scale);
                                }
                            }
                        } else {
                            new Block(x1 * grid_scale, y1 * grid_scale);
                        }
                    }
                    break;
            }

        }

        if(grid_scale <= 0 || grid_width <= 0 || grid_height <= 0) {
            return false;
        }

        World.width = grid_width*grid_scale;
        World.height = grid_height*grid_scale;

        return true;
    }
}
