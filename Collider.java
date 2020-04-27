import java.util.ArrayList;

/**
 * The base class for collider objects, which are used to create and check hitboxes for game objects.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public abstract class Collider {
    protected static ArrayList<Collider> colliders = new ArrayList<>();
    
    protected PhysicsObject object; // The object that this collider is attached to
    protected double x_offset, y_offset; // The coordinates of this collider relative to its attached object

    /**
     * @param object The game object that this collider will attach to.
     */
    public Collider(PhysicsObject object) {
        this.object = object;
        colliders.add(this);
    }

    /**
     * Removes this collider from the global colliders list and removes its reference to the attached PhysicsObject.
     */
    public void delete() {
        colliders.remove(this);
        this.object = null;
    }

    /**
     * Checks for collisions with other Colliders at position (x, y).
     * @param absolute True to use absolute coordinates, false for coordinates relative to the attached PhysicsObject.
     * @return If a collision occurs, the PhysicsObject attached to the other collider, otherwise null.
     */
    public PhysicsObject checkCollision(double x, double y, boolean absolute) {
        boolean found_collision = false;
        double try_x, try_y;

        if(absolute) {
            try_x = x;
            try_y = y;
        }
        else {
            try_x = object.x + x;
            try_y = object.y + y;
        }

        for(Collider c : colliders) {
            if(c != this) {
                boolean result = false;
                if(this.collidesWith(try_x, try_y, c)) {
                    return c.object;
                }
            }
        }

        return null;
    }

    /**
     * Checks if this collider at position (x, y) collides with c. This function should be overridden to handle
     * collisions with different collider types.
     */
    public abstract boolean collidesWith(double x, double y, Collider collider);
}
