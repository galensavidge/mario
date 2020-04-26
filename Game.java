import java.util.*;

/**
 * The object that handles the main update and draw loops in the game.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Game
{
    private static final ArrayList<GameObject> update_queue = new ArrayList<>();
    private static final ArrayList<GameObject> draw_queue = new ArrayList<>();
    
    private static boolean running;
    private static long step_time = 0;
    private static int target_fps = 60;

    /**
     * Sets the target frame rate of the game.
     * @param fps Target frame rate in frames per second.
     */
    public static void setTargetFPS(int fps) {
        target_fps = fps;
    }

    /**
     * Adds an object at the proper places in the update and draw queues.
     * @param object The GameObject or object of a derived class.
     */
    public static void addObject(GameObject object)
    {
        // Insert o into the update queue
        int i = 0;
        while(i < update_queue.size()) {
            if(update_queue.get(i).getPriority() <= object.getPriority()) {
                break;
            }
            i++;
        }
        update_queue.add(i, object);

        // Insert o into the draw queue
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
     * Removes an object from the update and draw queues.
     * @param object The GameObject or object of a derived class.
     */
    public static void removeObject(GameObject object) {
        update_queue.remove(object);
        draw_queue.remove(object);
    }

    /**
     * Removes all non-persistent objects from the update and draw queues.
     */
    public static void clearNonPersistentObjects() {
        update_queue.removeIf(o -> !o.isPersistent());
        draw_queue.removeIf(o -> !o.isPersistent());
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
                o.update();
            }
            
            // Iterate over draw queue
            i = draw_queue.listIterator();
            while(i.hasNext()) {
                GameObject o = i.next();
                o.draw();
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
            step_time = System.nanoTime() - start_time; // Update step time
        }
    }

    /**
     * Stops the game loop after the current step is complete.
     */
    public static void stop() {
        running = false;
    }

    /**
     * Returns time of the last step. Returns 0 if a step has not been completed yet.
     * @return Step time in nanoseconds.
     */
    public static long stepTime() {
        return step_time;
    }
}
