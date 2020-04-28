/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 4/27/2020
 */
public abstract class PhysicsObject extends GameObject {
    protected Collider collider;
    public Vector2 position;
    public Vector2 velocity;
    public boolean solid = false;

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();
        position = position.add(velocity.multiply(t));
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

