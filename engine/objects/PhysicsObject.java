package engine.objects;

import engine.Game;
import engine.util.Line;
import engine.util.Vector2;

import java.util.ArrayList;

/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 4/27/2020
 */
public abstract class PhysicsObject extends GameObject {
    protected String type;
    public Collider collider;
    public Vector2 position;
    public Vector2 velocity;
    public boolean solid = false;

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
    }

    public String getType() {
        return type;
    }

    /**
     * @return The position of the object rounded down to the nearest pixel.
     */
    public Vector2 pixelPosition() {
        return position.round();
    }

    /**
     * Handles collision with solid objects. Moves the {@code PhysicsObject} as far as possible in the desired direction
     * without intersecting a solid object and returns the normal vector(s) that applied "force" to it during the
     * collision.
     *
     * @param delta_position The change in position this step.
     * @return A list of the normal vectors from the surfaces collided with.
     */
    public ArrayList<Vector2> collideWithSolids(Vector2 delta_position) {
        ArrayList<Vector2> normals = new ArrayList<>();
        if(delta_position.equals(Vector2.zero())) {
            return normals;
        }

        Vector2 new_position = position;
        
        // Loop until a position is found with no collisions or we hit too many iterations
        for(int i = 0;i < 100;i++) {
            // Determine the new position to check
            new_position = position.add(delta_position);

            // Check for collisions at this position
            collider.setPosition(new_position);
            Collider.Collision collision = collider.getCollisions();
            collider.setPosition(position);

            // Make a list of the solid objects collided with
            ArrayList<PhysicsObject> objects = collision.collided_with;
            ArrayList<Collider> other_colliders = new ArrayList<>();
            for (PhysicsObject o : objects) {
                if (o.solid) {
                    other_colliders.add(o.collider);
                }
            }

            // Break if there is no collision at this position
            if(other_colliders.size() == 0) break;

            // Get a normal vector from the closest surface of the objects collided with
            Vector2 normal = collider.getNormal(delta_position, other_colliders);
            if(normal != null) {
                normals.add(normal);
            }
            else {
                System.out.println("Null normal!");
            }

            if (normal == null) {
                System.out.println("Null normal!");
            } else {
                // Remove the portion of the attempted motion that is parallel to the normal vector
                delta_position = delta_position.add(normal);
            }
        }

        // Update object position
        position = new_position;
        collider.setPosition(position);

        return normals;
    }

    public abstract void collisionEvent(PhysicsObject object);

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();
    }

    @Override
    public abstract void draw();

    @Override
    public void delete() {
        if(!this.isDeleted()) {
            super.delete();
            collider.delete();
            collider = null;
        }
    }
}

