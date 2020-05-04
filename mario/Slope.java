package mario;

import engine.World;
import engine.objects.Collider;
import engine.objects.PhysicsObject;
import engine.util.Vector2;

public class Slope extends PhysicsObject {
    public static final String type_name = "Slope";

    public Slope(double x, double y) {
        super(1, 1, x, y);
        double width = 192-Collider.edge_separation;
        double height = 193;
        Vector2[] vertices = {new Vector2(Collider.edge_separation, height),
                new Vector2(width, Collider.edge_separation),
                new Vector2(width + World.getGridScale(), Collider.edge_separation),
                new Vector2(width + World.getGridScale(), height)};
        this.collider = new Collider(this, vertices);
        this.collider.setPosition(position);
        this.collider.draw_self = true;
        this.solid = true;
        this.type = type_name;
    }

    @Override
    public void collisionEvent(PhysicsObject object) {

    }

    public void update() {
        super.update();
    }

    public void draw() {
        collider.draw();
    }
}