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
    private static long step_time = 1;
    private static int target_fps = 60;

    public static int window_width;
    public static int window_height;

    /**
     * Sets the target frame rate of the game.
     * @param fps Target frame rate in frames per second.
     */
    public static void setTargetFPS(int fps) {
        target_fps = fps;
    }

    /**
     * Adds an object at the proper places in the update and draw queues.
     */
    public static void addObject(GameObject object)
    {
        // Insert o at the proper place in the update queue
        int i = 0;
        while(i < update_queue.size()) {
            if(update_queue.get(i).getPriority() <= object.getPriority()) {
                break;
            }
            i++;
        }
        update_queue.add(i, object);
        
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
            System.out.println("----------");
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
     * Returns time of the last step. Returns 1 if a step has not been completed yet.
     * @return Step time in nanoseconds.
     */
    public static long stepTime() {
        return step_time;
    }
}
