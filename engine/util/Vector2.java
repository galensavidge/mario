package engine.util;

/**
 * A simple class to hold a 2-vector with double precision.
 *
 * @author Galen Savidge
 * @version 4/30/2020
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
     * @return This vector, rounded down to pixel precision.
     */
    public Vector2 round() {
        return new Vector2((int)this.x, (int)this.y);
    }

    /**
     * @return The vector sum of {@code this} and {@code v}.
     */
    public Vector2 add(Vector2 v) {
        return new Vector2(this.x + v.x, this.y + v.y);
    }

    /**
     * @return The vector {@code <x + a, x + b>}.
     */
    public Vector2 add(double a) {
        return new Vector2(this.x + a, this.y + a);
    }

    /**
     * @return The result of the vector subtraction {@code this - v}.
     */
    public Vector2 subtract(Vector2 v) {
        return new Vector2(this.x - v.x, this.y - v.y);
    }

    /**
     * @return The result of the multiplication {@code this*a}.
     */
    public Vector2 multiply(double a) {
        return new Vector2(this.x*a, this.y*a);
    }

    /**
     * @return The scalar absolute value {@code ||this||}.
     */
    public double abs() {
        return Math.sqrt(this.x*this.x + this.y*this.y);
    }

    /**
     * @return {@code this} normalized. Returns the zero vector if {@code this} equals the zero vector.
     */
    public Vector2 normalize() {
        if(this.equals(z)) {
            return this.copy();
        }
        return this.multiply(1.0/this.abs());
    }

    /**
     * @return The vector dot product of {@code this} and {@code v}.
     */
    public double dot(Vector2 v) {
        return this.x*v.x + this.y*v.y;
    }

    /**
     * @return The projection of {@code this} onto {@code v}.
     */
    public Vector2 projection(Vector2 v) {
        double mag_v = v.abs();
        double mag_projection = this.dot(v)/(mag_v*mag_v);
        return v.multiply(mag_projection);
    }
}
