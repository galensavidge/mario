package engine.util;

/**
 * A simple class to hold a 2-vector with double precision.
 *
 * @author Galen Savidge
 * @version 4/27/2020
 */
public class Vector2 {
    /**
     * Returns the zero vector.
     */
    public static Vector2 zero() {
        return z.copy();
    }
    private static final Vector2 z = new Vector2(0, 0);

    public double x;
    public double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return A copy of this vector.
     */
    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    /**
     * @return True iff the vectors are mathematically equal.
     */
    public boolean equals(Vector2 v) {
        return this.x == v.x && this.y == v.y;
    }

    /**
     * @return This vector, rounded to pixel precision.
     */
    public Vector2 round() {
        return new Vector2((int)this.x, (int)this.y);
    }

    /**
     * @param v Vector to add to this.
     * @return The sum of the two vectors.
     */
    public Vector2 add(Vector2 v) {
        return new Vector2(this.x + v.x, this.y + v.y);
    }

    /**
     * @param a A scalar to add to both components.
     * @return The new vector (x + a, x + b).
     */
    public Vector2 add(double a) {
        return new Vector2(this.x + a, this.y + a);
    }

    /**
     * @param v Vector to subtract from this.
     * @return The difference of the two vectors.
     */
    public Vector2 subtract(Vector2 v) {
        return new Vector2(this.x - v.x, this.y - v.y);
    }

    /**
     * @param a A scalar.
     * @return This vector multiplied by a.
     */
    public Vector2 multiply(double a) {
        return new Vector2(this.x*a, this.y*a);
    }

    /**
     * @return The scalar absolute value of the vector.
     */
    public double abs() {
        return Math.sqrt(this.x*this.x + this.y*this.y);
    }

    /**
     * @return The dot product of this and v.
     */
    public double dot(Vector2 v) {
        return this.x*v.x + this.y*v.y;
    }
}
