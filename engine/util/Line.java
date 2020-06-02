package engine.util;

import engine.graphics.GameGraphics;

import java.awt.*;

/**
 * A class to represent line segments. The line segment is defined as pointing from {@code p1} to {@code p2}.
 *
 * @author Galen Savidge
 * @version 6/1/2020
 */
public class Line {
    public final Vector2 p1, p2;
    private double A, B, C; // Defines the line in the form Ax + By = C
    public final boolean p1_endpoint;
    public final boolean p2_endpoint;

    /**
     * Creates a {@code Line} between points {@code p1} and {@code p2}. This line is a line segment.
     *
     * @param p1 The beginning of the line segment.
     * @param p2 The end of the line segment.
     */
    public Line(Vector2 p1, Vector2 p2) {
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        this.p1_endpoint = true;
        this.p2_endpoint = true;
        setConstants();
    }

    /**
     * Creates a {@code Line} between points {@code p1} and {@code p2}. This line can be a line segment, ray, or line
     * depending on the values of {@code p1_endpoint} and {@code p2_endpoint}.
     *
     * @param p1_endpoint If true, {@code p1} is an endpoint. If false, the line extends infinitely past {@code p1}.
     * @param p2_endpoint If true, {@code p2} is an endpoint. If false, the line extends infinitely past {@code p2}.
     */
    public Line(Vector2 p1, Vector2 p2, boolean p1_endpoint, boolean p2_endpoint) {
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        this.p1_endpoint = p1_endpoint;
        this.p2_endpoint = p2_endpoint;
        setConstants();
    }

    /**
     * Helper function to calculate the formula defining the line.
     */
    private void setConstants() {
        A = p2.y - p1.y;
        B = p1.x - p2.x;
        C = A*p1.x + B*p1.y;
    }

    /**
     * @return The vector pointing from the beginning of the line segment to the end, i.e. from {@code p1} to {@code
     * p2}.
     */
    public Vector2 vector() {
        return new Vector2(p2.x - p1.x, p2.y - p1.y);
    }

    /**
     * @return A line segment equal to {@code this} but pointing in the opposite direction.
     */
    public Line reverse() {
        return new Line(this.p2, this.p1, this.p2_endpoint, this.p1_endpoint);
    }

    public double length() {
        return this.vector().abs();
    }

    /**
     * @return The unit-magnitude right-hand normal vector of the {@code Line}.
     */
    public Vector2 RHNormal() {
        Vector2 v = this.vector();
        Vector2 normal = v.LHNormal();
        return normal.normalize();
    }

    /**
     * Finds the point of intersection between this {@code Line} and {@code l}. Returns {@code null} if no intersection
     * exists -- this could be either because the lines are parallel or because the intersection point is not on both
     * line segments.
     */
    public Vector2 intersection(Line l) {
        Vector2 p = poi(l);
        if(p == null) {
            return null;
        }

        // Check that the intersection lies on both lines
        if(betweenBounds(p1.x, p2.x, p.x, p1_endpoint, p2_endpoint)
                && betweenBounds(p1.y, p2.y, p.y, p1_endpoint, p2_endpoint)
                && betweenBounds(l.p1.x, l.p2.x, p.x, l.p1_endpoint, l.p2_endpoint)
                && betweenBounds(l.p1.y, l.p2.y, p.y, l.p1_endpoint, l.p2_endpoint)) {
            return p;
        }
        return null;
    }

    /**
     * Finds the shortest vector from the axis defined by this {@link Line} to {@code point}. The returned {@link
     * Vector2} will always be normal to {@code this}.
     */
    public Vector2 dropNormal(Vector2 point) {
        Line normal_through_point = new Line(point, point.sum(this.vector().RHNormal()), false, false);
        Vector2 i = poi(normal_through_point);
        if(i == null) {
            return null; // I'm not sure if this can happen
        }
        else {
            return i.difference(point);
        }
    }


    /* Helper functions */

    /**
     * Fins the point of intersection of two lines. Returns {@code null} iff the lines are parallel.
     */
    private Vector2 poi(Line l) {
        double det = (this.A*l.B - l.A*this.B);
        if(det == 0) {
            return null;
        }
        return new Vector2((l.B*this.C - this.B*l.C)/det, (this.A*l.C - l.A*this.C)/det);
    }

    /**
     * Returns whether c lies in [a, b]; however, if {@code a_is_bound} or {@code b_is_bound} is {@code false}, that
     * bound is ignored. For example, assuming a < b and {@code a_is_bound = false}, the function will return {@code
     * true} when c <= b. If both bounds are ignored the function will always return {@code true}.
     */
    private boolean betweenBounds(double a, double b, double c, boolean a_is_bound, boolean b_is_bound) {
        boolean a_check, b_check;

        // a >= b, with some tolerance
        if(Misc.largerIncl(a, b)) {
            a_check = Misc.smallerIncl(c, a) || !a_is_bound;
            b_check = Misc.largerIncl(c, b) || !b_is_bound;
        }

        // a < b
        else {
            a_check = Misc.largerIncl(c, a) || !a_is_bound;
            b_check = Misc.smallerIncl(c, b) || !b_is_bound;
        }

        return a_check && b_check;
    }


    /* Misc */

    public void draw() {
        GameGraphics.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y, false, Color.green);
    }

    public String toString() {
        return p1.toString() + " " + p2.toString();
    }
}
