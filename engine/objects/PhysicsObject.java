package engine.objects;

import engine.objects.Collider.Collision;
import engine.util.Vector2;

import java.util.ArrayList;

/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 5/6/2020
 */
public abstract class PhysicsObject extends GameObject {

    // Used to identify objects
    protected String type;
    protected String type_group;
    public boolean solid = false;
    public Collider collider;
    public Vector2 position;
    public Vector2 velocity;

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
     * @return The PhysicsObject's {@code type_group}, if one was set, otherwise returns null.
     */
    public String getTypeGroup() {
        return type_group;
    }

    /**
     * @return The position of the object rounded down to the nearest pixel.
     */
    public Vector2 pixelPosition() {
        return position.round();
    }


    /* Physics functions */

    /**
     * Handles collision with other objects. Moves the {@code PhysicsObject} as far as possible in the desired direction
     * without intersecting an object and returns the collisions that it encountered. Note: calling this function with a
     * {@code delta_position} of {@code <0, 0>} will always return an empty list. Pushing should be handled in the
     * pushing object's movement code.
     *
     * Override {@link #collidesWith} to change which objects are passed through and which are not. Defaults to colliding
     * with only objects marked solid.
     *
     * @param delta_position The change in position this step.
     * @return A list of {@code Collision} objects corresponding to the surfaces collided with.
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
            if(collidesWith(c)) {
                collisionEvent(c);
                Collision other_c = c.copy();
                c.collided_with = this;
                c.collided_with.collisionEvent(other_c);
            }
        }

        return collisions;
    }

    /**
     * Returns the {@code Collision} first encountered when moving from {@code position} to
     * {@code position + delta_position}.
     *
     * @param check_all If true, will check all colliders in the game world. If false, ignores collisions based on
     *                  {@code collideWith}. Defaults to false.
     * @return A {code Collision} if a collision was found, otherwise null.
     */
    protected Collision sweepForCollision(Vector2 delta_position, boolean check_all) {
        // Do a fast collision check at the final position to narrow down the list of collider to check
        collider.setPosition(position.sum(delta_position));
        ArrayList<Collision> collisions_here = collider.getCollisions();
        collider.setPosition(position);

        // Make a list of the objects that should be collided with rather than passed through
        ArrayList<Collider> other_colliders = new ArrayList<>();
        for (Collision c : collisions_here) {
            if (c.collision_found) {
                other_colliders.add(c.collided_with.collider);
            }
        }

        while(other_colliders.size() > 0) {
            // Find details for the closest collision encountered when travelling along delta_position
            Collision c = collider.getCollisionDetails(delta_position, other_colliders);

            // Done if: no collision was found, check all is true, or this object collides with the object at c
            if(!c.collision_found || check_all || collidesWith(c)) {
                return c;
            }

            // If not done this object does not collide with the object at c; remove it and try again
            other_colliders.remove(c.collided_with.collider);
        }

        return new Collision(this.collider);
    }

    /**
     * Returns the {@code Collision} first encountered when moving from {@code position} to
     * {@code position + delta_position}.
     *
     * @return A {code Collision} if a collision was found, otherwise null.
     */
    protected Collision sweepForCollision(Vector2 delta_position) {
        return sweepForCollision(delta_position, false);
    }

    /**
     * Used by {@link #collideWithObjects} to determine which objects to collide with and which to pass through.
     * @return True to collide with this object, false to pass through.
     */
    protected boolean collidesWith(Collision c) {
        return c.collided_with.solid;
    }

    /**
     * Override this method to respond to collisions with other objects. Events will be generated if
     * {@code collider.active_check == true}.
     */
    public void collisionEvent(Collision c) {}

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

