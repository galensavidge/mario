package engine.colliders;
import engine.util.*;
import engine.objects.PhysicsObject;
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

    public static class Collision {
        public Collider collider;
        boolean collision_found;
        public ArrayList<Collider> collided_with = new ArrayList<>();
        public ArrayList<Vector2> intersections = new ArrayList<>();
        public ArrayList<Line> lines = new ArrayList<>();
        public Vector2 mean = null;
        public ArrayList<Vector2> normals = new ArrayList<>();

        public Collision(Collider collider) {
            this.collider = collider;
            this.collision_found = false;
        }
    }

    /**
     * Adds {@code element} to {@code array} iff {@code array} does not contain an element mathematically equal to
     * {@code element}.
     */
    protected static void addNoDuplicates(ArrayList<Vector2> array, Vector2 element) {
        boolean contains_element = false;
        for(Vector2 v : array) {
            if(v.equals(element)) {
                contains_element = true;
                break;
            }
        }
        if(!contains_element) {
            array.add(element);
        }
    }

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
     * @return A Collision object.
     */
    public Collision getCollisions() {
        Collision collision = new Collision(this);

        // Get intersections
        for(Collider collider : Collider.colliders) {
            if(collider != this) {
                this.checkCollision(collision, collider);
            }
        }

        return collision;
    }

    /**
     * Checks if this collider collides with other. This function should be overridden to handle collisions with
     * different collider types.
     */
    public abstract void checkCollision(Collision collision, Collider other);

    public Vector2 getNormal(Vector2 direction) {
        return null;
    }
}
