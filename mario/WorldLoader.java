package mario;

import engine.World;

import java.util.ArrayList;

public class WorldLoader {

    private static final World.LineFormat[] line_formats = {new BlockLineFormat(), new CloudBlockLineFormat()};

    public static boolean loadFromFile(String file_name) {
        return World.loadFromFile(file_name, Mario.getGridScale(), line_formats);
    }

    private static class BlockLineFormat extends World.LineFormat {

        @Override
        public String getName() {
            return Block.type_name;
        }

        @Override
        public void handleLine(ArrayList<Integer> args) {
            if(args.size() == 2) {
                addBlock(args.get(0), args.get(1));
            }
            else if(args.size() == 4) {
                int x1, y1, x2, y2;
                x1 = Math.min(args.get(0), args.get(2));
                y1 = Math.min(args.get(1), args.get(3));
                x2 = Math.max(args.get(0), args.get(2));
                y2 = Math.max(args.get(1), args.get(3));

                for (int x = x1; x <= x2; x++) {
                    for (int y = y1; y <= y2; y++) {
                        addBlock(x, y);
                    }
                }
            }
        }

        protected void addBlock(int x, int y) {
            Block b = new Block(x*World.getGridScale(), y*World.getGridScale());
            World.gridSet(x, y, b);
        }
    }

    private static class CloudBlockLineFormat extends BlockLineFormat {

        @Override
        public String getName() {
            return CloudBlock.type_name;
        }

        @Override
        protected void addBlock(int x, int y) {
            CloudBlock b = new CloudBlock(x*World.getGridScale(), y*World.getGridScale());
            World.gridSet(x, y, b);
        }
    }
}
