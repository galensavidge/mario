import java.util.ArrayList;

/**
 * The base class for collider objects, which are used to create and check hitboxes for game objects.
 *
 * @author Galen Savidge
 * @version 4/25/2020
 */
public abstract class Collider {
    protected static ArrayList<Collider> colliders = new ArrayList<>();

    protected GameObject object; // The object that this collider is attached to
    protected String type;

    protected double x_offset, y_offset; // The coordinates of this collider relative to its attached object

    /**
     * @param object The game object that this collider will attach to.
     */
    public Collider(GameObject object) {
        this.object = object;
        colliders.add(this);
    }

    public void delete() {
        colliders.remove(this);
        this.object = null;
    }

    public abstract boolean checkCollision(double x, double y, boolean absolute);
}
