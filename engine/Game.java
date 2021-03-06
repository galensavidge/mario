package engine;

import engine.objects.*;
import java.util.*;

/**
 * The object that handles the main update and draw loops in the game.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class Game {

    /**
     * A list of all objects in the game sorted by {@code priority}. Defines the order in which objects' {@code update}
     * methods are called.
     */
    private static final ArrayList<GameObject> update_queue = new ArrayList<>();

    /**
     * A list of all objects in the game sorted by {@code layer}. Defines the order in which objects' {@code draw}
     * methods are called.
     */
    private static final ArrayList<GameObject> draw_queue = new ArrayList<>();

    /**
     * A stack of new objects to be added to {@link #update_queue} and {@link #draw_queue} once this step is complete.
     */
    private static final Stack<GameObject> new_objects = new Stack<>();
    
    private static boolean running;
    private static int current_suspend_tier = 0;
    private static boolean use_frame_time;
    private static long step_time = 0; // In nanoseconds
    private static int target_fps = 60;

    public static final int inputmanager_priority = 1000;
    public static final int camera_priority = 100;
    public static final int gamegraphics_layer = Integer.MAX_VALUE;
    public static final int background_layer = Integer.MIN_VALUE;

    /**
     * Sets which objects are suspended. Any objects with {@code suspend_tier} less than {@code tier} will not receive
     * update events.
     */
    public static void setSuspendTier(int tier) {
        if(tier < 0) {
            tier = 0;
        }
        current_suspend_tier = tier;
    }

    /**
     * Sets the target frame rate of the game.
     * @param fps Target frame rate in frames per second.
     */
    public static void setTargetFPS(int fps) {
        target_fps = fps;
    }

    /**
     * Set the game to use the target frame delta for time deltas in the game rather than real time measurement. If set
     * to true, calls to {@link #stepTimeNanos} and {@link #stepTimeSeconds} return the target time delta between
     * frames as defined by {@link #setTargetFPS}. If set to false, time deltas are calculated from real time
     * measurements of time between updates.
     */
    public static void setUseFrameTime(boolean use_frame_time) {
        Game.use_frame_time = use_frame_time;
    }

    /**
     * Adds an object to the {@link #new_objects} stack.
     * @param object The {@link GameObject} or object of a child class.
     */
    public static void addObject(GameObject object) {
        new_objects.push(object);
    }

    /**
     * Adds an object at the proper places in {@link #update_queue} and {@link #draw_queue}.
     * @param object The {@link GameObject} or object of a child class.
     */
    private static void addToQueues(GameObject object) {
        if(object.isDeleted()) {
            return;
        }

        // Insert object into the update queue
        int i = 0;
        while(i < update_queue.size()) {
            if(update_queue.get(i).getPriority() <= object.getPriority()) {
                break;
            }
            i++;
        }
        update_queue.add(i, object);

        // Insert object into the draw queue
        i = 0;
        while(i < draw_queue.size()) {
            if(draw_queue.get(i).getLayer() >= object.getLayer()) {
                break;
            }
            i++;
        }
        draw_queue.add(i, object);
    }

    /**
     * Deletes all non-persistent {@code GameObject} instances. These objects will be removed from the update and
     * draw queues and will also free any additional resources they are using.
     */
    public static void clearNonPersistentObjects() {
        for(GameObject o : update_queue) {
            if(!o.isPersistent()) {
                o.delete();
            }
        }

        for(GameObject o : new_objects) {
            if(!o.isPersistent()) {
                new_objects.remove(o);
                o.delete();
            }
        }
    }

    /**
     * Returns the list iterator for the update queue.
     */
    public static ListIterator<GameObject> updateQueueIterator() {
        return update_queue.listIterator();
    }

    /**
     * Runs the game loop until {@link #stop()} is called.
     */
    public static void run() {
        running = true;
        while(running) {
            // Record step start time
            long start_time = System.nanoTime();

            // Iterate over update queue
            ListIterator<GameObject> i = update_queue.listIterator();
            while(i.hasNext()) {
                GameObject o = i.next();
                if(!o.isDeleted() && o.getSuspendTier() >= current_suspend_tier) {
                    o.update();
                }
            }
            
            // Iterate over draw queue
            i = draw_queue.listIterator();
            while(i.hasNext()) {
                GameObject o = i.next();
                if(!o.isDeleted() && o.visible) {
                    o.draw();
                }
            }

            // Remove deleted objects from update and draw queues
            for(GameObject o : update_queue) {
                if(o.isDeleted()) {
                    o.deleteEvent();
                }
            }
            update_queue.removeIf(GameObject::isDeleted);
            draw_queue.removeIf(GameObject::isDeleted);

            // Add new objects to the queues from the new object stack
            while(new_objects.size() > 0) {
                addToQueues(new_objects.pop());
            }

            // Sleep to save CPU cycles
            long update_time = System.nanoTime() - start_time;
            long target_ns = (long) (1e9/target_fps);
            if(update_time < target_ns) {
                try {
                    Thread.sleep((target_ns - update_time) / (long)1e6);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Update step time
            if(use_frame_time) {
                step_time = target_ns;
            }
            else {
                step_time = System.nanoTime() - start_time;
            }
        }
    }

    /**
     * Stops the game loop after the current step is complete.
     */
    public static void stop() {
        running = false;
    }

    /**
     * Returns the total time spent in the last step. Returns 0 if a step has not been completed yet.
     * @return Step time in nanoseconds.
     */
    public static long stepTimeNanos() {
        return step_time;
    }

    /**
     * Returns the total time spent in the last step. Returns 0 if a step has not been completed yet.
     * @return Step time in seconds.
     */
    public static double stepTimeSeconds() {
        return (double)step_time/1e9;
    }
}
