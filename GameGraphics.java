import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;

/**
 * Class with methods to set up and draw things in the game window.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class GameGraphics extends GameObject {
    /* Static GameGraphics class variables */
    private static final GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();

    private static JFrame frame = new JFrame();
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

    /* Static GameGraphics methods */

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
        frame.addWindowListener(new GameGraphics.FrameClose());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        //frame.setSize(width * scale, height * scale);
        frame.setVisible(true);
        frame.setResizable(false);

        // Set up Canvas
        Canvas canvas = new Canvas(config);
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
        new GameGraphics();
    }

    private static class FrameClose extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            Game.stop();
        }
    }

    public static void closeWindow() {
        frame.dispose();
    }

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
            // if(strategy.contentsLost())

        } catch (NullPointerException | IllegalStateException e) {
            System.out.println("Exception in GameGraphics, updateGraphics(), strategy section!");
        }
    }

    /* Accessor functions */
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

    /* Functions to draw different things */
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


    /* GameGraphics game object */

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