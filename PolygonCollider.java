import java.awt.*;
import java.util.ArrayList;

public class PolygonCollider extends Collider {

    public static PolygonCollider newBox(PhysicsObject object, int x_offset, int y_offset, int width, int height) {
        Vector2[] corners = {new Vector2(x_offset,y_offset), new Vector2(x_offset+width-1,y_offset),
                new Vector2(x_offset+width-1,y_offset+height-1), new Vector2(x_offset,y_offset+height-1)};
        PolygonCollider collider = new PolygonCollider(object, corners);
        collider.setPosition(object.position);
        return collider;
    }

    private Collision last_collision;
    private final ArrayList<Vector2> local_vertices = new ArrayList<>();

    public PolygonCollider(PhysicsObject object, Vector2[] local_vertices) {
        super(object);
        for (Vector2 v : local_vertices) {
            this.local_vertices.add(v.round());
        }
    }

    public ArrayList<Vector2> getVertices() {
        ArrayList<Vector2> vertices = new ArrayList<>();
        for(Vector2 v : local_vertices) {
           vertices.add(v.add(position));
        }
        return vertices;
    }

    public ArrayList<Line> getLines() {
        ArrayList<Vector2> vertices = this.getVertices();
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0;i < vertices.size();i++) {
            int j = i-1;
            if(j < 0) j += vertices.size();
            lines.add(new Line(vertices.get(j), vertices.get(i)));
        }
        return lines;
    }

    public void collisionWithPolygon(Collision collision, PolygonCollider other) {
        ArrayList<Line> this_lines = this.getLines();
        ArrayList<Line> other_lines = other.getLines();
        boolean collided_with_other = false;

        for(Line this_l : this_lines) {
            for(Line other_l : other_lines) {
                Vector2 p = this_l.intersection(other_l);
                if(p != null) {
                    collided_with_other = true;

                    boolean contains_p = false;
                    for(Vector2 i : collision.intersections) {
                        if(i.equals(p)) {
                            contains_p = true;
                        }
                    }

                    if(!contains_p) {
                        collision.intersections.add(p);
                    }

                    collision.lines.add(other_l);
                }
            }
        }

        if(collided_with_other) {
            collision.collision_found = true;
            collision.collided_with.add(other);
        }
    }

    @Override
    public void draw() {
        ArrayList<Line> lines = getLines();
        for(Line l : lines) {
            l.draw();
        }

        if(!(object instanceof Player)) {
            return;
        }

        if(last_collision != null) {
            for (Vector2 i : last_collision.intersections) {
                GameGraphics.drawPoint((int) i.x, (int) i.y, false, Color.RED);
            }

            if(last_collision.least_squares != null) {
                last_collision.least_squares.draw();
            }

            if(last_collision.normal != null && last_collision.mean != null) {
                GameGraphics.drawLine((int)last_collision.mean.x, (int)last_collision.mean.y,
                        (int)(last_collision.mean.x + last_collision.normal.x),
                        (int)(last_collision.mean.y + last_collision.normal.y), false, Color.CYAN);
            }

            last_collision = null;
        }
    }

    @Override
    public void updateCollision(Collision collision, Collider collider) {
        if(collider instanceof PolygonCollider) {
            collisionWithPolygon(collision, (PolygonCollider)collider);
        }

        last_collision = collision;
    }
}
