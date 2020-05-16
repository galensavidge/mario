package engine;

import engine.objects.GameObject;
import engine.objects.PhysicsObject;
import engine.util.Vector2;

public class Camera extends GameObject {

    public PhysicsObject anchor;

    public Camera(PhysicsObject anchor) {
        super(Game.camera_priority, 0);
        this.suspend_tier = Integer.MAX_VALUE;
        this.anchor = anchor;
    }

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

    @Override
    public void draw() {

    }
}
