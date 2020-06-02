package engine.collider;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class holds information about collision events. It is instantiated by certain {@code Collider} methods.
 *
 * @author Galen Savidge
 * @version 6/1/2020
 */
public class Collision {
    /**
     * True if at least one intersection was found.
     */
    public boolean collision_found;

    /**
     * The list of intersection points founds at this collision. Sorted from closest to farthest.
     */
    private final ArrayList<Intersection> intersections;


    /**
     * Creates an empty {@link Collision} instance where {@code collision_found = false} and {@code intersections} is an
     * empty list.
     */
    public Collision() {
        this.collision_found = false;
        this.intersections = new ArrayList<>();
    }

    /**
     * Adds an {@link Intersection} to the internal intersections list.
     */
    public void addIntersection(Intersection new_intersection) {
        if(new_intersection != null) {
            collision_found = true;
            int i = 0;
            for(;i < intersections.size();i++) {
                if(new_intersection.distance < intersections.get(i).distance) {
                    break;
                }
            }
            intersections.add(i, new_intersection);
        }
    }

    /**
     * @return The length of the intersections list.
     */
    public int numIntersections() {
        return intersections.size();
    }

    /**
     * @return An iterator for the internal intersections list.
     */
    public Iterator<Intersection> getIterator() {
        return intersections.iterator();
    }

    /**
     * Returns the closest {@link Intersection} in the internal intersections list as defined by the object's
     * {@code distance}. Removes the object from the list.
     *
     * @return The closest {@link Intersection}, or {@code null} if {@link #numIntersections} {@code == 0}.
     */
    public Intersection popClosestIntersection() {
        if(intersections.size() > 0) {
            Intersection i = intersections.get(0);
            intersections.remove(0);
            return i;
        }
        else {
            return null;
        }
    }

    /**
     * Returns the farthest {@link Intersection} in the internal intersections list as defined by the object's
     * {@code distance}. Removes the object from the list.
     *
     * @return The farthest {@link Intersection}, or {@code null} if {@link #numIntersections} {@code == 0}.
     */
    public Intersection popFarthestIntersection() {
        if(intersections.size() > 0) {
            Intersection i = intersections.get(intersections.size() - 1);
            intersections.remove(intersections.size() - 1);
            return i;
        }
        else {
            return null;
        }
    }
}
