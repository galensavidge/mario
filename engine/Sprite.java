package engine;

import java.awt.*;
import java.util.ArrayList;

/**
 * An animated sprite. Plays in a loop at a configurable speed.
 *
 * @author Galen Savidge
 * @version 5/17/2020
 */
public class Sprite {
    ArrayList<Image> frames = new ArrayList<>();
    private int current_frame;
    private int frame_time = 1;
    private int frame_counter;

    /**
     * @param filenames The names of the image files for the frames of the animation.
     */
    public Sprite(String[] filenames) {
        for(String filename : filenames) {
            frames.add(GameGraphics.getImage(filename));
        }
        reset();
    }

    /**
     * Sets the number of incrementFrame() calls before the current frame will change to the next image.
     */
    public void setFrameTime(int frames) {
        if(frames >= 1) {
            frame_time = frames;
        }
    }

    /**
     * Resets the animation to the first frame and the accumulated time to zero.
     */
    public void reset() {
        current_frame = 0;
        frame_counter = 1;
    }

    /**
     * Increments the internal counter used to determine when to switch to the next frame. Calling this once per step
     * runs the animation.
     */
    public void incrementFrame() {
        frame_counter++;
        if(frame_counter > frame_time) {
            frame_counter = 1;
            current_frame++;

            if(current_frame == frames.size()) {
                current_frame = 0;
            }
        }
    }

    /**
     * @return The current frame of the animation.
     */
    public Image getCurrentFrame() {
        return frames.get(current_frame);
    }
}
