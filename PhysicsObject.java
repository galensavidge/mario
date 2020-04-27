public abstract class PhysicsObject extends GameObject {
    protected Collider collider;

    // Position
    public double x;
    public double y;

    // Velocity
    public double vx;
    public double vy;

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();
        this.x += vx*t;
        this.y += vy*t;
    }

    @Override
    public abstract void draw();

    @Override
    public void delete() {
        collider.delete();
        collider = null;
        super.delete();
    }
}
