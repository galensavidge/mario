import java.awt.geom.Point2D;

/**
 * A simple class to hold a point in 2D space with double precision.
 *
 * @author Galen Savidge
 * @version 4/27/2020
 */
public abstract class PhysicsObject extends GameObject {
    protected Collider collider;

    // Position
    public Vector2 position;

    // Velocity
    public Vector2 velocity;

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();
        position = position.add(velocity.multiply(t));
    }

    @Override
    public abstract void draw();

    @Override
    public void delete() {
        collider.delete();
        collider = null;
        super.delete();
    }
}

