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
        public Line least_squares = null;
        public Vector2 mean = null;
        public Vector2 normal = null;

        public Collision(Collider collider) {
            this.collider = collider;
            this.collision_found = false;
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
                this.updateCollision(collision, collider);
            }
        }

        // Get least squares best fit line
        collision.least_squares = Line.leastSquares(collision.intersections);
        if(collision.least_squares == null) {
            return collision;
        }

        // Get mean point
        collision.mean = new Vector2(0, 0);
        for(Vector2 p : collision.intersections) {
            collision.mean.x += p.x;
            collision.mean.y += p.y;
        }
        collision.mean.x /= collision.intersections.size();
        collision.mean.y /= collision.intersections.size();

        Line raycast = new Line(collision.mean.subtract(collision.least_squares.RHNormal()),
                collision.mean.add(collision.least_squares.RHNormal()));

        for(Line l : collision.lines) {
            if(l.intersection(raycast) != null) {
                collision.normal = l.RHNormal();
            }
        }

        return collision;
    }

    /**
     * Checks if this collider collides with other. This function should be overridden to handle collisions with
     * different collider types.
     */
    public abstract void updateCollision(Collision collision, Collider other);
}
