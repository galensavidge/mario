import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class InputManager extends GameObject {

    /* Public InputManager variables and methods */

    // Keybinds
    public static final int K_LEFT = KeyEvent.VK_A;
    public static final int K_RIGHT = KeyEvent.VK_D;
    public static final int K_UP = KeyEvent.VK_W;
    public static final int K_DOWN = KeyEvent.VK_S;

    public static void init() {
        // Set up key list
        InputManager.keys.put(InputManager.K_LEFT, new Key());
        InputManager.keys.put(InputManager.K_RIGHT, new Key());
        InputManager.keys.put(InputManager.K_UP, new Key());
        InputManager.keys.put(InputManager.K_DOWN, new Key());

        // Set up key listener
        GameGraphics.getFrame().addKeyListener(InputManager.key_listener);

        // Add an InputManager to the update queue
        new InputManager();
    }

    public static boolean getDown(int key) {
        return keys.get(key).down;
    }

    public static boolean getPressed(int key) {
        return keys.get(key).pressed;
    }

    public static boolean getReleased(int key) {
        return keys.get(key).released;
    }

    /* Internal variables and methods */

    protected static final Dictionary<Integer, Key> keys = new Hashtable<>();

    protected static final KeyListener key_listener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            // Do nothing here
        }

        @Override
        public void keyPressed(KeyEvent e) {
            Key k = keys.get(e.getKeyCode());
            k.down = true;
            k.pressed = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Key k = keys.get(e.getKeyCode());
            k.down = false;
            k.released = true;
        }
    };

    private static class Key {
        boolean down; // True if the key is currently pressed down
        boolean pressed; // True if the key was pressed this step
        boolean released; // True if the key was released this step
    }

    /* InputManager object */

    /**
     * This object has low priority; it should go *after* all objects in the update queue that need to read key presses.
     */
    private InputManager() {
        super(-100,0);
    }

    @Override
    public void update() {
        // Clear pressed/released flags
        Enumeration<Key> e = keys.elements();
        while(e.hasMoreElements()) {
            Key k = e.nextElement();
            k.pressed = false;
            k.released = false;
        }
    }

    @Override
    public void draw() {

    }
}
