package mario;

import engine.Game;
import engine.GameGraphics;
import engine.InputManager;
import engine.Sprite;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import engine.util.Vector2;
import mario.objects.Coin;

import java.awt.*;

/**
 * The physical object that the player controls.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class NewPlayer extends PlatformingObject {

    /* Class constants */

    public static final String type_name = "Player";

    private static final double max_fall_speed = 700;
    private static final double gravity = 2500;
    private static final double friction = 600;
    private static final double slide_gravity = 1500;
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

    private static final String[] walk_sprite_files = {"./sprites/mario-walk-1.png", "./sprites/mario-walk-2.png"};
    private static final Sprite walk_sprite = new Sprite(walk_sprite_files);
    private static final String[] run_sprite_files = {"./sprites/mario-run-1.png", "./sprites/mario-run-2.png"};
    private static final Sprite run_sprite = new Sprite(run_sprite_files);
    private static final Image skid_sprite = GameGraphics.getImage("./sprites/mario-skid.png");
    private static final Image jump_sprite = GameGraphics.getImage("./sprites/mario-jump.png");
    private static final Image fall_sprite = GameGraphics.getImage("./sprites/mario-fall.png");
    private static final Image run_jump_sprite = GameGraphics.getImage("./sprites/mario-run-jump.png");
    private static final Image duck_sprite = GameGraphics.getImage("./sprites/mario-duck.png");
    private static final Image slide_sprite = GameGraphics.getImage("./sprites/mario-slide.png");


    /* Instance variables */

    public NewPlayer(double x, double y) {
        super(10, Mario.player_layer, x, y);
        this.suspend_tier = Mario.pause_suspend_tier;
        collider = Collider.newBox(this,0,
                Mario.getGridScale()/2.0, Mario.getGridScale(), Mario.getGridScale());
        collider.active_check = true;
        this.type = type_name;
        this.type_group = type_name;
        this.height = (int)(Mario.getGridScale()*1.5);
        this.direction_facing = Direction.RIGHT;
        this.state = new WalkState();
        this.state.enter();
    }

    @Override
    public void collisionEvent(Collision c) {
        if(c.isDetailed()) {
            super.collisionEvent(c);
        }

        switch(c.collided_with.getType()) {
            case Coin.type_name:
                GameController.coins += 1;
                c.collided_with.delete();
                break;
            case Galoomba.type_name:
                Galoomba g = (Galoomba)c.collided_with;
                if(position.y + height < c.collided_with.position.y + g.height/2.0) {
                    state.setNextState(new JumpState(high_jump_time, false, false));
                    g.stun();
                }
            default:
                //System.out.println("Collided with "+object.getType());
                break;
        }
    }

    @Override
    public void draw() {
        super.draw();

        for(Collider c : collider.getCollidersInNeighboringZones()) {
            c.draw_self = true;
            c.draw();
            c.draw_self = false;
        }
    }

    /* Misc */

    protected Vector2 applyLateralMovement(Vector2 v, Vector2 ground_normal, double accel, double max_speed) {
        if(ground_normal == null) {
            ground_normal = up;
        }

        Vector2 axis = ground_normal.RHNormal().normalize();
        Vector2 vx = v.projection(axis);
        Vector2 new_v = v.copy();

        if(vx.abs() <= max_speed || vx.x >= 0) {
            if (InputManager.getDown(InputManager.K_LEFT)) {
                new_v = new_v.difference(axis.multiply(accel * Game.stepTimeSeconds()));
                direction_facing = Direction.LEFT;
            }
        }
        if(vx.abs() <= max_speed || vx.x <= 0) {
            if (InputManager.getDown(InputManager.K_RIGHT)) {
                new_v = new_v.sum(axis.multiply(accel * Game.stepTimeSeconds()));
                direction_facing = Direction.RIGHT;
            }
        }

        return new_v;
    }


    /* State machine */

    private class WalkState extends State {

        public String name = "Walk";
        Vector2 local_velocity; // Velocity relative to ground
        boolean skidding;
        boolean running;
        Sprite s;

        @Override
        String getState() {
            return name;
        }

        @Override
        void enter() {
            Collision ground = snapToGround();
            if(ground.collision_found) {
                velocity.y = 0;
                velocity = ground.normal_reject.RHNormal().normalize().multiply(velocity.x);
            }
            local_velocity = velocity;
        }

        @Override
        void handleCollision(Collision c, GroundType c_ground_type) {
            super.handleCollision(c, c_ground_type);
            local_velocity = local_velocity.difference(local_velocity.projection(c.normal_reject));
        }

        @Override
        void update() {
            // Ground check
            Collision ground = snapToGround();
            GroundType ground_type = checkGroundType(ground.normal_reject);

            if(ground_type != GroundType.NONE) {
                // Stick to slope corners
                if(local_velocity.x <= 0) {
                    local_velocity = ground.normal_reject.LHNormal().normalize().multiply(local_velocity.abs());
                }
                else {
                    local_velocity = ground.normal_reject.RHNormal().normalize().multiply(local_velocity.abs());
                }

                /*Vector2 off_axis_velocity = local_velocity.projection(ground.normal_reject);
                Vector2 new_local_velocity = local_velocity.difference(off_axis_velocity);
                new_local_velocity = new_local_velocity.normalize().multiply(local_velocity.abs());
                local_velocity = new_local_velocity;*/

                // Friction
                local_velocity = applyFriction(local_velocity, friction);

                // Left/right input and running check
                running = false;
                if(InputManager.getDown(InputManager.K_SPRINT)) {
                    local_velocity = applyLateralMovement(local_velocity, ground.normal_reject, run_accel, run_max_speed);
                }
                else {
                    local_velocity = applyLateralMovement(local_velocity, ground.normal_reject, walk_accel, walk_max_speed);
                }

                // Check for running
                running = local_velocity.abs() >= run_speed;

                // Check for skidding
                skidding = (InputManager.getDown(InputManager.K_RIGHT) && local_velocity.x < 0) ||
                        (InputManager.getDown(InputManager.K_LEFT) && local_velocity.x > 0);

                // Set global velocity
                velocity = local_velocity.sum(ground.collided_with.velocity);
            }
            else {
                setNextState(new FallState());
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                setNextState(new JumpState(high_jump_time, running, false));
            }

            // Update animation
            if (running) {
                s = run_sprite;
            } else {
                s = walk_sprite;
            }
            double speed = local_velocity.abs();
            s.setFrameTime((int)(run_max_speed - speed/2)/100);
            if(speed < walk_max_speed /20) {
                s.reset();
            }
            s.incrementFrame();

            // Slide
            /*if(InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_type == GroundType.FLAT) {
                    state_machine.changeState(State.DUCK);
                }
                else if(ground_type == GroundType.SLOPE) {
                    state_machine.changeState(State.SLIDE);
                }
            }*/
        }

        @Override
        void draw() {
            if(skidding) {
                drawSprite(skid_sprite);
            }
            else {
                drawSprite(s.getCurrentFrame());
            }
        }
    }

    private class JumpState extends State {

        public String name = "Jump";
        protected int timer;
        boolean running;
        boolean crouching;

        public JumpState(int timer, boolean running, boolean crouching) {
            this.timer = timer;
            this.running = running;
            this.crouching = crouching;
        }

        @Override
        String getState() {
            return name;
        }

        @Override
        void enter() {
            velocity.y = jump_speed;
        }

        @Override
        void update() {
            // Input checks
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                velocity = applyLateralMovement(velocity, null, walk_accel, run_max_speed);
            }
            else {
                velocity = applyLateralMovement(velocity, null, walk_accel, walk_max_speed);
            }

            timer--;
            if(timer == 0 || !InputManager.getDown(InputManager.K_JUMP) || velocity.y >= jump_speed/2) {
                state.setNextState(new FallState());
            }
        }

        @Override
        void draw() {
            drawSprite(jump_sprite);
        }
    }

    private class FallState extends State {
        public String name = "Fall";

        @Override
        String getState() {
            return name;
        }

        @Override
        void update() {
            velocity = applyGravity(velocity, gravity, max_fall_speed);

            // Input checks
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                velocity = applyLateralMovement(velocity, null, walk_accel, run_max_speed);
            }
            else {
                velocity = applyLateralMovement(velocity, null, walk_accel, walk_max_speed);
            }

            /*if(InputManager.getDown(InputManager.K_DOWN)) {
                if (ground_type == GroundType.SLOPE) {
                    state_machine.changeState(State.SLIDE);

                    Collision ground = snapToGround();
                    if(ground.collision_found) {
                        velocity.y = 0;
                        velocity = ground.normal_reject.RHNormal().normalize().multiply(velocity.x);
                    }
                }
                else if(ground_type == GroundType.FLAT) {
                    state_machine.changeState(State.DUCK);
                }
            }*/
        }

        @Override
        void handleCollision(Collision c, GroundType c_ground_type) {
            if(c_ground_type != GroundType.NONE) {
                state.setNextState(new WalkState());
            }
            else {
                super.handleCollision(c, c_ground_type);
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
    }

    /*private class RunFallState extends FallState {
        @Override
        void draw() {
            drawSprite(run_jump_sprite);
        }
    }

    private class SlideState extends DuckState {

        @Override
        State getState() {
            return State.SLIDE;
        }

        @Override
        void update() {
            super.update();

            if(InputManager.getDown(InputManager.K_DOWN) && velocity.x == 0) {
                state_machine.changeState(State.DUCK);
            }
        }

        @Override
        void draw() {
            drawSprite(slide_sprite);
        }
    }

    private class DuckState extends PlayerState {

        @Override
        State getState() {
            return State.DUCK;
        }

        @Override
        void update() {
            // Physics
            Collision ground = sweepForCollision(down);
            Vector2 v_parallel_to_ground;
            if(ground.collision_found) {
                v_parallel_to_ground = getVelocityParallelToGround(ground);
                applyFriction(v_parallel_to_ground, slide_friction);
                applyGravity(slide_gravity);
            }
            else {
                applyGravity(gravity);
            }

            GroundType ground_type = updatePositionWithCollisions(true);

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                state_machine.changeState(State.WALK);
            }

            // Jump
            else if(InputManager.getPressed(InputManager.K_JUMP) && ground_type != GroundType.NONE) {
                state_machine.changeState(State.JUMP);
            }

            // Slide
            else if(ground_type == GroundType.SLOPE && this.getState() != State.SLIDE) {
                state_machine.changeState(State.SLIDE);
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
    }*/
}
