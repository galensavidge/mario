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
     * @return The position of the top left corner of the collider's bounding box.
     */
    public Vector2 getPosition() {
        return object.position.add(offset);
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
     * @param allow_edges True to include edges and corners in collision calculation, false to use only intersection.
     * @return An ArrayList of all physics objects collided with.
     */
    public ArrayList<PhysicsObject> getCollisions(Vector2 position, boolean absolute, boolean allow_edges) {
        if(!absolute) {
            position = position.add(object.position);
        }

        ArrayList<PhysicsObject> collisions = new ArrayList<>();

        for(Collider c : colliders) {
            if(c != this) {
                if(this.collidesWith(position, c, allow_edges)) {
                    collisions.add(c.object);
                }
            }
        }

        return collisions;
    }

    /**
     * Checks if this collider collides with c when at position p. This function should be overridden to handle
     * collisions with different collider types.
     */
    public abstract boolean collidesWith(Vector2 p, Collider c, boolean allow_edges);

    /**
     * Finds the minimum distance needed to move in a given direction to touch collider c.
     * @param direction A vector defining the direction in which to move.
     * @return The vector needed to touch c. Returns null if no vector was found.
     */
    public abstract Vector2 vectorToContact(Collider collider, Vector2 direction);

    /**
     * @return A point on the edge of both this and collider, or null if none exists.
     */
    public abstract Vector2 pointOfContact(Collider collider);
}
