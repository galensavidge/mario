public class Slope extends PhysicsObject {

    public Slope(double x, double y) {
        super(1, 1, x, y);
        Vector2[] vertices = {new Vector2(0,47), new Vector2(47,0), new Vector2(47,47)};
        this.collider = new PolygonCollider(this, vertices);
        this.collider.setPosition(position);
        this.solid = true;
    }

    public void update() {
        super.update();
    }

    public void draw() {
        collider.draw();
    }
}