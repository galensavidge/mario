/**
 * A rectangular collider object. Defined by width and height.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public class BoxCollider extends Collider {
    private double width;
    private double height;

    /**
     * A circle shaped collider object. Attaches by the top left corner of its bounding box.
     *
     * @param object The PhysicsObject to attach to.
     * @param width Radius of the circle.
     * @param x_offset Relative x position of the collider with respect to object.
     * @param y_offset Relative y position of the collider with respect to object.
     */
    public BoxCollider(PhysicsObject object, double width, double height, double x_offset, double y_offset) {
        super(object);
        this.width = width;
        this.height = height;
        this.x_offset = x_offset;
        this.y_offset = y_offset;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public boolean collidesWithCircle(double x, double y, CircleCollider other) {
        return false;
    }

    public boolean collidesWithBox(double x, double y, BoxCollider other) {
        return false;
    }

    @Override
    public boolean collidesWith(double x, double y, Collider collider) {
        if(collider instanceof CircleCollider) {
            return collidesWithCircle(x, y, (CircleCollider)collider);
        }
        else if(collider instanceof BoxCollider) {
            return collidesWithBox(x, y, (BoxCollider)collider);
        }
        return false; // Unhandled case(s)
    }
}
