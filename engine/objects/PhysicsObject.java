package engine.objects;

import engine.graphics.GameGraphics;
import engine.objects.Collider.Collision;
import engine.util.Line;
import engine.util.Vector2;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 5/19/2020
 */
public abstract class PhysicsObject extends GameObject {

    // Used to identify instances of child classes
    protected String type;
    protected String type_group;

    protected ArrayList<String> tags = new ArrayList<>();

    // Variables used for physics calculations
    public boolean solid = false;
    public Collider collider;
    public Vector2 position;
    public Vector2 velocity;


    /* Constructors */

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        position = new Vector2(x, y);
        velocity = Vector2.zero();
    }

    public PhysicsObject(int priority, int layer, HashMap<String, Object> args) {
        super(priority, layer);
        position = Vector2.zero();
        velocity = Vector2.zero();
        parseArgs(args);
    }

    /**
     * Parses properties from a list of JSON style name/value pairs.
     */
    protected void parseArgs(HashMap<String, Object> args) {
        try {
            Object suspend_tier = args.get("suspend tier");
            if(suspend_tier != null) this.suspend_tier = (int)(long)suspend_tier;
            Object persistent = args.get("persistent");
            if(persistent != null) this.persistent = (boolean)persistent;
            Object visible = args.get("visible");
            if(visible != null) this.visible = (boolean)visible;
            Object position = args.get("position");
            if(position != null) this.position = ((Vector2)position).copy();
            Object solid = args.get("solid");
            if(solid != null) this.solid = (boolean)solid;
        }
        catch(ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete() {
        if(!this.isDeleted()) {
            super.delete();
            if(collider != null) {
                collider.delete();
            }
        }
    }

    /* Accessor functions */

    /**
     * @return The {@link PhysicsObject}'s {@code type}, if one was set, otherwise returns null.
     */
    public String getType() {
        return type;
    }

    /**
     * @return The {@link PhysicsObject}'s {@code type_group}, if one was set, otherwise returns null.
     */
    public String getTypeGroup() {
        return type_group;
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    /**
     * @return The position of the object rounded down to the nearest pixel.
     */
    public Vector2 pixelPosition() {
        return position.round();
    }

    /**
     * @param width  This object's width.
     * @param height This object's height.
     * @param margin Extra margin around the screen to include, in pixels.
     * @return True if the object is completely off the screen.
     */
    public boolean isOnScreen(double width, double height, double margin) {
        return position.x <= GameGraphics.camera_x + GameGraphics.getWindowWidth() + margin
                && position.y <= GameGraphics.camera_y + GameGraphics.getWindowHeight() + margin
                && position.x >= GameGraphics.camera_x - width - margin
                && position.y >= GameGraphics.camera_y - height - margin;
    }


    /* Physics functions */

    /**
     * Used to determine which objects to collide with and which to pass through.
     *
     * @return True to collide with this object, false to pass through.
     */
    protected boolean collidesWith(Collision c) {
        return c.collided_with.solid;
    }

    /**
     * Handles collision with other objects. Moves the {@link PhysicsObject} as far as possible in the desired direction
     * without intersecting an object and returns the collisions that it encountered. Note: calling this function with a
     * {@code delta_position} of {@code <0, 0>} will always return an empty list. Pushing should be handled in the
     * pushing object's movement code.
     * <p>
     * Override {@link #collidesWith} to change which objects are passed through and which are not. Defaults to
     * colliding with only objects marked solid.
     *
     * @param delta_position The change in position this step.
     * @return A list of {@link Collision} objects corresponding to the surfaces collided with.
     */
    protected ArrayList<Collision> moveAndCollide(Vector2 delta_position) {
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
            else {
                break;
            }
        }

        // Update object position
        position = new_position;
        collider.setPosition(position);

        // Send collision events
        for(Collision c : collisions) {
            physicsCollisionEvent(c);
            collisionEvent(c);
            Collision other_c = c.copy();
            other_c.collided_with = this;
            c.collided_with.physicsCollisionEvent(other_c);
            c.collided_with.collisionEvent(other_c);
        }

        return collisions;
    }

    /**
     * Returns the {@link Collision} first encountered when moving from {@code position} to {@code position +
     * delta_position}.
     *
     * @param check_all If true, will check all colliders in the game world. If false, ignores collisions based on
     *                  {@link #collidesWith}. Defaults to false.
     * @return A {@link Collision} if a collision was found, otherwise null.
     */
    protected Collision sweepForCollision(Vector2 delta_position, boolean check_all) {
        // Do a fast collision check at the final position to narrow down the list of collider to check
        collider.setPosition(position.sum(delta_position));
        ArrayList<Collision> collisions_here = collider.getCollisions();
        collider.setPosition(position);

        // Make a list of the objects that should be collided with rather than passed through
        ArrayList<Collider> other_colliders = new ArrayList<>();
        for(Collision c : collisions_here) {
            if(c.collision_found) {
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

        return new Collision();
    }

    /**
     * Returns the {@link Collision} first encountered when moving from {@code position} to {@code position +
     * delta_position}.
     *
     * @return A {@link Collision} if a collision was found, otherwise null.
     */
    protected Collision sweepForCollision(Vector2 delta_position) {
        return sweepForCollision(delta_position, false);
    }

    /**
     * Returns the closest object in a given direction.
     *
     * @param direction A vector representing the direction to check.
     * @return The closest object touching or intersecting with this object in the given direction.
     */
    protected PhysicsObject getObjectInDirection(Vector2 direction) {
        ArrayList<Collider> colliders = collider.getCollidersInNeighboringZones();

        // Check what is collided with when moving in the given direction
        collider.setPosition(position.sum(direction));
        ArrayList<Collision> collisions = collider.getCollisions(colliders);
        collider.setPosition(position);

        if(collisions.size() == 0) {
            return null;
        }
        else if(collisions.size() == 1) {
            return collisions.get(0).collided_with; // If only one object is collided with, return that
        }
        else {
            // Raycast from the center of the collider
            Line ray = new Line(collider.getCenter(), collider.getCenter().sum(direction), true, false);
            try {
                // Position at the edge of collider on the raycast line
                ArrayList<Vector2> start_positions = Collider.lineIntersectsCollider(ray, collider);

                // Find the intersection farthest from the center
                double longest_dist_from_center = 0;
                Vector2 farthest_position = null;
                for(Vector2 start_position : start_positions) {
                    double dist_from_center = start_position.difference(collider.getCenter()).abs();
                    if(dist_from_center > longest_dist_from_center) {
                        longest_dist_from_center = dist_from_center;
                        farthest_position = start_position;
                    }
                }

                if(farthest_position != null) {
                    return Collider.rayCast(farthest_position, direction, colliders).collided_with;
                }
                return null;
            }
            catch(ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }


    /* Events */

    /**
     * Override this method to respond to collisions with other objects. Events are generated when collision rejection
     * occurs when using {@link #moveAndCollide}.
     *
     * @param c A collision event with details populated (e.g. {@code c.isDetailed()} returns {@code true}).
     */
    public void physicsCollisionEvent(Collision c) {}

    /**
     * Override this method to respond to collider intersections with other objects. Events are generated if {@code
     * collider.active_check} is {@code true} and this object intersects other objects, or when another object with
     * active checking enabled intersects this object, and after {@link #physicsCollisionEvent} is called.
     *
     * @param c A collision event that may or may not be detailed (e.g. {@code c.isDetailed()} may return {@code
     * false}).
     */
    public void collisionEvent(Collision c) {}

    /**
     * Called after the world is finished loading and all object have been instantiated.
     */
    public void worldLoadedEvent() {}

    @Override
    public void deleteEvent() {
        collider = null;
    }


    /* Misc */

    @Override
    public String toString() {
        return "Object of type " + this.type + " of type-group " + this.type_group;
    }
}

