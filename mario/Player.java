package mario;

import engine.*;
import engine.objects.*;
import engine.util.Vector2;
import engine.objects.Collider.Collision;
import mario.objects.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * The physical object that the player controls.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Player extends PhysicsObject {

    /* Class constants */

    public static final String type_name = "Player";

    public static final int height = (int)(Mario.getGridScale()*1.5);

    private static final Vector2 gravity = new Vector2(0,2500); // Pixels/s^2
    private static final double max_fall_speed = 700;
    private static final double friction = 600;
    private static final double slide_friction = 400;

    private static final double walk_max_speed = 300;
    private static final double walk_accel = 900;
    private static final double run_speed = 625;
    private static final double run_max_speed = 650;
    private static final double run_accel = 1200;

    private static final double high_jump_xspeed = 200;
    private static final double jump_speed = -700;
    private static final int high_jump_time = Mario.fps/3;
    private static final int low_jump_time = Mario.fps/4;

    private static final Vector2 up = new Vector2(0, -1);
    private static final Vector2 down = new Vector2(0, Mario.getGridScale()/2.0);

    private static final String[] walk_sprite_Files = {"./sprites/mario-walk-1.png", "./sprites/mario-walk-2.png"};
    private static final Sprite walk_sprite = new Sprite(walk_sprite_Files);
    private static final String[] run_sprite_files = {"./sprites/mario-run-1.png", "./sprites/mario-run-2.png"};
    private static final Sprite run_sprite = new Sprite(run_sprite_files);
    private static final Image skid_sprite = GameGraphics.getImage("./sprites/mario-skid.png");
    private static final Image jump_sprite = GameGraphics.getImage("./sprites/mario-jump.png");
    private static final Image fall_sprite = GameGraphics.getImage("./sprites/mario-fall.png");
    private static final Image run_jump_sprite = GameGraphics.getImage("./sprites/mario-run-jump.png");
    private static final Image duck_sprite = GameGraphics.getImage("./sprites/mario-duck.png");
    private static final Image slide_sprite = GameGraphics.getImage("./sprites/mario-slide.png");


    /* Instance variables */

    private final PlayerStateMachine state_machine;

    private Direction direction_facing = Direction.RIGHT;

    public Player(double x, double y) {
        super(10, 10, x, y);
        collider = Collider.newBox(this,0,
                Mario.getGridScale()/2.0, Mario.getGridScale(), Mario.getGridScale());
        collider.active_check = true;
        this.type = type_name;
        state_machine = new PlayerStateMachine(States.WALK);
    }

    @Override
    protected boolean collideWith(PhysicsObject o) {
        if(o.solid) {
            return true;
        }
        else if(o.getType().equals(CloudBlock.type_name) &&
                position.y + height - 8 - Collider.edge_separation < o.position.y && velocity.y >= 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void collisionEvent(PhysicsObject object) {
        switch(object.getType()) {
            case Coin.type_name:
                GameController.coins += 1;
                object.delete();
                break;
            default:
                //System.out.println("Collided with "+object.getType());
                break;
        }
    }

    @Override
    public void update() {
        state_machine.state.update();
    }

    @Override
    public void draw() {
        state_machine.state.draw();
    }


    /* Private data types */

    private enum Direction {
        RIGHT,
        LEFT
    }

    private enum GroundType {
        NONE,
        FLAT,
        SLOPE
    }

    private enum States {
        WALK,
        JUMP,
        RUN_JUMP,
        FALL,
        RUN_FALL,
        DUCK,
        SLIDE
    }


    /* State machine */

    private class PlayerStateMachine {
        Dictionary<States, PlayerState> state_objects = new Hashtable<>();
        PlayerState state;

        PlayerStateMachine(States state) {
            state_objects.put(States.WALK, new WalkState());
            state_objects.put(States.JUMP, new JumpState());
            state_objects.put(States.RUN_JUMP, new RunJumpState());
            state_objects.put(States.FALL, new FallState());
            state_objects.put(States.RUN_FALL, new RunFallState());
            state_objects.put(States.SLIDE, new SlideState());
            state_objects.put(States.DUCK, new DuckState());

            this.state = state_objects.get(state);
            this.state.enter();
        }

        void changeState(States new_state) {
            state.exit();
            state = state_objects.get(new_state);
            state.enter();
        }
    }

    private abstract class PlayerState {

        protected void gravity() {
            velocity = velocity.sum(gravity.multiply(Game.stepTimeSeconds()));
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

        protected Collision snapToGround() {
            Collision collision = sweepForCollision(down);
            if(collision.collision_found) {
                // Check that normal points up and is within 45 degrees of vertical
                if(checkGroundType(collision.normal_reject) != GroundType.NONE) {
                    // Snap to ground
                    position = position.sum(collision.to_contact);
                }
            }
            return collision;
        }

        protected Vector2 getVelocityParallelToGround(Collision ground) {
            if(!ground.collision_found) {
                return null;
            }
            return velocity.normalComponent(ground.normal_reject).difference(ground.collided_with.velocity);
        }

        protected void groundPhysics(Vector2 v_relative_to_ground, double friction) {
            // Friction
            double friction_delta = friction*Game.stepTimeSeconds();
            if(v_relative_to_ground.abs() > friction_delta) {
                velocity = velocity.difference(v_relative_to_ground.normalize().multiply(friction_delta));
            }
            else {
                velocity = velocity.difference(v_relative_to_ground);
            }
        }

        /**
         *
         * @param slide True to slide on all surfaces, false to only slide on steep surfaces.
         * @return The type of ground encountered.
         */
        protected GroundType updatePositionWithCollisions(boolean slide) {
            // Cap fall speed
            if(velocity.y > max_fall_speed) {
                velocity.y = max_fall_speed;
            }

            // Integrate velocity
            Vector2 delta_position = velocity.multiply(Game.stepTimeSeconds());

            // Move, colliding with objects
            ArrayList<Collision> collisions = collideWithObjects(delta_position);

            // Subtract velocity when colliding with things
            GroundType ground_found = GroundType.NONE;
            for(Collision c : collisions) {
                GroundType c_ground_type = checkGroundType(c.normal_reject);
                if(slide) {
                    velocity = velocity.difference(velocity.projection(c.normal_reject));
                }
                else {
                    if(c_ground_type != GroundType.NONE) {
                        velocity.y = 0;
                    }
                    else {
                        velocity = velocity.difference(velocity.projection(c.normal_reject));
                    }
                }

                // Return highest type found in the order NONE < SLOPE < FLAT
                if(c_ground_type == GroundType.FLAT ||
                        (c_ground_type == GroundType.SLOPE && ground_found != GroundType.FLAT)) {
                    ground_found = c_ground_type;
                }
            }

            return ground_found;
        }

        protected void movement(double accel, double max_speed, Vector2 v_relative_to_ground,
                                Vector2 ground_normal) {
            if(ground_normal == null) {
                ground_normal = up;
            }

            Vector2 vx = v_relative_to_ground.normalComponent(ground_normal);
            Vector2 axis = ground_normal.RHNormal().normalize();

            if(vx.abs() <= max_speed || vx.x >= 0) {
                if (InputManager.getDown(InputManager.K_LEFT)) {
                    velocity = velocity.difference(axis.multiply(accel * Game.stepTimeSeconds()));
                    direction_facing = Direction.LEFT;
                }
            }
            if(vx.abs() <= max_speed || vx.x <= 0) {
                if (InputManager.getDown(InputManager.K_RIGHT)) {
                    velocity = velocity.sum(axis.multiply(accel * Game.stepTimeSeconds()));
                    direction_facing = Direction.RIGHT;
                }
            }
        }

        protected void drawSprite(Image image) {
            GameGraphics.drawImage((int)position.x, (int)position.y, false,
                    direction_facing == Direction.RIGHT, false, 0, image);
        }


        abstract void update();

        abstract void draw();

        abstract void enter();

        abstract void exit();
    }

    private class WalkState extends PlayerState {

        Vector2 last_normal;
        Vector2 v_parallel_to_ground;
        boolean skidding;
        boolean running;

        @Override
        void update() {
            // Ground checks and pre-movement physics
            Collision ground = snapToGround();
            if(ground.collision_found) {
                v_parallel_to_ground = getVelocityParallelToGround(ground);
                groundPhysics(v_parallel_to_ground, friction);
            }
            else {
                v_parallel_to_ground = velocity;
            }
            GroundType ground_type = checkGroundType(ground.normal_reject);

            // Check for skidding
            skidding = (InputManager.getDown(InputManager.K_RIGHT) && v_parallel_to_ground.x < 0) ||
                    (InputManager.getDown(InputManager.K_LEFT) && v_parallel_to_ground.x > 0);

            // Left/right input and running check
            running = false;
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                movement(run_accel, run_max_speed, v_parallel_to_ground, ground.normal_reject);

                if(Math.abs(v_parallel_to_ground.x) >= run_speed) {
                    running = true;
                }
            }
            else {
                movement(walk_accel, walk_max_speed, v_parallel_to_ground, ground.normal_reject);
            }

            // Stick to slopes
            if(ground.collision_found) {
                velocity = velocity.difference(velocity.projection(ground.normal_reject));
            }

            // Move based on new velocity
            updatePositionWithCollisions(false);

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                if(running) {
                    state_machine.changeState(States.RUN_JUMP);
                }
                else {
                    state_machine.changeState(States.JUMP);
                }
            }

            // Fall
            if(ground_type == GroundType.NONE) {
                if(running) {
                    state_machine.changeState(States.RUN_FALL);
                }
                else {
                    state_machine.changeState(States.FALL);
                }
            }

            // Slide
            if(InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_type == GroundType.FLAT) {
                    state_machine.changeState(States.DUCK);
                }
                else if(ground_type == GroundType.SLOPE) {
                    state_machine.changeState(States.SLIDE);
                }
            }
        }

        @Override
        void draw() {
            if(skidding) {
                drawSprite(skid_sprite);
            }
            else {
                Sprite s;
                if (running) {
                    s = run_sprite;
                } else {
                    s = walk_sprite;
                }
                s.setFrameTime((int)(run_max_speed - Math.abs(v_parallel_to_ground.x)/2)/100);
                if(Math.abs(v_parallel_to_ground.x) < walk_max_speed /20) {
                    s.reset();
                }
                drawSprite(s.getCurrentFrame());
            }
        }

        @Override
        void enter() {
            last_normal = new Vector2(0, -1);
            Collision ground = snapToGround();

            if(ground.collision_found) {
                velocity.y = 0;
                velocity = ground.normal_reject.RHNormal().normalize().multiply(velocity.x);
            }
        }

        @Override
        void exit() {

        }
    }

    private class JumpState extends PlayerState {

        protected int timer;

        @Override
        void update() {
            // Input checks
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                movement(walk_accel, run_max_speed, velocity, null);
            }
            else {
                movement(walk_accel, walk_max_speed, velocity, null);
            }

            // Move based on new velocity
            updatePositionWithCollisions(true);

            timer--;
            if(timer == 0 || !InputManager.getDown(InputManager.K_JUMP) || velocity.y >= jump_speed/2) {
                state_machine.changeState(States.FALL);
            }
        }

        @Override
        void draw() {
            drawSprite(jump_sprite);
        }

        @Override
        void enter() {
            velocity.y = jump_speed;
            if(Math.abs(velocity.x) >= high_jump_xspeed && InputManager.getDown(InputManager.K_SPRINT)) {
                timer = high_jump_time;
            }
            else {
                timer = low_jump_time;
            }
        }

        @Override
        void exit() {

        }
    }

    private class RunJumpState extends JumpState {
        @Override
        void update() {
            // Input checks
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                movement(walk_accel, run_max_speed, velocity,null);
            }
            else {
                movement(walk_accel, walk_max_speed, velocity, null);
            }

            // Move based on new velocity
            updatePositionWithCollisions(true);

            timer--;
            if(timer == 0 || InputManager.getReleased(InputManager.K_JUMP) || velocity.y >= 0) {
                state_machine.changeState(States.RUN_FALL);
            }
        }

        @Override
        void draw() {
            drawSprite(run_jump_sprite);
        }
    }

    private class FallState extends PlayerState {

        @Override
        void update() {
            gravity();

            if(InputManager.getDown(InputManager.K_SPRINT)) {
                movement(walk_accel, run_max_speed, velocity, null);
            }
            else {
                movement(walk_accel, walk_max_speed, velocity,null);
            }

            if(InputManager.getDown(InputManager.K_DOWN)) {
                GroundType ground_type = updatePositionWithCollisions(true);
                if (ground_type == GroundType.SLOPE) {
                    state_machine.changeState(States.SLIDE);
                }
                else if(ground_type == GroundType.FLAT) {
                    state_machine.changeState(States.DUCK);
                }
            }
            else {
                GroundType ground_type = updatePositionWithCollisions(false);
                if (ground_type != GroundType.NONE) {
                    state_machine.changeState(States.WALK);
                }
            }
        }

        @Override
        void draw() {
            if(velocity.y > 0) {
                drawSprite(fall_sprite);
            }
            else {
                drawSprite(jump_sprite);
            }
        }

        @Override
        void enter() {

        }

        @Override
        void exit() {

        }
    }

    private class RunFallState extends FallState {
        @Override
        void draw() {
            drawSprite(run_jump_sprite);
        }
    }

    private class SlideState extends DuckState {

        @Override
        void update() {
            super.update();

            if(InputManager.getDown(InputManager.K_DOWN) && velocity.x == 0) {
                state_machine.changeState(States.DUCK);
            }
        }

        @Override
        void draw() {
            drawSprite(slide_sprite);
        }
    }

    private class DuckState extends PlayerState {
        @Override
        void update() {
            // Physics
            Collision ground = sweepForCollision(down);
            Vector2 v_parallel_to_ground;
            if(ground.collision_found) {
                v_parallel_to_ground = getVelocityParallelToGround(ground);
                groundPhysics(v_parallel_to_ground, slide_friction);
            }
            gravity();

            GroundType ground_type = updatePositionWithCollisions(true);

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                state_machine.changeState(States.WALK);
            }

            // Slide
            if(ground_type == GroundType.SLOPE) {
                state_machine.changeState(States.SLIDE);
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP) && ground_type != GroundType.NONE) {
                state_machine.changeState(States.JUMP);
            }
        }

        @Override
        void draw() {
            drawSprite(duck_sprite);
        }

        @Override
        void enter() {

        }

        @Override
        void exit() {

        }
    }
}
