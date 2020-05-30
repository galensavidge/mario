package engine.collider;

import engine.objects.PhysicsObject;
import engine.util.Line;
import engine.util.Vector2;

/**
 * @author Galen Savidge
 * @version 5/27/2020
 */
public class Intersection {

    /**
     * The object to which the other {@link Collider} belongs.
     */
    public final PhysicsObject collided_with;

    /**
     * The point of intersection.
     */
    public final Vector2 point;

    /**
     * The edge intersected with.
     */
    public final Line edge;

    /**
     * The ray-cast line which this intersection is on.
     */
    public final Line ray;

    /**
     * The distance from the point of origin of the ray-cast to the intersection.
     */
    public final double distance;

    private Vector2 normal = null;
    private Vector2 reject = null;
    private Vector2 to_contact = null;

    public final boolean reversed;

    public Intersection(PhysicsObject collided_with, Vector2 point, Line edge, Line ray, boolean reverse) {
        this.collided_with = collided_with;
        this.point = point.copy();
        if(reverse) {
            this.edge = edge.reverse();
        }
        else {
            this.edge = edge;
        }
        this.ray = ray;
        this.distance = point.difference(ray.p1).abs();

        this.reversed = reverse;
    }

    public Intersection copy() {
        return new Intersection(collided_with, point, edge, ray, false);
    }

    public Intersection reverse() {
        return new Intersection(collided_with, point, edge, ray, true);
    }

    /**
     * @return The normal of the edge intersected with. If {@code reversed == true} the normal points inside the {@code
     * Collider}, though generally it points outside.
     */
    public Vector2 getNormal() {
        if(normal == null) {
            normal = edge.RHNormal();
        }
        return normal;
    }

    /**
     * Returned when doing a sweep or ray-cast: the change in position to move to the point of contact with the other
     * object. Parallel to the direction of the sweep or ray-cast.
     */
    public Vector2 getToContact() {
        if(to_contact == null) {
            double to_contact_distance = distance - Collider.reject_separation;
            if(reversed) {
                to_contact_distance *= -1;
            }
            to_contact = ray.vector().normalize().multiply(to_contact_distance);
        }
        return to_contact;
    }

    /**
     * Returned when doing a sweep or ray-cast: the change in position required to fully resolve the collision so that
     * the objects no longer collide. Normal to the first surface collided with.
     */
    public Vector2 getReject() {
        if(reject == null) {
            double reject_distance = point.difference(ray.p2).projection(getNormal()).abs() + Collider.reject_separation;
            reject = getNormal().multiply(reject_distance);
        }
        return reject;
    }
}
