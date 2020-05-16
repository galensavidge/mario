package engine;

import engine.objects.GameObject;
import java.awt.*;

/**
 * A class to draw a static background below the other objects in the draw queue.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class Background extends GameObject {

    private Color color = Color.DARK_GRAY;

    public Background() {
        super(0, Game.background_layer);
        this.persistent = true;
    }

    @Override
    public void update() {}

    @Override
    public void draw() {
        GameGraphics.drawRectangle(0,0, GameGraphics.getWindowWidth(), GameGraphics.getWindowHeight(),
                true, color);
    }
}