package mario;

import engine.Game;
import engine.GameGraphics;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import engine.objects.PhysicsObject;
import engine.util.Vector2;
import mario.objects.Types;

import java.awt.*;
import java.util.HashMap;

/**
 * Base class for objects that use platforming physics.
 *
 * @author Galen Savidge
 * @version 5/9/2020
 */
public abstract class PlatformingObject extends PhysicsObject {

    /* Constants */
    protected static final Vector2 up = new Vector2(0, -1);
    protected static final Vector2 down = new Vector2(0, Mario.getGridScale()/4.0);


    /* Instance variables */
    protected State state;
    protected GroundType ground_found;
    protected double height;
    protected Direction direction_facing = Direction.LEFT;


    /* Constructor */
    public PlatformingObject(int priority, int layer, double x, double y) {
        super(priority, layer, x, y);
    }

    public PlatformingObject(int priority, int layer, HashMap<String, Object> args) {
        super(priority, layer, args);
    }


    /* Data types */
    protected enum Direction {
        RIGHT,
        LEFT
    }

    protected enum GroundType {
        NONE,
        FLAT,
        SLOPE
    }


    /* State machine template */

    /**
     * States made from this base class will have their functions executed in the following order every frame:
     * 1. The state will be switched to {@link #next_state} as set by {@link #setNextState}. At this point, {@link #exit}
     * will be called for the last state and {@link #enter} will be called for the new state.
     * 2. The current state's {@link #update} is called.
     * 3. Physics updates. The parent's {@link #position} is updated based on {@link #velocity} and the state's
     * {@link #handleCollisionEvent} is called for each object collided with while moving.
     * 4. The current ground type {@link #ground_found} is updated based on the collisions encountered.
     * 5. The current state's {@link #draw} is called.
     */
    protected abstract class State {
        State next_state = null;

        /**
         * @return An identifier string that can be used for state checks.
         */
        abstract String getState();

        /**
         * Flags the state machine to change states at the end of this step.
         * @param next_state The {@link State} to switch to.
         */
        void setNextState(State next_state) {
            this.next_state = next_state;
        }

        /**
         * Switches states.
         * @return The {@link State} switched to.
         */
        State switchToNextState() {
            if(next_state != null) {
                this.exit();
                next_state.enter();
                return next_state;
            }
            else {
                return this;
            }
        }

        /**
         * Called automatically when this {@link State} is entered.
         */
        void enter() {}

        /**
         * Called automatically when this {@link State} is left.
         */
        void exit() {}

        /**
         * Called every step during this object's update.
         * @see State
         */
        void update() {}

        /**
         * Called every step during this object's draw.
         * @see State
         */
        void draw() {}

        /**
         * Called when this object attempts to move into, and is rejected by, the {@link Collider} of another object.
         * @param c A detailed {@link Collision} object.
         * @param c_ground_type The {@link GroundType} of the object encountered.
         */
        void handleCollisionEvent(Collision c, GroundType c_ground_type) {
            velocity = velocity.difference(velocity.projection(c.normal_reject));
        }

        /**
         * Called when this object's {@link Collider} detects that it is intersecting another {@link Collider}.
         * @param c A non-detailed {@link Collision} object.
         */
        void handleIntersectionEvent(Collision c) {}
    }

    public String getState() {
        return state.getState();
    }


    /* Physics */

    /**
     * @param v The current velocity vector.
     * @param gravity_acceleration The downwards acceleration due to gravity.
     * @param max_speed The maximum downwards speed allowed.
     * @return A new velocity vector.
     */
    protected Vector2 applyGravity(Vector2 v, double gravity_acceleration, double max_speed) {
        Vector2 new_v = v.copy();
        new_v.y += gravity_acceleration*Game.stepTimeSeconds();
        if(new_v.y > max_speed) {
            new_v.y = max_speed;
        }
        return new_v;
    }

    /**
     * @param v_parallel_to_ground Velocity component parallel to the ground.
     * @param friction_acceleration The acceleration due to friction.
     * @return A new velocity vector (parallel to the ground) with friction applied.
     */
    protected Vector2 applyFriction(Vector2 v_parallel_to_ground, double friction_acceleration) {
        double friction_delta = friction_acceleration*Game.stepTimeSeconds();
        if(v_parallel_to_ground.abs() > friction_delta) {
            return v_parallel_to_ground.multiply(1.0 - friction_delta/v_parallel_to_ground.abs());
        }
        else {
            return Vector2.zero();
        }
    }


    /* Ground checks */

    /**
     * Checks up to 1/2 grid square down for ground. If it is found, moves this object straight down to the point of
     * contact with the ground.
     * @return A detailed {@link Collision} object.
     */
    protected Collision snapToGround() {
        Collision collision = sweepForCollision(down);
        // Check that down is ground
        if(checkGroundType(collision.normal_reject) != GroundType.NONE) {
            // Snap to ground
            position = position.sum(collision.to_contact);
        }
        return collision;
    }

    /**
     * @param normal The normal vector of a surface, e.g. the {@code normal_reject} from a detailed {@link Collision}.
     */
    protected GroundType checkGroundType(Vector2 normal) {
        if(normal == null) {
            return GroundType.NONE;
        }

        // Check if normal is pointing up and slope is less than about 46 degrees
        if(normal.x == 0 && normal.y < 0) {
            return GroundType.FLAT;
        }
        else if(normal.y < 0 && Math.abs(normal.y/normal.x) >= 0.95) {
            return GroundType.SLOPE;
        }
        else {
            return GroundType.NONE;
        }
    }


    /* Misc */

    protected void drawSprite(Image image) {
        drawSprite(image, false);
    }

    protected void drawSprite(Image image, boolean flip_vertical) {
        GameGraphics.drawImage((int)position.x, (int)position.y, false,
                direction_facing == Direction.RIGHT, flip_vertical, 0, image);
    }


    /* Template overridden methods */

    @Override
    public void update() {
        // Switch states
        state = state.switchToNextState();

        // Run state update code
        state.update();

        // Integrate velocity
        Vector2 delta_position = velocity.multiply(Game.stepTimeSeconds());

        // Move, colliding with objects
        ground_found = GroundType.NONE;
        collideWithObjects(delta_position);
    }

    @Override
    protected boolean collidesWith(Collision c) {
        if(c.collided_with.solid) {
            return true;
        }

        // Semisolid collision check
        else if(c.collided_with.hasTag(Types.semisolid_tag)) {
            return position.y+height-Mario.getGridScale()/2.0-Collider.edge_separation < c.collided_with.position.y
                    && c.normal_reject.x == 0;
        }
        else {
            return false;
        }
    }

    @Override
    public void collisionEvent(Collision c) {
        // Get ground type of this collision
        GroundType c_ground_type = checkGroundType(c.normal_reject);

        // Update velocity or do other things based on state behavior
        state.handleCollisionEvent(c, c_ground_type);

        // Record ground type in global variable
        if(c_ground_type == GroundType.FLAT ||
                (c_ground_type == GroundType.SLOPE && ground_found != GroundType.FLAT)) {
            ground_found = c_ground_type;
        }
    }

    @Override
    public void intersectionEvent(Collision c) {
        state.handleIntersectionEvent(c);
    }

    @Override
    public void draw() {
        state.draw();
    }

    @Override
    public void delete() {
        state.exit();
        super.delete();
    }
}
