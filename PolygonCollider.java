import java.util.ArrayList;

public class PolygonCollider extends Collider {

    public static PolygonCollider newBox(PhysicsObject object, int x_offset, int y_offset, int width, int height) {
        Vector2[] corners = {new Vector2(x_offset,y_offset), new Vector2(x_offset+width-1,y_offset),
                new Vector2(x_offset+width-1,y_offset+height-1), new Vector2(x_offset,y_offset+height-1)};
        PolygonCollider collider = new PolygonCollider(object, corners);
        collider.setPosition(object.position);
        return collider;
    }

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

    public boolean collidesWithPolygon(PolygonCollider other) {
        ArrayList<Line> this_lines = this.getLines();
        ArrayList<Line> other_lines = other.getLines();
        return false;
    }

    @Override
    public void draw() {
        ArrayList<Line> lines = getLines();
        for(Line l : lines) {
            l.draw();
        }
    }

    @Override
    public boolean collidesWith(Vector2 p, Collider c) {
        return false;
    }
}
