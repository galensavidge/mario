package mario;

import engine.World;
import engine.objects.PhysicsObject;
import mario.objects.*;

import java.util.ArrayList;

/**
 * This class loads text files using {@link engine.World}. Each object type has a corresponding {@code LineFormat}
 * object that describes the parameters used to place it into the game world.
 *
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class WorldLoader {

    private static final World.LineFormat[] line_formats =
            {new BlockLineFormat(), new CloudBlockLineFormat(), new CoinLineFormat(), new SlopeLineFormat()};

    public static boolean loadFromFile(String file_name) {
        return World.loadFromFile(file_name, Mario.getGridScale(), line_formats);
    }

    /**
     * The standard line format for adding multiple objects. Takes five arguments in the form:
     * {@code <name> <x1>,<y1> <x2>,<y2>} and creates an object at every grid space between the points
     * {@code (x1, y1)} and {@code (x2, y2)}.
     */
    private static abstract class StandardLineFormat extends World.LineFormat {

        @Override
        public void handleLine(ArrayList<Integer> args) {
            if(args.size() == 2) {
                int x = args.get(0);
                int y = args.get(1);
                World.gridSet(x, y, addObject(x*World.getGridScale(), y*World.getGridScale()));
            }
            else if(args.size() == 4) {
                int x1, y1, x2, y2;
                x1 = Math.min(args.get(0), args.get(2));
                y1 = Math.min(args.get(1), args.get(3));
                x2 = Math.max(args.get(0), args.get(2));
                y2 = Math.max(args.get(1), args.get(3));

                for (int x = x1; x <= x2; x++) {
                    for (int y = y1; y <= y2; y++) {
                        World.gridSet(x, y, addObject(x*World.getGridScale(), y*World.getGridScale()));
                    }
                }
            }
            else {
                System.out.println("Bad line!");
            }
        }

        /**
         * Override this method in child classes to create the correct object at position {@code (x, y)}.
         */
        protected abstract PhysicsObject addObject(double x, double y);
    }

    private static class BlockLineFormat extends StandardLineFormat {

        @Override
        public String getName() {
            return HardBlock.type_name;
        }

        @Override
        protected PhysicsObject addObject(double x, double y) {
            return new HardBlock(x, y);
        }
    }

    private static class CloudBlockLineFormat extends StandardLineFormat {

        @Override
        public String getName() {
            return CloudBlock.type_name;
        }

        @Override
        protected PhysicsObject addObject(double x, double y) {
            return new CloudBlock(x, y);
        }
    }

    private static class CoinLineFormat extends StandardLineFormat {

        @Override
        public String getName() {
            return Coin.type_name;
        }

        @Override
        protected PhysicsObject addObject(double x, double y) {
            return new Coin(x, y);
        }
    }

    /**
     * Format: {@code <x>,<y> <width> <height> (<flip-vertical?> = 0) (<flip-horizontal?> = 0)}
     */
    private static class SlopeLineFormat extends World.LineFormat {
        @Override
        public String getName() {
            return Slope.type_name;
        }

        @Override
        public void handleLine(ArrayList<Integer> args) {
            if(args.size() == 4) {
                int x = args.get(0);
                int y = args.get(1);
                int width = args.get(2);
                int height = args.get(3);
                Slope s = new Slope(x*World.getGridScale(), y*World.getGridScale(), width, height,
                        false, false);
                World.gridSet(x, y, s);
            }
            else if(args.size() == 6) {
                int x = args.get(0);
                int y = args.get(1);
                int width = args.get(2);
                int height = args.get(3);
                boolean flip_horizontal = args.get(4) == 1;
                boolean flip_vertical = args.get(5) == 1;
                Slope s = new Slope(x*World.getGridScale(), y*World.getGridScale(), width, height,
                        flip_horizontal, flip_vertical);
                World.gridSet(x, y, s);
            }
        }
    }
}
