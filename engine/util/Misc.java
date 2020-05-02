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

    /**
     * Returns true iff a is in (b, c).
     */
    public static boolean between(double a, double b, double c) {
        return a > Math.min(b, c) && a < Math.max(b, c);
    }

    /**
     * Returns true iff a is in [b, c].
     */
    public static boolean betweenIncl(double a, double b, double c) {
        return a >= (Math.min(b, c)-delta) && a <= (Math.max(b, c)+delta);
    }

    /**
     * Adds {@code element} to {@code array} iff {@code array} does not contain an element mathematically equal to
     * {@code element}.
     */
    public static void addNoDuplicates(ArrayList<Vector2> array, Vector2 element) {
        boolean contains_element = false;
        for(Vector2 v : array) {
            if(v.equals(element)) {
                contains_element = true;
                break;
            }
        }
        if(!contains_element) {
            array.add(element);
        }
    }
}
