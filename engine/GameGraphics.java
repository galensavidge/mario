package engine;

import engine.objects.GameObject;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;

/**
 * Class with methods to set up the game window and draw things.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class GameGraphics extends GameObject {

    /* Static GameGraphics class variables */

    private static final GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();

    private static final JFrame frame = new JFrame();
    private static Canvas canvas;
    private static BufferedImage buffer;
    private static BufferStrategy strategy;
    private static Graphics2D bufferGraphics; // Render shapes and sprites to this

    // Width and height in pixels and the scaling factor used when drawing to the screen
    private static int window_width;
    private static int window_height;
    private static int window_scale;

    // Position of the window in the game world
    public static int camera_x;
    public static int camera_y;

    // Graphics constants
    private static int draw_scale = 1;

    // GameGraphics instance in the draw queue
    private static GameGraphics g;


    /* GameGraphics configuration methods */

    /**
     * @param title The name of the window.
     * @param width Width of the interior draw area in pixels.
     * @param height Height of the interior draw area in pixels.
     * @param scale Scaling factor between the interior draw area and how it is rendered on the monitor.
     */
    public static void initWindow(String title, int width, int height, int scale) {
        // Set window class variables
        window_width = width;
        window_height = height;
        window_scale = scale;

        // Set up JFrame
        frame.setTitle(title);
        frame.addWindowListener(new CustomWindow());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        //frame.setSize(width * scale, height * scale);
        frame.setVisible(true);
        frame.setResizable(false);

        // Set up Canvas
        canvas = new Canvas(config);
        canvas.setSize(width * scale, height * scale);
        frame.add(canvas, 0);
        frame.pack();

        // Set up buffer frame
        buffer = config.createCompatibleImage(width, height, Transparency.OPAQUE);
        canvas.createBufferStrategy(2);
        do {
            strategy = canvas.getBufferStrategy();
        } while (strategy == null);
        bufferGraphics = (Graphics2D) buffer.getGraphics();

        // Add GameGraphics object to the draw queue
        g = new GameGraphics();
    }

    /**
     * Closes the window. Should probably only be called when the game exits.
     */
    public static void closeWindow() {
        frame.dispose();
    }


    /* Accessors */

    public static int getWindowWidth() {
        return window_width;
    }

    public static int getWindowHeight() {
        return window_height;
    }

    public static int getWindowScale() {
        return window_scale;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static Canvas getCanvas() {
        return canvas;
    }

    public static void setDrawScale(int draw_scale) {
        if(draw_scale > 0) {
            GameGraphics.draw_scale = draw_scale;
        }
    }

    /* Camera */

    /**
     * Moves the camera to {@code (x, y)} in the game world.
     *
     * @param absolute If true sets absolute position; if false, sets position relative to current position.
     */
    public static void moveCamera(int x, int y, boolean absolute) {
        if(absolute) {
            camera_x = x;
            camera_y = y;
        }
        else {
            camera_x += x;
            camera_y += y;
        }
    }


    /* Functions to draw different things */

    /**
     * Draws a point at {@code (x, y)}.
     * @param absolute_position True to draw in the window coordinate space rather than the game coordinate space.
     */
    public static void drawPoint(int x, int y, boolean absolute_position, Color color) {
        if(!absolute_position) {
            x -= camera_x;
            y -= camera_y;
        }
        bufferGraphics.setColor(color);
        bufferGraphics.drawLine(x, y, x, y);
    }

    /**
     * Draws a line from {@code (x1, y1)} to {@code (x2, y2)}.
     * @param absolute_position True to draw in the window coordinate space rather than the game coordinate space.
     */
    public static void drawLine(int x1, int y1, int x2, int y2, boolean absolute_position, Color color) {
        if(!absolute_position) {
            x1 -= camera_x;
            y1 -= camera_y;
            x2 -= camera_x;
            y2 -= camera_y;
        }
        bufferGraphics.setColor(color);
        bufferGraphics.drawLine(x1, y1, x2, y2);
    }

    /**
     * @param x X position of top left corner.
     * @param y Y position of top left corner.
     * @param absolute_position True to draw in the window coordinate space rather than the game coordinate space.
     */
    public static void drawRectangle(int x, int y, int width, int height, boolean absolute_position, Color color) {
        if(!absolute_position) {
            x -= camera_x;
            y -= camera_y;
        }
        bufferGraphics.setColor(color);
        bufferGraphics.fillRect(x, y, width, height);
    }

    /**
     * @param x X position of top left corner.
     * @param y Y position of top left corner.
     * @param absolute_position True to draw in the window coordinate space rather than the game coordinate space.
     */
    public static void drawCircle(int x, int y, int radius, boolean absolute_position, Color color) {
        if(!absolute_position) {
            x -= camera_x;
            y -= camera_y;
        }
        bufferGraphics.setColor(color);
        bufferGraphics.fillOval(x, y, radius*2,radius*2);
    }

    /**
     * Loads the image corresponding to the passed file name.
     */
    public static Image getImage(String filename) {
        return Toolkit.getDefaultToolkit().getImage(filename);
    }

    /**
     * @param x X position of top left corner.
     * @param y Y position of top left corner.
     * @param absolute_position True to draw in the window coordinate space rather than the game coordinate space.
     * @param image An {@code Image} object, e.g. returned by a call to {@code GameGraphics.getImage}.
     */
    public static void drawImage(int x, int y, boolean absolute_position, Image image) {
        drawImage(x, y, absolute_position, false, false, draw_scale, image);
    }

    /**
     * @param x X position of top left corner.
     * @param y Y position of top left corner.
     * @param absolute_position True to draw in the window coordinate space rather than the game coordinate space.
     * @param image An {@code Image} object, e.g. returned by a call to {@code GameGraphics.getImage}.
     * @param flip_horizontal True to flip the image horizontally around its center axis.
     * @param flip_vertical True to flip the image horizontally around its center axis.
     * @param scale Scaling factor for drawing. 1: no scaling. 0: use default scaling as set by {@code setDrawScale}.
     */
    public static void drawImage(int x, int y, boolean absolute_position, boolean flip_horizontal,
                                 boolean flip_vertical, double scale, Image image) {
        if(!absolute_position) {
            x -= camera_x;
            y -= camera_y;
        }

        if(scale == 0) {
            scale = draw_scale;
        }

        int w = (int)(image.getWidth(null)*scale);
        int h = (int)(image.getHeight(null)*scale);

        int _x = x;
        int _y = y;

        if(flip_horizontal) {
            _x = _x + w;
            w = -w;
        }
        if(flip_vertical) {
            _y = _y + h;
            h = -h;
        }

        bufferGraphics.drawImage(image, _x, _y, w, h, null);
    }


    /* Private GameGraphics methods */

    /**
     * Writes the contents of the buffer frame to the window. Should be called after all other drawing is complete.
     */
    private static void updateGraphics() {
        Graphics2D graphics;
        try {
            graphics = (Graphics2D) strategy.getDrawGraphics();
        } catch (IllegalStateException e) {
            graphics = null;
            System.out.println("Exception in GameGraphics, updateGraphics(), graphics section!");
        }

        // Draw buffer to screen
        assert graphics != null;
        graphics.drawImage(buffer, 0, 0, window_width*window_scale, window_height*window_scale,
                0, 0, window_width, window_height, null);

        graphics.dispose();

        try {
            strategy.show();
            Toolkit.getDefaultToolkit().sync();

        } catch (NullPointerException | IllegalStateException e) {
            System.out.println("Exception in GameGraphics, updateGraphics(), strategy section!");
        }
    }

    private static class CustomWindow extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            Game.stop();
        }
    }


    /* Private GameGraphics game object */

    /**
     * Instantiable GameGraphics class: should not be created by the user. Is instantiated by
     * {@link #initWindow(String, int, int, int)} and calls {@link #updateGraphics()} at the end of each draw loop.
     */
    private GameGraphics() {
        super(0, Integer.MAX_VALUE); // Put this object at the very bottom of the draw queue
        this.persistent = true;
    }

    @Override
    public void update() {}

    @Override
    public void draw() {
        GameGraphics.updateGraphics();
    }
}