package engine.objects;

import engine.Game;
import engine.collider.Collider;
import engine.collider.Collision;
import engine.collider.Intersection;
import engine.graphics.GameGraphics;
import engine.util.Line;
import engine.util.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The parent class for all objects that inhabit physical space in the game world.
 *
 * @author Galen Savidge
 * @version 6/1/2020
 */
public abstract class PhysicsObject extends GameObject {

    // Used to identify instances of child classes
    protected String type;
    protected String type_group;

    protected final ArrayList<String> tags = new ArrayList<>();

    // Variables used for physics calculations
    public boolean solid = false;
    public Collider collider;
    private Vector2 position;
    public Vector2 velocity;


    /* Constructors/destructors */

    public PhysicsObject(int priority, int layer, double x, double y) {
        super(priority, layer);
        setPosition(new Vector2(x, y));
        velocity = Vector2.zero();
    }

    public PhysicsObject(int priority, int layer, HashMap<String, Object> args) {
        super(priority, layer);
        setPosition(Vector2.zero());
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
            if(position != null) setPosition(((Vector2)position).copy());
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

    /**
     * @param tag A tag name.
     * @return True iff {@code this} has {@code tag} in its list of tags.
     */
    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    public Vector2 getPosition() {
        return position.copy();
    }

    public void setPosition(Vector2 position) {
        this.position = position.copy();

        if(collider != null) {
            collider.setPosition(position);
        }
    }

    public void setPosition(double x, double y) {
        setPosition(new Vector2(x, y));
    }

    public void addPosition(Vector2 delta_position) {
        setPosition(position.sum(delta_position));
    }

    public void addPosition(double x, double y) {
        setPosition(position.sum(new Vector2(x, y)));
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
     * Used to determine which objects to collide with and which to pass through. Override this function to change
     * collision behavior.
     *
     * @return True to collide with this object, false to pass through.
     */
    protected boolean collidesWith(Intersection i) {
        return i.collided_with.solid;
    }

    /**
     * Returns the collision first encountered when moving from {@code position} to {@code position + delta_position}.
     *
     * @return A {@link Intersection} if a collision was found, otherwise null.
     */
    protected Intersection sweepForCollision(Vector2 delta_position) {
        return collider.sweep(this.position, delta_position, this::collidesWith).popClosestIntersection();
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
     * @return A list of {@link Intersection} objects corresponding to the surfaces collided with.
     */
    protected ArrayList<Intersection> moveAndCollide(Vector2 delta_position) {
        ArrayList<Intersection> collisions = new ArrayList<>();
        if(delta_position.equals(Vector2.zero())) {
            return new ArrayList<>();
        }

        // Loop until a position is found with no collisions or we hit too many iterations
        for(int i = 0;i < 100;i++) {
            // Get the closest surface of the objects collided with
            Intersection closest = sweepForCollision(delta_position);

            if(closest == null) {
                addPosition(delta_position);
                break;
            }

            // Remove the portion of the attempted motion that is parallel to the normal vector
            delta_position = delta_position.sum(closest.getReject());

            // Send a physics collision event to this object and a collision event to the other object
            this.physicsCollision(closest);
            //Intersection other_i = new Intersection(this, closest.point, closest.edge, closest.ray, true);
            closest.collided_with.collisionEvent(this);
        }

        return collisions;
    }

    /**
     * Moves the minimum distance in the passed direction to no longer intersect solid objects. Does not move the object
     * if no collision-free position can be found.
     *
     * @param direction A vector defining the direction and maximum distance to move.
     * @return {@code true} if a valid new position was found, {@code false} if no valid position was found along {@code
     * direction}.
     */
    protected boolean escapeSolids(Vector2 direction) {
        if(collider.check(position, o -> o.solid).size() == 0) {
            return true;
        }

        Vector2 axis = direction.normalize();
        Collision collision = collider.sweep(position, direction);

        if(collision.collision_found) {
            while(collision.numIntersections() > 0) {
                // Get the closest collision encountered when travelling along direction
                Intersection closest = collision.popClosestIntersection();

                Vector2 escape = axis.multiply(closest.distance + Collider.reject_separation);

                // Done if no collision was found
                if(collider.check(position.sum(escape), o -> o.solid).size() == 0) {
                    addPosition(escape);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the object that touches this object in the direction closest to the passed direction, calculated based on
     * the {@code center} of this object's {@link Collider}.
     *
     * @param direction A vector of any magnitude defining the direction to check.
     * @return An {@link Intersection} if an object is found touching this object within 90 degrees of the passed
     * direction, otherwise {@code null}.
     */
    protected Intersection checkDirection(Vector2 direction) {
        direction = direction.normalize().multiply(2*Collider.reject_separation);

        Collision collision = collider.sweep(position, direction, this::collidesWith);

        if(collision.numIntersections() > 1) {

            // Get closest intersection to center axis of the object
            double shortest_distance = Double.MAX_VALUE;
            Intersection closest = null;
            Line axis = new Line(collider.getCenter(), collider.getCenter().sum(direction), false, false);
            for(Iterator<Intersection> intersections = collision.getIterator();intersections.hasNext();) {
                Intersection i = intersections.next();
                double distance_to_axis = axis.dropNormal(i.point).abs();
                if(distance_to_axis < shortest_distance) {
                    shortest_distance = distance_to_axis;
                    closest = i;
                }
            }
            return closest;
        }
        else {
            return collision.popClosestIntersection();
        }
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
    public void collisionEvent(PhysicsObject other) {}

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
        collisionEvent(i.collided_with);
    }

    @Override
    public void update() {
        prePhysicsUpdate();
        if(!velocity.equals(Vector2.zero())) {
            moveAndCollide(velocity.multiply(Game.stepTimeSeconds()));
            collider.setPosition(position);
        }
        for(PhysicsObject o : collider.check(position)) {
            this.collisionEvent(o);
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

