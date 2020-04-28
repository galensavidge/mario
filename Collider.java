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
    protected Vector2 offset; // The coordinates of this collider relative to its attached object

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
     * Checks for collisions with other Colliders at position p.
     * @param absolute True to use absolute coordinates, false for coordinates relative to the attached PhysicsObject.
     * @return If a collision occurs, the PhysicsObject attached to the other collider, otherwise null.
     */
    public PhysicsObject checkCollision(Vector2 p, boolean absolute) {
        if(!absolute) {
            p = p.add(object.position);
        }

        for(Collider c : colliders) {
            if(c != this) {
                boolean result = false;
                if(this.collidesWith(p, c)) {
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
    public abstract boolean collidesWith(Vector2 p, Collider c);

    /**
     * Finds the minimum distance needed to move in a given direction to no longer be colliding with c.
     * @param direction A vector defining the direction in which to move.
     * @return The vector needed to move outside of c.
     */
    public abstract Vector2 moveOutside(Collider c, Vector2 direction);
}
