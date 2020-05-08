package mario.objects;

import engine.GameGraphics;
import engine.objects.PhysicsObject;
import mario.Mario;

/**
 * The base class for objects that exist in the Mario game world.
 *
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class WorldObject extends PhysicsObject {
    public WorldObject(int priority, int layer, double x, double y) {
        super(priority, layer, x, y);
    }

    @Override
    public void update() {

    }

    /**
     * @return True if the object is completely off the screen.
     */
    protected boolean isOnScreen() {
        return position.x <= GameGraphics.camera_x + GameGraphics.getWindowWidth()
                && position.y <= GameGraphics.camera_y + GameGraphics.getWindowHeight()
                && position.x >= GameGraphics.camera_x - Mario.getGridScale()
                && position.y >= GameGraphics.camera_y - Mario.getGridScale();
    }

    @Override
    public void draw() {

    }
}
