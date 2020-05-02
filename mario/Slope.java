package mario;

import engine.objects.Collider;
import engine.objects.PhysicsObject;
import engine.util.Vector2;

public class Slope extends PhysicsObject {
    public static final String type_name = "Slope";

    public Slope(double x, double y) {
        super(1, 1, x, y);
        Vector2[] vertices = {new Vector2(0,47), new Vector2(47,0), new Vector2(47,47)};
        this.collider = new Collider(this, vertices);
        this.collider.setPosition(position);
        this.collider.draw_self = true;
        this.solid = true;
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