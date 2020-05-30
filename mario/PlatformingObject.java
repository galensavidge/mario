package mario;

import engine.Game;
import engine.collider.Intersection;
import engine.graphics.GameGraphics;
import engine.collider.Collider;
import engine.collider.Collision;
import engine.objects.PhysicsObject;
import engine.util.Vector2;
import mario.objects.Ground;
import mario.objects.Types;

import java.awt.*;
import java.util.HashMap;

/**
 * Base class for objects that use platforming physics.
 *
 * @author Galen Savidge
 * @version 5/19/2020
 */
public abstract class PlatformingObject extends PhysicsObject {

    /* Constants */
    protected static final Vector2 up = new Vector2(0, -1);
    protected static final Vector2 down = new Vector2(0, Mario.getGridScale()/4.0);


    /* Instance variables */
    protected State state;
    protected GroundType ground_found;
    protected Direction direction_facing = Direction.LEFT;


    /* Constructors */
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

    /* Accessors */
    public double getHeight() {
        return collider.getHeight();
    }

    /* State machine template */

    /**
     * States made from this base class will have their functions executed in the following order every frame: 1. The
     * state will be switched to {@link #next_state} as set by {@link #setNextState}. At this point, {@link #exit} will
     * be called for the last state and {@link #enter} will be called for the new state. 2. The current state's {@link
     * #update} is called. 3. Physics updates. The parent's position is updated based on {@link #velocity} and
     * the state's {@link #handlePhysicsCollisionEvent} is called for each object collided with while moving. 4. The
     * current ground type {@link #ground_found} is updated based on the collisions encountered. 5. The current state's
     * {@link #draw} is called.
     */
    protected abstract class State {
        public State next_state = null;

        /**
         * @return An identifier string that can be used for state checks.
         */
        public abstract String getState();

        /**
         * Flags the state machine to change states at the end of this step.
         *
         * @param next_state The {@link State} to switch to.
         */
        public void setNextState(State next_state) {
            this.next_state = next_state;
        }

        /**
         * Switches states.
         *
         * @return The {@link State} switched to.
         */
        protected State switchToNextState() {
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
        public void enter() {}

        /**
         * Called automatically when this {@link State} is left.
         */
        public void exit() {}

        /**
         * Called every step during this object's update.
         *
         * @see State
         */
        public void update() {}

        /**
         * Called every step during this object's draw.
         *
         * @see State
         */
        public void draw() {}

        /**
         * Called when this object attempts to move into, and is rejected by, the {@link Collider} of another object.
         *
         * @param c_ground_type The {@link GroundType} of the object encountered.
         */
        protected void handlePhysicsCollisionEvent(Intersection i, GroundType c_ground_type) {
            velocity = inelasticCollision(velocity, i);
        }

        /**
         * Called when this object's {@link Collider} detects that it is intersecting another {@link Collider}.
         */
        protected void handleCollisionEvent(PhysicsObject other) {}
    }

    public String getState() {
        return state.getState();
    }


    /* Physics */

    /**
     * @param v                    The current velocity vector.
     * @param gravity_acceleration The downwards acceleration due to gravity.
     * @param max_speed            The maximum downwards speed allowed.
     * @return A new velocity vector.
     */
    protected Vector2 applyGravity(Vector2 v, double gravity_acceleration, double max_speed) {
        if(gravity_acceleration == 0) return v;
        Vector2 new_v = v.copy();
        new_v.y += gravity_acceleration*Game.stepTimeSeconds();
        if(new_v.y > max_speed) {
            new_v.y = max_speed;
        }
        return new_v;
    }

    /**
     * @param v_parallel_to_ground  Velocity component parallel to the ground.
     * @param friction_acceleration The acceleration due to friction.
     * @return A new velocity vector (parallel to the ground) with friction applied.
     */
    protected Vector2 applyFriction(Vector2 v_parallel_to_ground, double friction_acceleration) {
        if(friction_acceleration == 0) return v_parallel_to_ground;
        double friction_delta = friction_acceleration*Game.stepTimeSeconds();
        if(v_parallel_to_ground.abs() > friction_delta) {
            return v_parallel_to_ground.multiply(1.0 - friction_delta/v_parallel_to_ground.abs());
        }
        else {
            return Vector2.zero();
        }
    }

    /**
     * Finds the net velocity after an inelastic collision described by {@code i}. No friction is calculated.
     *
     * @param v The current velocity vector.
     * @param i The {@link Intersection} object describing the collision.
     * @return A new velocity vector.
     */
    protected Vector2 inelasticCollision(Vector2 v, Intersection i) {
        Vector2 v_parallel_to_collision = v.normalComponent(i.getNormal());
        Vector2 object_v_normal_to_collision = i.collided_with.velocity.projection(i.getNormal());
        return v_parallel_to_collision.sum(object_v_normal_to_collision);
    }

    protected boolean slideAroundCorners(Intersection i) {
        Vector2 delta_p = velocity.multiply(Game.stepTimeSeconds());
        Vector2 parallel_axis = i.getNormal().RHNormal().multiply(2*Mario.getPixelSize());
        Vector2[] position_checks = {getPosition().sum(parallel_axis), getPosition().sum(parallel_axis.multiply(-1))};
        Vector2 old_position = getPosition();
        for(Vector2 p : position_checks) {
            setPosition(p);
            if(sweepForCollision(delta_p) != null) {
                return true;
            }
        }

        setPosition(old_position);
        return false;
    }


    /* Ground checks */

    /**
     * Checks up to 1/2 grid square down for ground. If it is found, moves this object straight down to the point of
     * contact with the ground.
     *
     * @return A detailed {@link Collision} object.
     */
    protected Intersection snapToGround() {
        Intersection i = sweepForCollision(down);

        // Check that down is ground
        if(checkGroundType(i) != GroundType.NONE) {
            // Snap to ground
            addPosition(i.getToContact());
        }
        return i;
    }

    protected GroundType checkGroundType(Intersection i) {
        if(i == null) {
            return GroundType.NONE;
        }

        Vector2 normal = i.getNormal();
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
        GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, false,
                direction_facing == Direction.RIGHT, 0, 0, image);
    }


    /* Template overridden methods */

    @Override
    public void prePhysicsUpdate() {
        // Switch states
        state = state.switchToNextState();

        // Run state update code
        state.update();

        ground_found = GroundType.NONE;
    }

    @Override
    protected boolean collidesWith(Intersection i) {
        if(i.collided_with.solid) {
            return true;
        }

        // Semisolid collision check
        else if(i.collided_with.hasTag(Types.semisolid_tag)) {
            return getPosition().y + collider.getHeight() < i.collided_with.getPosition().y && i.getNormal().y < 0;
        }
        else {
            return false;
        }
    }

    @Override
    public void physicsCollisionEvent(Intersection i) {
        // Get ground type of this collision
        GroundType c_ground_type = checkGroundType(i);

        // Update velocity or do other things based on state behavior
        state.handlePhysicsCollisionEvent(i, c_ground_type);

        // Record ground type in global variable
        if(c_ground_type == GroundType.FLAT ||
                (c_ground_type == GroundType.SLOPE && ground_found != GroundType.FLAT)) {
            ground_found = c_ground_type;
        }
    }

    @Override
    public void collisionEvent(PhysicsObject other) {
        state.handleCollisionEvent(other);
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
