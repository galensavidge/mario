package engine.graphics;

import engine.objects.GameObject;

import java.awt.*;
import java.util.ArrayList;

/**
 * An animated sprite. Plays in a loop at a configurable speed. By default starts playing immediately after
 * instantiation.
 *
 * @author Galen Savidge
 * @version 6/8/2020
 */
public class AnimatedSprite extends GameObject {
    ArrayList<Image> frames = new ArrayList<>();
    private int current_frame;
    private int frame_time;
    private int frame_counter;
    private boolean playing = true;

    /**
     * @param filenames The names of the image files for the frames of the animation.
     * @param suspend_tier See {@link GameObject}.
     */
    public AnimatedSprite(String[] filenames, int suspend_tier) {
        super(0, 0);
        init(filenames, suspend_tier, 1);
    }

    /**
     * @param filenames The names of the image files for the frames of the animation.
     * @param suspend_tier See {@link GameObject}.
     * @param frame_time See {@link #setFrameTime}.
     */
    public AnimatedSprite(String[] filenames, int suspend_tier, int frame_time) {
        super(0, 0);
        init(filenames, suspend_tier, frame_time);
    }

    private void init(String[] filenames, int suspend_tier, int frame_time) {
        for(String filename : filenames) {
            frames.add(GameGraphics.getImage(filename));
        }
        this.suspend_tier = suspend_tier;
        setFrameTime(frame_time);
        reset();
    }

    /**
     * Sets the number of steps before the current frame will change to the next image.
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

    /**
     * Plays or pauses the animation.
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public void update() {
        if(playing) {
            incrementFrame();
        }
    }
}
