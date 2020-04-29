import java.awt.*;

public class Line {
    public final Vector2 p1, p2;
    public final double A, B, C;

    public Line(Vector2 p1, Vector2 p2) {
        this.p1 = p1;
        this.p2 = p2;
        A = p2.y - p1.y;
        B = p1.x - p2.x;
        C = A*p1.x + B*p1.y;
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

    public void draw() {
        GameGraphics.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y,false, Color.green);
    }
}
