package engine.objects;

import engine.Game;
import engine.collider.Collider;
import engine.collider.Collision;
import engine.collider.Intersection;
import engine.graphics.GameGraphics;
import engine.util.Vector2;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 5/27/2020
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
    protected boolean collidesWith(Intersection i) {
        return i.collided_with.solid;
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
    protected ArrayList<Intersection> moveAndCollide(Vector2 delta_position) {
        ArrayList<Intersection> collisions = new ArrayList<>();
        if(delta_position.equals(Vector2.zero())) {
            return new ArrayList<>();
        }

        Vector2 new_position = position;

        // Loop until a position is found with no collisions or we hit too many iterations
        for(int i = 0;i < 100;i++) {
            // Determine the new position to check
            new_position = position.sum(delta_position);

            // Get the closest surface of the objects collided with
            Intersection closest = sweepForCollision(delta_position);

            if(closest != null) {
                // Remove the portion of the attempted motion that is parallel to the normal vector
                delta_position = delta_position.sum(closest.getReject());
                collisions.add(closest);
            }
            else {
                break;
            }
        }

        // Update object position
        position = new_position;
        collider.setPosition(position);

        // Send collision events
        for(Intersection i : collisions) {
            this.physicsCollision(i);
            Intersection other_i = new Intersection(this, i.point, i.edge, i.ray, true);
            i.collided_with.physicsCollision(other_i);
        }

        return collisions;
    }

    /**
     * Returns the {@link Collision} first encountered when moving from {@code position} to {@code position +
     * delta_position}.
     *
     * @return A {@link Collision} if a collision was found, otherwise null.
     */
    protected Intersection sweepForCollision(Vector2 delta_position) {
        Collision c = collider.sweep(this.position, delta_position);

        if(c.collision_found) {
            while(c.numIntersections() > 0) {
                // Find details for the closest collision encountered when travelling along delta_position
                Intersection closest = c.popClosestIntersection();

                // Done if: no collision was found, check all is true, or this object collides with the object at c
                if(collidesWith(closest)) {
                    return closest;
                }
            }
        }
        return null;
    }


    /* Overridable event handlers */

    /**
     * Override this method to respond to collisions with other objects. Events are generated when collision rejection
     * occurs when using {@link #moveAndCollide}.
     */
    public void physicsCollisionEvent(Intersection i) {}

    /**
     * Override this method to respond to collider intersections with other objects. Events are generated if {@code
     * collider.active_check} is {@code true} and this object intersects other objects, or when another object with
     * active checking enabled intersects this object, and after {@link #physicsCollisionEvent} is called.
     */
    public void collisionEvent(Intersection i) {}

    /**
     * Called after the world is finished loading and all object have been instantiated.
     */
    public void worldLoadedEvent() {}

    /**
     * Called each update before this object's physics is updated.
     */
    public void prePhysicsUpdate() {}

    /**
     * Called each update after this object's physics is updated.
     */
    public void postPhysicsUpdate() {}


    /* Events */

    private void physicsCollision(Intersection i) {
        physicsCollisionEvent(i);
        collisionEvent(i);
    }

    @Override
    public void update() {
        prePhysicsUpdate();
        if(!velocity.equals(Vector2.zero())) {
            moveAndCollide(velocity.multiply(Game.stepTimeSeconds()));
        }
        postPhysicsUpdate();
    }

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

