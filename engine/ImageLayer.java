package engine;

import engine.objects.GameObject;

import java.awt.*;


/**
 * Renders a fixed image in the level on a given layer with configurable position, scale, parallax, and tiling.
 *
 * @author Galen Savidge
 * @version 5/13/2020
 */
public class ImageLayer extends GameObject {

    private final double x, y, parallax_factor;
    private final boolean tile;
    private final Image image;
    private int width, height;
    private final int scale;

    /**
     * Creates an image layer at position {@code (x, y)} in the world.
     * @param layer See {@link GameObject}.
     * @param scale Integer scaling factor.
     * @param parallax_factor A scaling factor to apply to this layer's movement relative to the camera. A higher value
     *                        (> 1) will make the layer appear closer to the camera while a smaller value (0-1) will
     *                        make it appear farther in the background. Use 1 for no parallax scrolling.
     * @param tile True to tile the image to fill the window; false to render only one copy of the image.
     * @param file_name Path and name of the image file.
     */
    public ImageLayer(double x, double y, int layer, int scale, double parallax_factor, boolean tile, String file_name) {
        super(0, layer);
        this.x = x;
        this.y = y;
        this.scale = Math.max(1, scale);
        this.parallax_factor = parallax_factor;
        this.tile = tile;

        this.image = GameGraphics.getImage(file_name);
        do {
            width = image.getWidth(null)*scale;
            height = image.getHeight(null)*scale;
        }
        while(width < 0 || height < 0);
    }

    @Override
    public void draw() {
        double x = this.x - GameGraphics.camera_x*parallax_factor;
        do {
            double y = this.y - GameGraphics.camera_y*parallax_factor;
            do {
                GameGraphics.drawImage((int)x, (int)y, true, false, false, 0, scale, image);
                y += height;
            }
            while(y < GameGraphics.getWindowHeight() && tile);

            x += width;
        }
        while(x < GameGraphics.getWindowWidth() && tile);
    }
}
