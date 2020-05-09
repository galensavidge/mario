package mario;

import engine.Game;
import engine.GameGraphics;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import engine.util.Vector2;
import mario.objects.Types;
import mario.objects.WorldObject;

import java.awt.*;

public class PlatformingObject extends WorldObject {

    /* Constants */
    protected static final Vector2 up = new Vector2(0, -1);
    protected static final Vector2 down = new Vector2(0, Mario.getGridScale()/2.0);


    /* Instance variables */
    protected State state;
    protected GroundType ground_found;
    protected double height;
    protected Direction direction_facing = Direction.LEFT;


    /* Constructor */
    public PlatformingObject(int priority, int layer, double x, double y) {
        super(priority, layer, x, y);
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
    protected abstract class State {
        State next_state = null;

        abstract String getState();

        void setNextState(State next_state) {
            this.next_state = next_state;
        }

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

        void enter() {

        }

        void exit() {

        }

        void update() {

        }

        void draw() {

        }

        void handleCollision(Collision c, GroundType c_ground_type) {
            velocity = velocity.difference(velocity.projection(c.normal_reject));
        }
    }


    /* Physics */

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

    protected Collision snapToGround() {
        Collision collision = sweepForCollision(down);
        if(collision.collision_found) {
            // Check that down is ground
            if(checkGroundType(collision.normal_reject) != GroundType.NONE) {
                // Snap to ground
                position = position.sum(collision.to_contact);
            }
        }
        return collision;
    }

    protected GroundType checkGroundType(Vector2 normal) {
        if(normal == null) {
            return GroundType.NONE;
        }

        // Check if normal is pointing up and slope is less than about 46 degrees
        if(normal.x == 0) {
            return GroundType.FLAT;
        }
        else if(normal.y < 0 && Math.abs(normal.y/normal.x) >= 0.95) {
            return GroundType.SLOPE;
        }
        else {
            return GroundType.NONE;
        }
    }

    protected Vector2 getHorizontalVelocityRelativeToGround(Collision ground) {
        if(!ground.collision_found) {
            return null;
        }
        return velocity.normalComponent(ground.normal_reject).difference(ground.collided_with.velocity);
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
        else if(c.collided_with.getTypeGroup().equals(Types.semisolid_type_group)) {
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
        state.handleCollision(c, c_ground_type);

        // Record ground type in global variable
        if(c_ground_type == GroundType.FLAT ||
                (c_ground_type == GroundType.SLOPE && ground_found != GroundType.FLAT)) {
            ground_found = c_ground_type;
        }
    }

    @Override
    public void draw() {
        if(isOnScreen()) {
            state.draw();
        }
    }
}
