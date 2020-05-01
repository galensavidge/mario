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

    public Vector2 pixelPosition() {
        return position.round();
    }


    public ArrayList<Vector2> collideWithSolids(Vector2 delta_position) {
        ArrayList<Vector2> normals = new ArrayList<>();
        if(delta_position.equals(Vector2.zero())) {
            return normals;
        }

        Vector2 new_position = position.add(delta_position);
        Vector2 reject_vector = delta_position.normalize().multiply(-0.1);

        while(true) {
            collider.setPosition(new_position);
            Collider.Collision collision = collider.getCollisions();

            if(!collision.collision_found) break;

            ArrayList<PhysicsObject> objects = collision.collided_with;

            ArrayList<Collider> colliders = new ArrayList<>();
            for (PhysicsObject o : objects) {
                if (o.solid) {
                    colliders.add(o.collider);
                }
            }

            do {
                new_position = new_position.add(reject_vector);
                collider.setPosition(new_position);
            }
            while (collider.getCollisions(colliders).collision_found);

            Vector2 normal = collider.getNormal(delta_position, colliders);
            if (normal == null) {
                System.out.println("Null normal!");
            } else {
                normals.add(normal);
                delta_position = delta_position.subtract(delta_position.projection(normal));
                new_position = position.add(delta_position);
            }
        }

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

