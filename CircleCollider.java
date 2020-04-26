/**
 * A circular collider object. Defined by a radius. Attaches by the top left corner of its bounding box.
 *
 * @author Galen Savidge
 * @version 4/25/2020
 */
public class CircleCollider extends Collider {
    public static final String colliderType = "Circle";
    private double radius;

    public CircleCollider(GameObject object, double radius) {
        super(object);
        this.radius = radius;
        this.type = CircleCollider.colliderType;
    }

    public boolean collidesWithCircle(double x, double y, CircleCollider other) {
        // This collider's center
        double cx = x + radius;
        double cy = y + radius;

        // Other collider's center
        double o_cx = other.object.x + other.radius;
        double o_cy = other.object.y + other.radius;

        // Distance between their centers
        double xdist = cx - o_cx;
        double ydist = cy - o_cy;
        double dist = Math.sqrt(xdist*xdist + ydist*ydist);

        // Check whether the objects are intersecting
        return dist < (radius + other.radius);
    }

    @Override
    public boolean checkCollision(double x, double y, boolean absolute) {
        boolean found_collision = false;
        double try_x, try_y;

        if(absolute) {
            try_x = x;
            try_y = y;
        }
        else {
            try_x = object.x + x;
            try_y = object.y + y;
        }

        for(Collider c : colliders) {
            if(c != this) {
                boolean result = false;
                if (c.type.equals(CircleCollider.colliderType)) {
                    result = collidesWithCircle(try_x, try_y, (CircleCollider) c);
                }
                found_collision = found_collision || result;
            }
        }

        return found_collision;
    }
}
