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
    /** Static GameGraphics class variables */
    private static GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();

    private static JFrame frame;

    private static Canvas canvas;

    private static BufferedImage buffer;
    private static BufferStrategy strategy;
    private static Graphics2D bufferGraphics; // Render shapes and sprites to this

    private static Graphics2D graphics;

    private static int window_width;
    private static int window_height;
    private static int window_scale;

    /** Static GrameGraphics methods */
    public static void initWindow(String title, int width, int height, int scale) {
        // Set window class variables
        window_width = width;
        window_height = height;
        window_scale = scale;

        // Set up JFrame
        frame = new JFrame(title);
        frame.addWindowListener(new GameGraphics.FrameClose());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(width * scale, height * scale);
        frame.setVisible(true);

        // Set up Canvas
        canvas = new Canvas(config);
        canvas.setSize(width * scale, height * scale);
        frame.add(canvas, 0);

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

    public static void updateGraphics() {
        try {
            graphics = (Graphics2D) strategy.getDrawGraphics();
        } catch (IllegalStateException e) {
            graphics = null;
            System.out.println("Exception in GameGraphics, updateGraphics(), graphics section!");
        }

        // Draw buffer to screen
        graphics.drawImage(buffer, 0, 0, window_width*window_scale, window_height*window_scale,
                0, 0, window_width, window_height, null);

        graphics.dispose();
        graphics = null;

        try {
            strategy.show();
            Toolkit.getDefaultToolkit().sync();
            // if(strategy.contentsLost())

        } catch (NullPointerException | IllegalStateException e) {
            System.out.println("Exception in GameGraphics, updateGraphics(), strategy section!");
        }
    }

    public static Frame getFrame() {
        return frame;
    }

    public static Canvas getCanvas() {
        return canvas;
    }

    public static int getWindowWidth() {
        return window_width;
    }

    public static int getWindowHeight() {
        return window_height;
    }

    public static int getWindowScale() {
        return window_scale;
    }

    public static void drawRectangle(int x, int y, int width, int height) {
        bufferGraphics.setColor(Color.BLUE);
        bufferGraphics.fillRect(x, y, width, height);
    }


    /** GameGraphics game object */

    /**
     * Instantiable GameGraphics class: should not be created by the user. Is instantiated by
     * {@link #initWindow(String, int, int, int)}.
     */
    public GameGraphics() {
        super(0, Integer.MAX_VALUE); // Put this object at the very bottom of the draw queue
    }

    public void draw() {
        GameGraphics.updateGraphics();
    }
}


/**
class Test extends Thread {
    private boolean isRunning = true;
    private Canvas canvas;
    private BufferStrategy strategy;
    private BufferedImage background;
    private Graphics2D backgroundGraphics;
    private Graphics2D graphics;
    private JFrame frame;
    private int width = 320;
    private int height = 240;
    private int scale = 1;
    private GraphicsConfiguration config =
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();

    // create a hardware accelerated image
    public final BufferedImage create(final int width, final int height,
                                      final boolean alpha) {
        return config.createCompatibleImage(width, height, alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    }

    // Setup
    public Test() {
        // JFrame
        frame = new JFrame();
        frame.addWindowListener(new FrameClose());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(width * scale, height * scale);
        frame.setVisible(true);

        // Canvas
        canvas = new Canvas(config);
        canvas.setSize(width * scale, height * scale);
        frame.add(canvas, 0);

        // Background & Buffer
        background = create(width, height, false);
        canvas.createBufferStrategy(2);
        do {
            strategy = canvas.getBufferStrategy();
        } while (strategy == null);
        start();
    }

    private class FrameClose extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            isRunning = false;
        }
    }

    // Screen and buffer stuff
    private Graphics2D getBuffer() {
        if (graphics == null) {
            try {
                graphics = (Graphics2D) strategy.getDrawGraphics();
            } catch (IllegalStateException e) {
                return null;
            }
        }
        return graphics;
    }

    private boolean updateScreen() {
        graphics.dispose();
        graphics = null;
        try {
            strategy.show();
            Toolkit.getDefaultToolkit().sync();
            return (!strategy.contentsLost());

        } catch (NullPointerException | IllegalStateException e) {
            return true;
        }
    }

    public void run() {
        backgroundGraphics = (Graphics2D) background.getGraphics();
        long fpsWait = (long) (1.0 / 30 * 1000);
        main: while (isRunning) {
            long renderStart = System.nanoTime();
            updateGame();

            // Update Graphics
            do {
                if (!isRunning) {
                    break main;
                }
                // DRAW LOOP
                renderGame(backgroundGraphics); // this calls your draw method

                // WRITE TO BUFFER
                Graphics2D bg = getBuffer();
                // thingy
                if (scale != 1) {
                    bg.drawImage(background, 0, 0, width * scale, height
                            * scale, 0, 0, width, height, null);
                } else {
                    bg.drawImage(background, 0, 0, null);
                }
                bg.dispose();
            } while (!updateScreen());

            // Better do some FPS limiting here
            long renderTime = (System.nanoTime() - renderStart) / 1000000;
            try {
                Thread.sleep(Math.max(0, fpsWait - renderTime));
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
            renderTime = (System.nanoTime() - renderStart) / 1000000;

        }
        frame.dispose();
    }

    public void updateGame() {
        // update game logic here
    }

    public void renderGame(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
    }

    public static void main(final String args[]) {
        new Test();
    }
}
 */