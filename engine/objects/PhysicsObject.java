package engine.objects;

import engine.Game;
import engine.util.Vector2;

import java.util.ArrayList;

/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 4/27/2020
 */
public abstract class PhysicsObject extends GameObject {
    public Collider collider;
    public Vector2 position;
    public Vector2 velocity;
    public boolean solid = false;

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
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
        Vector2 reject_vector = delta_position.normalize().multiply(-0.1);

        while(true) {
            // Determine the new position to check
            new_position = new_position.add(delta_position);
            collider.setPosition(new_position);

            // Check for collisions at this position
            Collider.Collision collision = collider.getCollisions();
            if(!collision.collision_found) break;

            // Make a list of the solid objects collided with
            ArrayList<PhysicsObject> objects = collision.collided_with;
            ArrayList<Collider> colliders = new ArrayList<>();
            for (PhysicsObject o : objects) {
                if (o.solid) {
                    colliders.add(o.collider);
                }
            }

            // Move along reject_vector until no longer colliding with the solid objects
            do {
                new_position = new_position.add(reject_vector);
                collider.setPosition(new_position);
            }
            while (collider.getCollisions(colliders).collision_found);

            // Get a normal vector from the closest surface of the objects collided with
            Vector2 normal = collider.getNormal(delta_position, colliders);
            if (normal == null) {
                System.out.println("Null normal!");
            } else {
                normals.add(normal);

                // Remove the portion of the attempted motion that is parallel to the normal vector
                delta_position = delta_position.subtract(delta_position.projection(normal));
            }
        }

        // Update object position
        position = new_position;
        collider.setPosition(position);

        return normals;
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();
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

