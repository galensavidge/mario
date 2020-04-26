/**
 * A circular collider object. Defined by a radius. Attaches by the top left corner of its bounding box.
 *
 * @author Galen Savidge
 * @version 4/25/2020
 */
public class CircleCollider extends Collider {
    public static final String colliderType = "Circle";
    private int radius;

    public CircleCollider(GameObject object, int radius) {
        super(object);
        this.radius = radius;
        this.type = CircleCollider.colliderType;
    }

    public boolean collidesWithCircle(int x, int y, CircleCollider other) {
        // This collider's center
        int cx = x + radius;
        int cy = y + radius;

        // Other collider's center
        int o_cx = other.object.x + other.radius;
        int o_cy = other.object.y + other.radius;

        // Distance between their centers
        int xdist = cx - o_cx;
        int ydist = cy - o_cy;
        double dist = Math.sqrt(xdist*xdist + ydist*ydist);

        // Check whether the objects are intersecting
        return dist < (radius + other.radius);
    }

    @Override
    public boolean checkCollision(int x, int y, boolean absolute) {
        boolean found_collision = false;
        int try_x, try_y;

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
