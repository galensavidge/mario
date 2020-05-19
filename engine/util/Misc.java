package engine.util;

import java.util.ArrayList;

/**
 * Contains miscellaneous utility functions.
 *
 * @author Galen Savidge
 * @version 4/30/2020
 */
public class Misc {

    // Error allowed before calling floating point values not equal in inclusive comparisons
    public static final double delta = 1e-9;

    public static boolean largerIncl(double a, double b) {
        return a >= b - delta;
    }

    public static boolean smallerIncl(double a, double b) {
        return a <= b + delta;
    }

    /**
     * Returns true iff a is in [b, c].
     */
    public static boolean betweenIncl(double a, double b, double c) {
        return a >= (Math.min(b, c)-delta) && a <= (Math.max(b, c)+delta);
    }

    /**
     * Adds all elements of {@code array1} to {@code array2} if {@code array2} does not have an equal element as defined
     * by {@code Vector2.equals()}.
     */
    public static void addNoDuplicates(ArrayList<Vector2> array1,  ArrayList<Vector2> array2) {
        for(Vector2 o1 : array1) {
            boolean contains_element = false;
            for (Vector2 o2 : array2) {
                if (o1.equals(o2)) {
                    contains_element = true;
                    break;
                }
            }
            if (!contains_element) {
                array2.add(o1);
            }
        }
    }
}
