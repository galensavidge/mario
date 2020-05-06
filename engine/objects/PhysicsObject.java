package engine.objects;

import engine.objects.Collider.Collision;
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


    /* Accessor functions */

    /**
     * @return The PhysicsObject's {@code type}, if one was set, otherwise returns null.
     */
    public String getType() {
        return type;
    }

    /**
     * @return The position of the object rounded down to the nearest pixel.
     */
    public Vector2 pixelPosition() {
        return position.round();
    }


    /* Physics functions */

    /**
     * Override {@link #collideWith} to change which objects are passed through and which are not. Defaults to colliding
     * with only objects marked solid.
     *
     * @return True iff this object is touching an object that it collides with in the direction defined by
     * {@code direction}.
     */
    protected boolean touchingCollidable(Vector2 direction) {
        direction = direction.normalize().multiply(2*Collider.reject_separation);
        collider.setPosition(position.sum(direction));
        ArrayList<Collision> collisions = collider.getCollisions();

        for(Collision c : collisions) {
            if(c.collision_found) {
                if (collideWith(c.collided_with)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles collision with other objects. Moves the {@code PhysicsObject} as far as possible in the desired direction
     * without intersecting an object and returns the collisions that it encountered. Note: calling this function with a
     * {@code delta_position} of {@code <0, 0>} will always return an empty list. Pushing should be handled in the
     * pushing object's movement code.
     *
     * Override {@link #collideWith} to change which objects are passed through and which are not. Defaults to colliding
     * with only objects marked solid.
     *
     * @param delta_position The change in position this step.
     * @return A list of the normal vectors from the surfaces collided with.
     */
    protected ArrayList<Collision> collideWithObjects(Vector2 delta_position) {
        ArrayList<Collision> collisions = new ArrayList<>();
        if(delta_position.equals(Vector2.zero())) {
            return new ArrayList<>();
        }

        Vector2 new_position = position;
        
        // Loop until a position is found with no collisions or we hit too many iterations
        for(int i = 0;i < 100;i++) {
            // Determine the new position to check
            new_position = position.sum(delta_position);

            // Get a normal vector from the closest surface of the objects collided with
            Collision collision = sweepForCollision(delta_position);

            if(collision.collision_found) {
                // Remove the portion of the attempted motion that is parallel to the normal vector
                delta_position = delta_position.sum(collision.normal_reject);
                collisions.add(collision);
            }
        }

        // Update object position
        position = new_position;
        collider.setPosition(position);

        // Send collision events
        for(Collision c : collisions) {
            if(c.collision_found) {
                collisionEvent(c.collided_with);
                c.collided_with.collisionEvent(this);
            }
        }

        return collisions;
    }

    /**
     * Returns the {@code Collision} first encountered when moving from {@code position} to
     * {@code position + delta_position}.
     *
     * @param check_all If true, will check all colliders in the game world. If false, will check only colliders
     *                  encountered at the destination point. False also ignores collisions based on
     *                  {@code collideWith}. Defaults to false.
     * @return A {code Collision} if a collision was found, otherwise null.
     */
    protected Collision sweepForCollision(Vector2 delta_position, boolean check_all) {
        if(!check_all) {
            // Check for collisions at final position
            collider.setPosition(position.sum(delta_position));
            ArrayList<Collision> collisions_here = collider.getCollisions();
            collider.setPosition(position);

            // Make a list of the objects that should be collided with rather than passed through
            ArrayList<Collider> other_colliders = new ArrayList<>();
            for (Collision c : collisions_here) {
                if (c.collision_found) {
                    if (collideWith(c.collided_with)) {
                        other_colliders.add(c.collided_with.collider);
                    }
                }
            }

            // Break if there is no collision at this position
            if (other_colliders.size() == 0) return new Collision(this.collider);

            // Get a normal vector from the closest surface of the objects collided with
            return collider.getCollisionDetails(delta_position, other_colliders);
        }
        else {
            return collider.getCollisionDetails(delta_position);
        }
    }

    protected Collision sweepForCollision(Vector2 delta_position) {
        return sweepForCollision(delta_position, false);
    }

    /**
     * Used by {@link #collideWithObjects} to determine which objects to collide with and which to pass through.
     * @return True to collide with this object, false to pass through.
     */
    protected boolean collideWith(PhysicsObject o) {
        return o.solid;
    }

    /**
     * Override this method to respond to collisions with other objects. Events will be generated if
     * {@code collider.active_check == true}.
     */
    public void collisionEvent(PhysicsObject object) {}

    @Override
    public abstract void update();

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

