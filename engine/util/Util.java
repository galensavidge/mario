package engine.util;

public class Util {

    private static final double delta = 1e-6;

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
}
