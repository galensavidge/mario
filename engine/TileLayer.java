package engine;

import engine.objects.GameObject;

import static engine.LevelParser.XMLGetChildrenByName;
import static engine.LevelParser.XMLOpen;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

/**
 * Caches a set of tiles in an image buffer and draws it in the world.
 *
 * @author Galen Savidge
 * @version 5/13/2020
 */
public class TileLayer extends GameObject {

    private final BufferedImage cache;
    private final double x, y, parallax_factor;

    /**
     * @param layer The layer on which to draw. See {@link GameObject}.
     * @param parallax_factor A scaling factor to apply to this layer's movement relative to the camera. A higher value
     *                        (> 1) will make the layer appear closer to the camera while a smaller value (0-1) will
     *                        make it appear farther in the background. Use 1 for no parallax scrolling.
     * @param tile_sets A list of {@link TileSet} objects to source tiles from.
     * @param gid_list A list of grid ID values representing all the squares in the world grid space. Values range from
     *                 top left to bottom right, wrapping after each row. Each ID represents a unique tile in a
     *                 {@link TileSet} in {@code tile_sets} if parsed correctly. Zero represents an empty space.
     */
    public TileLayer(double x, double y, int layer, double parallax_factor, ArrayList<TileSet> tile_sets, int[] gid_list) {
        super(0, layer);
        this.x = x;
        this.y = y;
        this.parallax_factor = parallax_factor;

        // Create image cache
        cache = GameGraphics.createBufferedImage(World.getWidth()/World.grid_scaling_factor,
                                              World.getHeight()/World.grid_scaling_factor);
        Graphics2D g = cache.createGraphics();
        int grid_size = World.getGridSize()/World.grid_scaling_factor;
        int grid_width = World.getWidth()/World.getGridSize();

        // Loop through gid array
        for(int i = 0;i < gid_list.length;i++) {
            int gid = gid_list[i];

            // Calculate coordinates
            int grid_x = (i % grid_width)*grid_size;
            int grid_y = (i / grid_width)*grid_size;

            // Draw from correct tile set
            for(TileSet tile_set : tile_sets) {
                Image tile = tile_set.getTile(gid);
                if(tile != null) {
                    g.drawImage(tile, grid_x, grid_y + grid_size - tile.getHeight(null), null);
                    break;
                }
            }
        }

        g.dispose();
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)(x+GameGraphics.camera_x*(parallax_factor - 1)),
                (int)(y+GameGraphics.camera_y*(parallax_factor - 1)),
                false, cache);
    }

    /**
     * Represents a set of tiles. Holds the source image and the data required to grab appropriate regions from the
     * image. Created from .tsx files output by Tiled.
     */
    public static class TileSet {
        String name;
        String source;
        int tile_width;
        int tile_height;
        ArrayList<TileImage> tiles;

        public static class TileImage {
            String file;
            BufferedImage image;
            int first_gid;
            int tile_count;
            int columns;
            int spacing;
            int margin;
        }

        /**
         *
         * @param directory Path to the tile file directory.
         * @param tsx_file Tile file created by Tiled.
         * @param first_gid The ID of the first (top-left) tile in this set, likely read from a Tiled map.
         */
        TileSet(String directory, String tsx_file, int first_gid) {
            Document doc = XMLOpen(directory+tsx_file);
            this.source = tsx_file;

            this.tiles = new ArrayList<>();

            // Create image object
            Node tileset = doc.getChildNodes().item(0);
            Element tileset_element = (Element) tileset;

            // Single-tile files
            for(Node tile_node : XMLGetChildrenByName(tileset, "tile")) {
                Element tile_element = (Element) tile_node;
                TileImage tile = new TileImage();

                // Load image
                Element image_element = (Element)((Element)tile_node).getElementsByTagName("image").item(0);
                tile.file = image_element.getAttribute("source");
                tile.image = GameGraphics.createBufferedImage(GameGraphics.getImage(directory+tile.file));

                // Set properties
                tile.first_gid = Integer.parseInt(tile_element.getAttribute("id")) + first_gid;
                tile.tile_count = 1;
                tile.columns = 1;
                tile.spacing = 0;
                tile.margin = 0;

                this.tiles.add(tile);
            }

            // Multi-tile files
            for(Node image_node : XMLGetChildrenByName(tileset, "image")) {
                Element image_element = (Element) image_node;
                TileImage tile = new TileImage();

                // Load image file
                tile.file = image_element.getAttribute("source");
                tile.image = GameGraphics.createBufferedImage(GameGraphics.getImage(directory+tile.file));

                // Apply transparency
                Color trans = new Color(Integer.parseInt(image_element.getAttribute("trans"), 16));
                tile.image = GameGraphics.createBufferedImage(removeBackgroundColor(tile.image, trans));

                // Set properties
                tile.first_gid = first_gid;
                tile.columns = Integer.parseInt(tileset_element.getAttribute("columns"));
                tile.tile_count = Integer.parseInt(tileset_element.getAttribute("tilecount"));
                tile.columns = Integer.parseInt(tileset_element.getAttribute("columns"));
                tile.spacing = Integer.parseInt(tileset_element.getAttribute("spacing"));
                tile.margin = Integer.parseInt(tileset_element.getAttribute("margin"));

                this.tiles.add(tile);
            }

            // Grab attribute values
            this.name = tileset_element.getAttribute("name");
            this.tile_width = Integer.parseInt(tileset_element.getAttribute("tilewidth"));
            this.tile_height = Integer.parseInt(tileset_element.getAttribute("tileheight"));
        }

        /**
         * @param gid The ID of a tile in this set.
         * @return An {@link Image} with width {@code tile_width} and height {@code tile_height}.
         */
        public Image getTile(int gid) {
            for(TileImage ti : tiles) {
                if(gid >= ti.first_gid && gid < ti.first_gid + ti.tile_count) {
                    int new_gid = gid - ti.first_gid;
                    int grid_x = new_gid % ti.columns;
                    int grid_y = new_gid / ti.columns;
                    int x = ti.margin + (tile_width + ti.spacing) * grid_x;
                    int y = ti.margin + (tile_height + ti.spacing) * grid_y;

                    return ti.image.getSubimage(x, y, Math.min(ti.image.getWidth(), tile_width), Math.min(ti.image.getHeight(), tile_height));
                }
            }

            return null;
        }

        /* Helper functions */

        /**
         * Makes all pixels of a specified color transparent in a given {@link BufferedImage}.
         */
        private Image removeBackgroundColor(BufferedImage image, Color color) {
            ImageFilter filter = new RGBImageFilter() {
                private final int alpha_mask = 0xFF000000;
                private final int trans_rgb = color.getRGB() | alpha_mask;

                @Override
                public int filterRGB(int x, int y, int rgb) {
                    if((rgb | alpha_mask) == trans_rgb) {
                        return 0;
                    }
                    else {
                        return rgb;
                    }
                }
            };

            ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
            return Toolkit.getDefaultToolkit().createImage(ip);
        }
    }
}
