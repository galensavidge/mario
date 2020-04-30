package engine;

import engine.objects.GameObject;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Checks for input from the keyboard and stores keybinds.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public class InputManager extends GameObject {

    /* Public engine.InputManager variables and methods */

    // Keybinds
    public static final int K_LEFT = KeyEvent.VK_A;
    public static final int K_RIGHT = KeyEvent.VK_D;
    public static final int K_UP = KeyEvent.VK_W;
    public static final int K_DOWN = KeyEvent.VK_S;
    public static final int K_JUMP = KeyEvent.VK_J;

    /**
     * Sets up the static engine.InputManager class and adds an object to the update queue. Should be called once when the
     * program starts.
     */
    public static void init() {
        // Set up key list
        InputManager.keys.put(InputManager.K_LEFT, new Key());
        InputManager.keys.put(InputManager.K_RIGHT, new Key());
        InputManager.keys.put(InputManager.K_UP, new Key());
        InputManager.keys.put(InputManager.K_DOWN, new Key());
        InputManager.keys.put(InputManager.K_JUMP, new Key());

        // Set up key listener
        GameGraphics.getFrame().addKeyListener(InputManager.key_listener);

        // Add an engine.InputManager to the update queue
        new InputManager();
    }

    /* Public functions to check the state of the keys */

    /**
     * @param key A key value defined in engine.InputManager.
     * @return True if the key is currently pressed.
     */
    public static boolean getDown(int key) {
        return keys.get(key).down;
    }

    /**
     * @param key A key value defined in engine.InputManager.
     * @return True if the key was pressed this step.
     */
    public static boolean getPressed(int key) {
        return keys.get(key).pressed;
    }

    /**
     * @param key A key value defined in engine.InputManager.
     * @return True if the key was released this step.
     */
    public static boolean getReleased(int key) {
        return keys.get(key).released;
    }

    /* Internal variables and methods */

    /**
     * Internal class to hold the state of a key.
     */
    private static class Key {
        boolean down_event;
        boolean up_event;

        boolean down; // True if the key is currently pressed down
        boolean pressed; // True if the key was pressed this step
        boolean released; // True if the key was released this step
    }

    protected static final Dictionary<Integer, Key> keys = new Hashtable<>();

    protected static final KeyListener key_listener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            // Do nothing here
        }

        @Override
        public void keyPressed(KeyEvent e) {
            Key k = keys.get(e.getKeyCode());
            if(k != null) {
                k.down_event = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Key k = keys.get(e.getKeyCode());
            if(k != null) {
                k.up_event = true;
            }
        }
    };

    /* Private engine.InputManager object */

    /**
     * This object has high priority; it should go before all objects in the update queue that need to read key presses.
     */
    private InputManager() {
        super(1000,0);
        this.persistent = true;
    }

    @Override
    public void update() {
        Enumeration<Key> e = keys.elements();
        while(e.hasMoreElements()) {
            Key k = e.nextElement();

            // Record key state
            k.pressed = false;
            k.released = false;
            if(k.down_event && !k.down) {
                k.down = true;
                k.pressed = true;
            }
            if(k.up_event && k.down) {
                k.down = false;
                k.released = true;
            }

            // Reset event flags
            k.down_event = false;
            k.up_event = false;
        }
    }

    @Override
    public void draw() {}
}
