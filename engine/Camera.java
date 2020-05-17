package engine;

import engine.objects.GameObject;
import engine.objects.PhysicsObject;
import engine.util.Vector2;

/**
 * An object that moves the camera to track another object.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class Camera extends GameObject {

    public PhysicsObject anchor;

    /**
     * @param anchor The object to track. Set to {@code null} for no tracking.
     */
    public Camera(PhysicsObject anchor) {
        super(Game.camera_priority, 0);
        this.suspend_tier = Integer.MAX_VALUE;
        this.anchor = anchor;
        this.update();
    }

    /**
     * Sets the position of the camera, checking the bounds of the game world.
     *
     * @param position The desired position of the center of the camera.
     */
    public void setPosition(Vector2 position) {
        int x = (int)(position.x - GameGraphics.getWindowWidth()/2.0);
        x = Integer.max(0, x);
        x = Integer.min(World.getWidth() - GameGraphics.getWindowWidth(), x);
        int y = (int)(position.y - GameGraphics.getWindowHeight()/2.0);
        y = Integer.max(0, y);
        y = Integer.min(World.getHeight() - GameGraphics.getWindowHeight(), y);
        GameGraphics.moveCamera(x, y, true);
    }

    @Override
    public void update() {
        if(anchor != null) {
            setPosition(anchor.position);
        }
    }
}
