package mario;

import engine.Game;
import engine.GameGraphics;
import engine.objects.GameObject;

import java.awt.*;

public class Transition extends GameObject {

    public enum Type {
        FADE_IN,
        FADE_OUT
    }

    private int timer;
    private final int transition_frames;
    private Type transition_type;

    public Transition(double transition_time, Type transition_type) {
        super(0, Mario.transition_layer);
        this.suspend_tier = Mario.transition_suspend_tier;
        this.transition_frames = (int)(transition_time*Mario.fps);
        this.transition_type = transition_type;
        this.timer = transition_frames;
        Game.setSuspendTier(Mario.transition_suspend_tier);
    }

    public void reset() {
        timer = transition_frames;
    }

    @Override
    public void update() {
        timer--;
        if(timer == 0) {
            GameController.animationFinishedEvent();
            this.delete();
        }
    }

    @Override
    public void draw() {
        int alpha = 0;
        if(transition_type == Type.FADE_IN) {
            alpha = 0xFF*timer/transition_frames;
        }
        else if(transition_type == Type.FADE_OUT) {
            alpha = 0xFF - 0xFF*timer/transition_frames;
        }
        Color c = new Color(alpha<<24, true);
        GameGraphics.drawRectangle(0, 0, GameGraphics.getWindowWidth(), GameGraphics.getWindowHeight(), true, c);
    }
}
