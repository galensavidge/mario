package engine.util;

import engine.GameGraphics;

import java.awt.*;

/**
 * A class to represent line segments. The line segment is defined as pointing from {@code p1} to {@code p2}.
 *
 * @author Galen Savidge
 * @version 4/30/2020
 */
public class Line {
    public final Vector2 p1, p2;
    public final double A, B, C;

    /**
     * Creates a {@code Line} between points {@code p1} and {@code p2}.
     * @param p1 The beginning of the line segment.
     * @param p2 The end of the line segment.
     */
    public Line(Vector2 p1, Vector2 p2) {
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        A = p2.y - p1.y;
        B = p1.x - p2.x;
        C = A*p1.x + B*p1.y;
    }

    /**
     * @return The vector pointing from the beginning of the line segment to the end, i.e. from {@code p1} to
     * {@code p2}.
     */
    public Vector2 vector() {
        return new Vector2(p2.x - p1.x, p2.y - p1.y);
    }

    /**
     * @return A line segment equal to {@code this} but pointing in the opposite direction.
     */
    public Line reverse() {
        return new Line(this.p2, this.p1);
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
     * Finds the point of intersection between this {@code Line} and {@code l). Returns {@code null} if no intersection
     * exists -- this could be either because the lines are parallel or because the intersection point is not within
     * both line segments.
     */
    public Vector2 intersection(Line l) {
        double det = 1.0/(this.A*l.B - l.A*this.B);
        if(det != 0) {
            // Point of intersection
            Vector2 p = new Vector2((l.B*this.C - this.B*l.C)*det,(this.A*l.C - l.A*this.C)*det);

            if(Misc.betweenIncl(p.x, p1.x, p2.x) && Misc.betweenIncl(p.y, p1.y, p2.y)
                    && Misc.betweenIncl(p.x, l.p1.x, l.p2.x) && Misc.betweenIncl(p.y, l.p1.y, l.p2.y)) {
                return p;
            }
        }
        return null;
    }

    public void draw() {
        GameGraphics.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y,false, Color.green);
    }
}
