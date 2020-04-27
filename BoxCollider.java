import java.awt.geom.Point2D;

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
        this.offset = new Vector2(x_offset, y_offset);
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public boolean collidesWithCircle(Vector2 p, CircleCollider other) {
        return false;
    }

    public boolean collidesWithBox(Vector2 p, BoxCollider other) {
        // This object's corners, clockwise from top left
        Vector2 this_p = p.add(this.offset);
        Vector2[] this_verts = {this_p.copy(),
                this_p.add(new Vector2(this.width, 0)),
                this_p.add(new Vector2(this.width, this.height)),
                this_p.add(new Vector2(0, this.height))};

        // Other object's corners, clockwise from top left
        Vector2 other_p = other.object.position.add(other.offset);
        Vector2[] other_verts = {other_p.copy(),
                other_p.add(new Vector2(other.width, 0)),
                other_p.add(new Vector2(other.width, other.height)),
                other_p.add(new Vector2(0, other.height))};

        //Check if a vertex from self is inside other
        for (Vector2 this_vert : this_verts) {
            if (pointInBox(this_vert, other_verts[0], other_verts[2])) {
                return true;
            }
        }

        //Check if a vertex from other is inside self
        for (Vector2 other_vert : other_verts) {
            if (pointInBox(other_vert, this_verts[0], this_verts[2])) {
                return true;
            }
        }

        // Return false if no vertices from either box is inside the other
        return false;
    }

    /**
     * @param p A point.
     * @param b1 The top left corner of the box.
     * @param b2 The bottom right corner of the box.
     * @return True iff the p is inside the box defined by b1 and b2.
     */
    private boolean pointInBox(Vector2 p, Vector2 b1, Vector2 b2) {
        // Check if the point is strictly inside
        return p.x > b1.x && p.x < b2.x && p.y > b1.y && p.y < b2.y;
    }

    private boolean boxesCollinearIntersecting(Vector2 b1_p1, Vector2 b1_p2, Vector2 b2_p1, Vector2 b2_p2) {
        return false;
    }

    @Override
    public boolean collidesWith(Vector2 p, Collider collider) {
        if(collider instanceof CircleCollider) {
            return collidesWithCircle(p, (CircleCollider)collider);
        }
        else if(collider instanceof BoxCollider) {
            return collidesWithBox(p, (BoxCollider)collider);
        }
        return false; // Unhandled case(s)
    }
}
