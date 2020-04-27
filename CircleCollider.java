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
        this.x_offset = x_offset;
        this.y_offset = y_offset;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean collidesWithCircle(double x, double y, CircleCollider other) {
        // This collider's center
        double cx = x + x_offset + radius;
        double cy = y + y_offset + radius;

        // Other collider's center
        double o_cx = other.object.x + other.x_offset + other.radius;
        double o_cy = other.object.y + other.y_offset + other.radius;

        // Distance between their centers
        double xdist = cx - o_cx;
        double ydist = cy - o_cy;
        double dist = Math.sqrt(xdist*xdist + ydist*ydist);

        // Check whether the objects are intersecting
        return dist < (radius + other.radius);
    }

    public boolean collidesWithBox() {
        return false;
    }

    @Override
    public boolean collidesWith(double x, double y, Collider collider) {
        if(collider instanceof CircleCollider) {
            return collidesWithCircle(x, y, (CircleCollider)collider);
        }
        return false; // Unhandled case
    }
}
