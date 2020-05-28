package engine.collider;

import engine.objects.PhysicsObject;
import engine.util.Line;
import engine.util.Vector2;

/**
 * @author Galen Savidge
 * @version 5/27/2020
 */
public class Intersection {

    public Intersection(PhysicsObject collided_with, Vector2 point, Line edge, Line ray) {
        this.collided_with = collided_with;
        this.point = point.copy();
        this.edge = edge;
        this.ray = ray;
        this.distance = point.difference(ray.p1).abs();
        this.reversed = false;
    }

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

    /**
     * If true, reverses to-contact and reject vectors. Useful when collision checking against self rather than against
     * another object.
     */
    public boolean reversed;

    public Intersection copy() {
        return new Intersection(this.collided_with, this.point, this.edge, this.ray);
    }

    /**
     * @return The normal of the edge intersected with. If {@code reversed == true} the normal points inside the {@code
     * Collider}, though generally it points outside.
     */
    public Vector2 getNormal() {
        if(reversed)
            return edge.RHNormal().multiply(-1);
        else {
            return edge.RHNormal();
        }
    }

    /**
     * Returned when doing a sweep or ray-cast: the change in position to move to the point of contact with the other
     * object. Parallel to the direction of the sweep or ray-cast.
     */
    public Vector2 getToContact() {
        double to_contact_distance = point.difference(ray.p1).abs() - Collider.reject_separation;
        if(reversed) {
            to_contact_distance *= -1;
        }
        return ray.vector().normalize().multiply(to_contact_distance);
    }

    /**
     * Returned when doing a sweep or ray-cast: the change in position required to fully resolve the collision so that
     * the objects no longer collide. Normal to the first surface collided with.
     */
    public Vector2 getReject() {
        Vector2 normal = getNormal();
        double reject_distance = point.difference(ray.p2).projection(normal).abs() + Collider.reject_separation;
        if(reversed) {
            reject_distance *= -1;
        }
        return normal.multiply(reject_distance);
    }
}
