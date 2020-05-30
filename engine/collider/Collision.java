package engine.collider;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class holds information about collision events. It is instantiated by certain {@code Collider} methods.
 *
 * @author Galen Savidge
 * @version 5/29/2020
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

    public void addIntersection(Intersection new_intersection) {
        int i = 0;
        for(;i < intersections.size();i++) {
            if(new_intersection.distance < intersections.get(i).distance) {
                break;
            }
        }
        intersections.add(i, new_intersection);
    }

    public int numIntersections() {
        return intersections.size();
    }

    public Iterator<Intersection> getIterator() {
        return intersections.iterator();
    }

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
