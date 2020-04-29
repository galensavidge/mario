import java.util.ArrayList;

/**
 * The base class for collider objects, which are used to create and check hitboxes for game objects.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public abstract class Collider {
    /* Static class methods */

    protected static ArrayList<Collider> colliders = new ArrayList<>();


    /* Instance methods */

    protected Vector2 position; // The coordinates of this collider relative to its attached object
    protected PhysicsObject object; // The object this collider is attached to

    public Collider(PhysicsObject object) {
        colliders.add(this);
        this.object = object;
        this.position = Vector2.zero();
    }

    /**
     * @return The position of the top left corner of the collider's bounding box.
     */
    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position.round();
    }

    /**
     * Removes this collider from the global colliders list and removes its reference to the attached PhysicsObject.
     */
    public void delete() {
        colliders.remove(this);
        this.object = null;
    }

    public abstract void draw();

    /**
     * Checks for collisions with other Colliders.
     * @return An ArrayList of all physics objects collided with.
     */
    public ArrayList<PhysicsObject> getCollisions() {
        ArrayList<PhysicsObject> collisions = new ArrayList<>();

        for(Collider c : colliders) {
            if(c != this) {
                if(this.collidesWith(position, c)) {
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
    public abstract boolean collidesWith(Vector2 p, Collider c);
}
