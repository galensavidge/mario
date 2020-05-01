package engine.util;

import engine.GameGraphics;
import engine.World;

import java.awt.*;
import java.util.ArrayList;

public class Line {
    public final Vector2 p1, p2;
    public final double A, B, C;

    public Line(Vector2 p1, Vector2 p2) {
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        A = p2.y - p1.y;
        B = p1.x - p2.x;
        C = A*p1.x + B*p1.y;
    }

    public Vector2 vector() {
        return new Vector2(p2.x - p1.x, p2.y - p1.y);
    }

    public Vector2 RHNormal() {
        Vector2 v = this.vector();
        Vector2 normal = new Vector2(v.y, -v.x);
        return normal.normalize();
    }

    public Vector2 intersection(Line l) {
        double det = this.A*l.B - l.A*this.B;
        if(det != 0) {
            // Point of intersection
            Vector2 p = new Vector2((l.B*this.C - this.B*l.C)/det,(this.A*l.C - l.A*this.C)/det);

            if(Util.betweenIncl(p.x, p1.x, p2.x) && Util.betweenIncl(p.y, p1.y, p2.y)
                    && Util.betweenIncl(p.x, l.p1.x, l.p2.x) && Util.betweenIncl(p.y, l.p1.y, l.p2.y)) {
                return p;
            }
        }
        return null;
    }

    public static Line leastSquares(ArrayList<Vector2> points) {
        double n = points.size();
        if(n < 2) {
            return null;
        }

        double sum_x = 0, sum_y = 0, sum_xy = 0, sum_x2 = 0;
        for(Vector2 p : points) {
            sum_x += p.x;
            sum_y += p.y;
            sum_xy += p.x*p.y;
            sum_x2 += p.x*p.x;
        }

        // Ax + By = C
        double A = sum_x*sum_y - n*sum_xy;
        double B = n*sum_x2 - sum_x*sum_x;
        double C = sum_y*sum_x2 - sum_x*sum_xy;

        // y = B0 + B1*x
        double B0 = (sum_y*sum_x2 - sum_x*sum_xy)/(n*sum_x2 - sum_x*sum_x);
        double B1 = (n*sum_xy - sum_x*sum_y)/(n*sum_x2 - sum_x*sum_x);

        Vector2 p1, p2;

        //p1 = new engine.util.Vector2(0, B0);
        //p2 = new engine.util.Vector2(engine.World.getWidth(), engine.World.getWidth()*B1 + B0);

        if(A != 0) {
            p1 = new Vector2(C / A, 0);
            p2 = new Vector2(C / A - B * World.getHeight() / A, World.getHeight());
        }
        else if(B != 0) {
            p1 = new Vector2(0, C / B);
            p2 = new Vector2(World.getWidth(), C / B - A * World.getWidth() / B);
        }
        else {
            return null;
        }
        return new Line(p1, p2);
    }

    public void draw() {
        GameGraphics.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y,false, Color.green);
    }
}
