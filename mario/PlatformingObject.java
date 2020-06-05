package mario;

import engine.Game;
import engine.collider.Intersection;
import engine.graphics.GameGraphics;
import engine.collider.Collider;
import engine.collider.Collision;
import engine.objects.PhysicsObject;
import engine.util.Vector2;
import mario.objects.Types;

import java.awt.*;
import java.util.HashMap;

/**
 * Base class for objects that use platforming physics.
 *
 * @author Galen Savidge
 * @version 6/4/2020
 */
public abstract class PlatformingObject extends PhysicsObject {

    /* Constants */
    protected static final Vector2 up = new Vector2(0, -1);
    protected static final Vector2 down = new Vector2(0, Mario.getGridScale()/4.0);


    /* Instance variables */
    protected State state;


    /**
     * Contains information on the object that this is standing on (if applicable) this frame and last frame.
     */
    protected Ground ground_found = new Ground(null), last_ground = new Ground(null);

    /**
     * The direction that the object is facing (left or right). Used for drawing, not automatically updated.
     */
    protected Direction direction_facing = Direction.LEFT;


    /* Constructors and destructors */
    public PlatformingObject(int priority, int layer, double x, double y) {
        super(priority, layer, x, y);
    }

    public PlatformingObject(int priority, int layer, HashMap<String, Object> args) {
        super(priority, layer, args);
    }

    @Override
    public void delete() {
        state.exit();
        super.delete();
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

    /**
     * A class to hold some data about the object this PlatformingObject is standing on.
     */
    protected static class Ground {
        /**
         * The {@link Intersection} with the ground.
         */
        public final Intersection intersection;

        /**
         * The {@link GroundType} of the collision and object collided with.
         */
        public final GroundType type;

        /**
         * The velocity of the surface that this PlatformingObject is standing on.
         */
        public final Vector2 velocity;

        /**
         * Records the passed {@link Intersection} object and determines both the {@link GroundType} and surface
         * velocity of the surface collided with.
         */
        public Ground(Intersection intersection) {
            this.intersection = intersection;
            type = checkGroundType(this.intersection);
            if(this.intersection != null) {
                velocity = this.intersection.collided_with.velocity.copy();
            }
            else {
                velocity = Vector2.zero();
            }
        }

        /**
         * Gets the {@link GroundType} for the surface collided with in {@link Intersection} {@code i}.
         */
        private static GroundType checkGroundType(Intersection i) {
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

        /**
         * @return True iff {@code this} and {@code other} are valid ground on surfaces of the same object.
         */
        public boolean sameObject(Ground other) {
            try {
                return this.intersection.collided_with == other.intersection.collided_with
                        && this.type != GroundType.NONE && other.type != GroundType.NONE;
            }
            catch(NullPointerException e) {
                return false;
            }
        }
    }


    /* Accessors */

    /**
     * @return The height of this object's collider's bounding box.
     */
    public double getHeight() {
        return collider.getHeight();
    }

    /**
     * @return The current state's name.
     */
    public String getState() {
        return state.getState();
    }


    /* State machine template */

    /**
     * States made from this base class will have their functions executed in the following order every frame:
     * <p>
     * 1. If necessary, the current state is updated to the state set by {@link #setNextState}. At this point, the last
     * state's {@link #exit} is called, followed by the next state's {@link #enter}.
     * <p>
     * 2. The parent's position is updated based on {@link #velocity} and the state's {@link
     * #handlePhysicsCollisionEvent} is called for each object collided with while moving. The state's {@link
     * #handleCollisionEvent} is called for each object intersected with at the new position.
     * <p>
     * 3. The object attempts to snap to ground if {@code state.stick_to_ground == true}.
     * <p>
     * 4. {@link #ground_found} and {@link #last_ground} are updated.
     * <p>
     * 5. The current state's {@link #update} is called.
     * <p>
     * 6. The current state's {@link #draw} is called.
     */
    protected abstract class State {
        public State next_state = null;

        /**
         * Set to {@code true} to snap to ground before making ground checks each step.
         */
        public boolean stick_to_ground = false;

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
         */
        protected void handlePhysicsCollisionEvent(Ground g) {
            velocity = inelasticCollision(velocity, g.intersection);
        }

        /**
         * Called when this object's {@link Collider} detects that it is intersecting another {@link Collider}.
         */
        protected void handleCollisionEvent(PhysicsObject other) {}
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

    /**
     * Checks up to 1/2 grid square down for ground. If it is found, moves this object straight down to the point of
     * contact with the ground.
     *
     * @return A detailed {@link Collision} object.
     */
    protected Intersection snapToGround() {
        Ground g = new Ground(sweepForCollision(down));

        // Check that down is ground
        if(g.type != GroundType.NONE) {
            // Snap to ground
            addPosition(g.intersection.getToContact());
        }
        return g.intersection;
    }


    /* Misc */

    /**
     * Draws a sprite at the object's current position. Flips the sprite horizontally if the object is facing right.
     */
    protected void drawSprite(Image image) {
        GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, false,
                direction_facing == Direction.RIGHT, 0, 0, image);
    }


    /* Events */

    @Override
    public void prePhysicsUpdate() {
        // Switch states
        state = state.switchToNextState();
    }

    @Override
    public void postPhysicsUpdate() {
        // Stick to ground
        if(state.stick_to_ground) {
            snapToGround();
        }

        // Check for ground
        last_ground = ground_found;
        ground_found = new Ground(checkDirection(down));

        // Run state update code
        state.update();
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
        Ground g = new Ground(i);

        // Update velocity or do other things based on state behavior
        state.handlePhysicsCollisionEvent(g);

        // Record ground type in global variable
        if(g.type == GroundType.FLAT ||
                (g.type == GroundType.SLOPE && ground_found.type != GroundType.FLAT)) {
            ground_found = new Ground(i);
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
}
