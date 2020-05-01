package engine.util;

/**
 * Contains miscellaneous utility functions.
 *
 * @author Galen Savidge
 * @version 4/30/2020
 */
public class Util {

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

    // Error allowed before calling floating point values not equal in inclusive comparisons
    public static final double delta = 1e-9;
}
