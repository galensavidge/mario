/**
 * A circular collider object. Defined by a radius.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public class CircleCollider extends Collider {
    private double radius;

    /**
     * A circle shaped collider object. Attaches by the top left corner of its bounding box.
     *
     * @param object The PhysicsObject to attach to.
     * @param radius Radius of the circle.
     * @param x_offset Relative x position of the collider with respect to object.
     * @param y_offset Relative y position of the collider with respect to object.
     */
    public CircleCollider(PhysicsObject object, double radius, double x_offset, double y_offset) {
        super(object);
        this.radius = radius;
        this.offset = new Vector2(x_offset, y_offset);
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean collidesWithCircle(Vector2 p, CircleCollider other) {
        // This collider's center
        Vector2 center = p.add(offset).add(radius); // p + offset + radius

        // Other collider's center
        Vector2 other_center = other.object.position.add(other.offset).add(other.radius);

        // Distance between their centers
        Vector2 dist_v = center.subtract(other_center);

        // Check whether the objects are intersecting
        return dist_v.abs() < (radius + other.radius);
    }

    public boolean collidesWithBox() {
        return false;
    }

    @Override
    public boolean collidesWith(Vector2 p, Collider collider) {
        if(collider instanceof CircleCollider) {
            return collidesWithCircle(p, (CircleCollider)collider);
        }
        return false; // Unhandled case
    }
}
