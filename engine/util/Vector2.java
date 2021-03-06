package engine.util;

/**
 * A simple class to represent a 2D vector with double precision.
 *
 * @author Galen Savidge
 * @version 5/18/2020
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
     * Copies this vector to {@code v}.
     */
    public void copyTo(Vector2 v) {
        v.x = this.x;
        v.y = this.y;
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
    public Vector2 sum(Vector2 v) {
        return new Vector2(this.x + v.x, this.y + v.y);
    }

    /**
     * @return The vector {@code <x + a, x + b>}.
     */
    public Vector2 sum(double a) {
        return new Vector2(this.x + a, this.y + a);
    }

    /**
     * @return The result of the vector subtraction {@code this - v}.
     */
    public Vector2 difference(Vector2 v) {
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

    /**
     * @return The right-hand normal of {@code this}. Note that the (0, 0) point is in the upper left-hand corner
     * meaning the handedness is inverted in practicality.
     */
    public Vector2 RHNormal() {
        return new Vector2(-y, x);
    }

    /**
     * @return The left-hand normal of {@code this}. Note that the (0, 0) point is in the upper left-hand corner meaning
     * the handedness is inverted in practicality.
     */
    public Vector2 LHNormal() {
        return new Vector2(y, -x);
    }

    /**
     * @return The component of {@code this} normal to {@code v}.
     */
    public Vector2 normalComponent(Vector2 v) {
        return this.difference(this.projection(v));
    }

    /**
     * @return The clockwise rotation, in radians, between {@code this} and the horizontal (the x+ unit vector).
     */
    public double clockwiseAngle() {
        double angle = Math.atan2(this.y, this.x);
        if(angle < 0) {
            angle += Math.PI*2;
        }
        return angle;
    }

    public String toString() {
        return "<" + this.x + "," + this.y + ">";
    }
}
