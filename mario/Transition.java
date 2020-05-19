package mario;

import engine.Game;
import engine.graphics.GameGraphics;
import engine.objects.GameObject;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Plays a transition effect and calls a passed lambda function when transition is complete.
 *
 * @author Galen Savidge
 * @version 5/18/2020
 */

public class Transition extends GameObject {

    /**
     * Used by the pixel type transitions to determine the highest level of pixelation.
     */
    private static final int max_pixel_size = Mario.getGridScale();

    public enum Type {
        FADE_IN,
        FADE_OUT,
        PIXEL_IN,
        PIXEL_OUT
    }

    /**
     * Generic lambda interface for animation finish event calls.
     */
    public interface EventPointer {
        void call();
    }

    private int timer;
    private final int transition_frames;
    private final Type transition_type;
    private final EventPointer event;

    /**
     * @param transition_time        Time to complete the transition in seconds.
     * @param transition_type        A value from {@link Transition.Type}.
     * @param animation_finish_event A lambda function that will be called when the animation completes.
     */
    public Transition(double transition_time, Type transition_type, EventPointer animation_finish_event) {
        super(0, Mario.transition_layer);
        this.suspend_tier = Mario.transition_suspend_tier;
        this.transition_frames = (int)(transition_time*Mario.fps);
        this.transition_type = transition_type;
        this.timer = transition_frames;
        this.event = animation_finish_event;
        Game.setSuspendTier(Mario.transition_suspend_tier);
    }

    /**
     * Resets the transition to the beginning.
     */
    public void reset() {
        timer = transition_frames;
    }


    /* Events */

    @Override
    public void update() {
        timer--;
        if(timer == 0) {
            event.call();
            this.delete();
        }
    }

    @Override
    public void draw() {
        int alpha;
        Color c;
        int pixel_size;
        switch(transition_type) {
            case FADE_IN:
                alpha = 0xFF*timer/transition_frames;
                c = new Color(alpha<<24, true);
                GameGraphics.drawRectangle(0, 0, GameGraphics.getWindowWidth(), GameGraphics.getWindowHeight(), true,
                        c);
                break;
            case FADE_OUT:
                alpha = 0xFF - 0xFF*timer/transition_frames;
                c = new Color(alpha<<24, true);
                GameGraphics.drawRectangle(0, 0, GameGraphics.getWindowWidth(), GameGraphics.getWindowHeight(), true,
                        c);
                break;
            case PIXEL_IN:
                pixel_size = max_pixel_size*timer/transition_frames + 1;
                drawPixelGrid(pixel_size);
                break;
            case PIXEL_OUT:
                pixel_size = max_pixel_size - max_pixel_size*timer/transition_frames + 1;
                drawPixelGrid(pixel_size);
            default:
                break;
        }
    }


    /* Helper functions */

    private static int averagePixels(int[] pixels) {
        int sum = 0;
        for(int p : pixels) {
            sum += p;
        }
        return sum/pixels.length;
    }

    private static void drawPixelGrid(int pixel_size) {
        BufferedImage buffer = GameGraphics.getBuffer();
        int y;
        for(y = 0;y < buffer.getHeight();y += pixel_size) {
            int x;
            Color c;
            for(x = 0;x < buffer.getWidth();x += pixel_size) {
                int x_pixel = Math.min(x + pixel_size/2, (x + buffer.getWidth())/2 - 1);
                int y_pixel = Math.min(y + pixel_size/2, (y + buffer.getHeight())/2 - 1);
                c = new Color(buffer.getRGB(x_pixel, y_pixel));
                GameGraphics.drawRectangle(x, y, pixel_size, pixel_size, true, c);
            }
        }
    }
}
