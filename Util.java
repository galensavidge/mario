public class Util {

    /**
     * Returns true iff a is in (b, c).
     */
    protected static boolean between(double a, double b, double c) {
        return a > Math.min(b, c) && a < Math.max(b, c);
    }

    /**
     * Returns true iff a is in [b, c].
     */
    protected static boolean betweenIncl(double a, double b, double c) {
        return a >= Math.min(b, c) && a <= Math.max(b, c);
    }
}
