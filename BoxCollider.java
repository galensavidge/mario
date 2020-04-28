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

    /* Collision checking for different collider types */

    public boolean collidesWithCircle(Vector2 box_p, Vector2 circle_p, CircleCollider circle, boolean allow_edges) {
        // Get top right and bottom left points on the box
        Vector2 p1 = box_p.add(this.offset);
        Vector2 p2 = p1.copy();
        p2.x += this.width;
        p2.y += this.height;

        // Get the center of the circle
        double r = circle.getRadius();
        Vector2 other_center = circle_p.add(r);

        double distance = Double.MAX_VALUE;

        // Closest point to the circle is on a top/bottom edge
        if(between(other_center.x, p1.x, p2.x)) {
            distance = Math.min(distance, Math.min(Math.abs(p1.y - other_center.y), Math.abs((p2.y - other_center.y))));
        }

        // Closest point to the circle is on a side edge
        else if(between(other_center.y, p1.y, p2.y)) {
            distance = Math.min(distance, Math.min(Math.abs(p1.x - other_center.x), Math.abs((p2.x - other_center.x))));
        }

        // Closest point to the circle is a corner
        else {
            Vector2[] corners = this.getCorners(box_p);
            for(Vector2 corner : corners) {
                distance = Math.min(distance, corner.subtract(other_center).abs());
            }
        }

        if(allow_edges && distance <= r) {
            return true;
        }
        else return !allow_edges && distance < r;
    }

    public boolean collidesWithBox(Vector2 this_p, BoxCollider other, boolean allow_edges) {
        // This object's corners, clockwise from top left
        Vector2[] this_corners = this.getCorners(this_p);

        // Other object's corners, clockwise from top left
        Vector2[] other_corners = other.getCorners(other.object.position);

        // Check if a corner from self is inside other
        for (Vector2 corner : this_corners) {
            if (pointInBox(corner, other_corners[0], other_corners[2], allow_edges)) {
                return true;
            }
        }

        // Check if a corner from other is inside self
        for (Vector2 corner : other_corners) {
            if (pointInBox(corner, this_corners[0], this_corners[2], allow_edges)) {
                return true;
            }
        }

        if(!allow_edges) {
            // Check if the boxes are collinear on the x or y axis and are intersecting
            return boxesCollinearIntersecting(this_corners[0], this_corners[2], other_corners[0], other_corners[2]);
        }
        return false;
    }

    /**
     * @param b1_p1 One corner of box 1.
     * @param b1_p2 A corner diagonal from b1_p1.
     * @param b2_p1 One corner of box 2.
     * @param b2_p2 A corner diagonal from b2_p1.
     * @return True iff the two boxes are intersecting and either: (1) the same height and at the same y position, or
     * (2) the same width and at the same x position.
     */
    private boolean boxesCollinearIntersecting(Vector2 b1_p1, Vector2 b1_p2, Vector2 b2_p1, Vector2 b2_p2) {
        // Collinear on x axis
        if((b1_p1.x == b2_p1.x && b1_p2.x == b2_p2.x) || (b1_p1.x == b2_p2.x && b1_p2.x == b2_p1.x)) {
            // Check if either point is on a top/bottom edge
            if(between(b1_p1.y, b2_p1.y, b2_p2.y) || between(b1_p2.y, b2_p1.y, b2_p2.y)) {
                return true;
            }
        }

        // Collinear on y axis
        if((b1_p1.y == b2_p1.y && b1_p2.y == b2_p2.y) || (b1_p1.y == b2_p2.y && b1_p2.y == b2_p1.y)) {
            // Check if either point is on a side edge
            if(between(b1_p1.x, b2_p1.x, b2_p2.x) || between(b1_p2.x, b2_p1.x, b2_p2.x)) {
                return true;
            }
        }

        return false;
    }

    /* Move to contact for different collider types */

    public Vector2 toContactBox(BoxCollider other, Vector2 direction) {
        Vector2[] this_corners = this.getCorners(this.object.position);
        Vector2[] other_corners = other.getCorners(other.object.position);
        Vector2 other_p1 = other_corners[0]; // Top left corner
        Vector2 other_p2 = other_corners[2]; // Bottom right corner

        Vector2 d = direction.multiply(1.0/direction.abs()); // Normalized direction vector

        Vector2 solution = null;

        for(Vector2 corner : this_corners) {
            if(d.y != 0) {
                /* Check top and bottom edges */
                double[] other_ypoints = {other_p1.y, other_p2.y};
                for(double o_y : other_ypoints) {
                    Vector2 test = new Vector2((o_y - corner.y) * d.x / d.y, o_y - corner.y);

                    // Check to make sure test is in the right direction
                    if (Math.signum(test.y) == Math.signum(d.y)) {
                        // Check if the move would put the corner on the edge of other
                        if (betweenInclusive(corner.x + test.x, other_p1.x, other_p2.x)) {
                            // Check if this solution is the shortest found so far
                            if (solution == null) {
                                solution = test;
                            }
                            else if (test.abs() < solution.abs()) {
                                solution = test;
                            }
                        }
                    }
                }
            }

            if(d.x != 0) {
                /* Check side edges */
                double[] other_xpoints = {other_p1.x, other_p2.x};
                for(double o_x : other_xpoints) {
                    Vector2 test = new Vector2(o_x - corner.x,(o_x - corner.x) * d.y / d.x);

                    // Check to make sure test is in the right direction
                    if (Math.signum(test.x) == Math.signum(d.x)) {
                        // Check if the move would put the corner on the edge of other
                        if (betweenInclusive(corner.y + test.y, other_p1.y, other_p2.y)) {
                            // Check if this solution is the shortest found so far
                            if (solution == null) {
                                solution = test;
                            }
                            else if (test.abs() < solution.abs()) {
                                solution = test;
                            }
                        }
                    }
                }
            }
        }

        return solution;
    }

    /* Utility */

    /**
     * Returns an array of this box's corners, starting from top left and going clockwise.
     * @param p The position of the collider.
     */
    public Vector2[] getCorners(Vector2 p) {
        Vector2 this_p = p.add(this.offset);
        return new Vector2[]{this_p.copy(),
                this_p.add(new Vector2(this.width, 0)),
                this_p.add(new Vector2(this.width, this.height)),
                this_p.add(new Vector2(0, this.height))};
    }

    /**
     * Returns true iff a is in (b, c).
     */
    private boolean between(double a, double b, double c) {
        return a > Math.min(b, c) && a < Math.max(b, c);
    }

    /**
     * Returns true iff a is in [b, c].
     */
    private boolean betweenInclusive(double a, double b, double c) {
        return a >= Math.min(b, c) && a <= Math.max(b, c);
    }

    /**
     * @param p A point.
     * @param b1 One corner of the box.
     * @param b2 A corner diagonal from b1.
     * @return True iff the p is strictly inside the box defined by b1 and b2.
     */
    private boolean pointInBox(Vector2 p, Vector2 b1, Vector2 b2, boolean allow_edges) {
        if(allow_edges) {
            return betweenInclusive(p.x, b1.x, b2.x) && betweenInclusive(p.y, b1.y, b2.y);
        }
        else {
            return between(p.x, b1.x, b2.x) && between(p.y, b1.y, b2.y);
        }
    }

    /* Overridden function from Collider */

    @Override
    public boolean collidesWith(Vector2 p, Collider collider, boolean allow_edges) {
        if(collider instanceof CircleCollider) {
            return collidesWithCircle(p, collider.object.position, (CircleCollider)collider, allow_edges);
        }
        else if(collider instanceof BoxCollider) {
            return collidesWithBox(p, (BoxCollider)collider, allow_edges);
        }
        return false; // Unhandled case(s)
    }

    @Override
    public Vector2 vectorToContact(Collider collider, Vector2 direction) {
        if(collider instanceof CircleCollider) {
            return null;
        }
        else if(collider instanceof BoxCollider) {
            return toContactBox((BoxCollider)collider,direction);
        }

        return null;
    }
}
