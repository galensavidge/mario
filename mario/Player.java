package mario;

import engine.*;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import engine.util.Vector2;
import mario.objects.Pickup;
import mario.objects.Types;

import java.awt.*;
import java.util.HashMap;

/**
 * The physical object that the player controls.
 *
 * @author Galen Savidge
 * @version 5/16/2020
 */
public class Player extends PlatformingObject {

    /* Class constants */

    public static final String type_name = "Player";

    private static final double max_fall_speed = 700;
    private static final double gravity = 2500;
    private static final double friction = 600;
    private static final double slide_gravity = 1500;
    private static final double slide_friction = 400;

    private static final double max_walk_speed = 300;
    private static final double walk_accel = 900;
    private static final double run_speed = 625;
    private static final double max_run_speed = 650;
    private static final double run_accel = 1200;
    private static final double max_slide_speed = 700;

    private static final double high_jump_xspeed_threshold = 200;
    private static final double jump_speed = -700;
    private static final int high_jump_time = Mario.fps/3;
    private static final int low_jump_time = Mario.fps/4;

    private static final double die_pause_time = 1; // In seconds
    private static final double die_jump_speed = -200;
    private static final double die_gravity = 500;
    private static final double die_max_fall_speed = 400;

    private static final Vector2 short_down = new Vector2(0, 2*Collider.reject_separation);

    private static final String sprite_sub = "";
    private static final String[] walk_sprite_files = {Mario.sprite_path+sprite_sub+"mario-walk-1.png", Mario.sprite_path+sprite_sub+"mario-walk-2.png"};
    private static final Sprite walk_sprite = new Sprite(walk_sprite_files);
    private static final String[] run_sprite_files = {Mario.sprite_path+sprite_sub+"mario-run-1.png", Mario.sprite_path+sprite_sub+"mario-run-2.png"};
    private static final Sprite run_sprite = new Sprite(run_sprite_files);
    private static final Image skid_sprite = GameGraphics.getImage(Mario.sprite_path+sprite_sub+"mario-skid.png");
    private static final Image jump_sprite = GameGraphics.getImage(Mario.sprite_path+sprite_sub+"mario-jump.png");
    private static final Image fall_sprite = GameGraphics.getImage(Mario.sprite_path+sprite_sub+"mario-fall.png");
    private static final Image run_jump_sprite = GameGraphics.getImage(Mario.sprite_path+sprite_sub+"mario-run-jump.png");
    private static final Image duck_sprite = GameGraphics.getImage(Mario.sprite_path+sprite_sub+"mario-duck.png");
    private static final Image slide_sprite = GameGraphics.getImage(Mario.sprite_path+sprite_sub+"mario-slide.png");
    private static final String[] die_sprite_files = {Mario.sprite_path+sprite_sub+"mario-die-1.png", Mario.sprite_path+sprite_sub+"mario-die-2.png"};
    private static final Sprite die_sprite = new Sprite(die_sprite_files);

    /* Instance variables */

    public Player(double x, double y) {
        super(Mario.player_priority, Mario.player_layer, x, y);
        init();
    }

    public Player(HashMap<String, Object> args) {
        super(Mario.player_priority, Mario.player_layer, args);
        init();
    }

    private void init() {
        this.suspend_tier = Mario.hitpause_suspend_tier;
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


    /* Public methods */

    public void bounce() {
        state.setNextState(new Player.JumpState(high_jump_time, false, false));
    }

    public void damage() {
        die();
    }

    public void die() {
        state.setNextState(new DieState());
    }


    /* Event handlers */

    @Override
    public void draw() {
        super.draw();
        /*collider.draw_self = true;
        for(Collider c : collider.getCollidersInNeighboringZones()) {
            c.draw_self = true;
            c.draw();
            c.draw_self = false;
        }*/
    }


    /* Helper functions */

    protected Vector2 applyLateralMovement(Vector2 v, Vector2 ground_normal, double accel, double max_speed) {
        if(ground_normal == null) {
            ground_normal = up;
        }

        Vector2 axis = ground_normal.RHNormal().normalize();
        Vector2 vx = v.projection(axis);
        double vx_abs = vx.abs();
        Vector2 new_vx = vx.copy();

        if (InputManager.getDown(InputManager.K_LEFT)) {
            if(vx_abs < max_speed || vx.x > 0) {
                new_vx = new_vx.difference(axis.multiply(accel * Game.stepTimeSeconds()));
                if (vx_abs <= max_speed && new_vx.abs() > max_speed) {
                    new_vx = axis.multiply(-max_speed);
                }
                direction_facing = Direction.LEFT;
            }
        }
        else if (InputManager.getDown(InputManager.K_RIGHT)) {
            if(vx_abs < max_speed || vx.x < 0) {
                new_vx = new_vx.sum(axis.multiply(accel * Game.stepTimeSeconds()));
                if (vx_abs <= max_speed && new_vx.abs() > max_speed) {
                    new_vx = axis.multiply(max_speed);
                }
                direction_facing = Direction.RIGHT;
            }
        }

        return new_vx.sum(v.projection(ground_normal));
    }


    /* State machine */

    private abstract class PlayerState extends State {

        @Override
        void handleIntersectionEvent(Collision c) {
            switch(c.collided_with.getTypeGroup()) {
                case Types.pickup_type_group:
                    ((Pickup)c.collided_with).collect();
                    break;
                case Types.enemy_type_group:
                    ((Enemy)c.collided_with).bounceEvent(Player.this);
                default:
                    //System.out.println("Collided with "+object.getType());
                    break;
            }
        }

        void dieInPits() {
            if(position.y - Mario.getGridScale() > World.getHeight()) {
                die();
            }
        }
    }

    private class WalkState extends PlayerState {

        public String name = "Walk";
        Vector2 local_velocity; // Velocity relative to ground
        boolean skidding;
        boolean running;
        Sprite s = walk_sprite;

        @Override
        String getState() {
            return name;
        }

        @Override
        void enter() {
            // Snap down to be touching ground
            Collision ground = snapToGround();

            // Conserve horizontal velocity and change its direction to be parallel with the ground
            if(ground.collision_found) {
                velocity.y = 0;
                velocity = ground.normal_reject.RHNormal().normalize().multiply(velocity.x);
                local_velocity = velocity.difference(ground.collided_with.velocity);
            }
            else {
                local_velocity = velocity.copy();
            }
        }

        @Override
        void handleCollisionEvent(Collision c, GroundType c_ground_type) {
            if(c_ground_type == GroundType.NONE) {
                super.handleCollisionEvent(c, c_ground_type);
                local_velocity = local_velocity.difference(local_velocity.projection(c.normal_reject));
            }
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

                // Friction
                local_velocity = applyFriction(local_velocity, friction);

                // Left/right input and running check
                running = false;
                if(InputManager.getDown(InputManager.K_SPRINT)) {
                    local_velocity = applyLateralMovement(local_velocity, ground.normal_reject, run_accel, max_run_speed);
                }
                else {
                    local_velocity = applyLateralMovement(local_velocity, ground.normal_reject, walk_accel, max_walk_speed);
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
                setNextState(new FallState(running));
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                setNextState(new JumpState(high_jump_time, running, false));
            }

            // Slide
            if(InputManager.getDown(InputManager.K_DOWN)) {
                setNextState(new DuckState());
            }

            // Update animation
            if (running) {
                s = run_sprite;
            } else {
                s = walk_sprite;
            }
            double speed = local_velocity.abs();
            s.setFrameTime((int)(max_run_speed - speed/2)/100);
            if(speed < max_walk_speed /20) {
                s.reset();
            }
            s.incrementFrame();

            dieInPits();
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

    private class JumpState extends PlayerState {

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
                velocity = applyLateralMovement(velocity, null, walk_accel, max_run_speed);
            }
            else {
                velocity = applyLateralMovement(velocity, null, walk_accel, max_walk_speed);
            }

            timer--;
            if(timer == 0 || !InputManager.getDown(InputManager.K_JUMP) || velocity.y >= jump_speed/2) {
                state.setNextState(new FallState(running));
            }
        }

        @Override
        void draw() {
            if(running) {
                drawSprite(run_jump_sprite);
            }
            else {
                drawSprite(jump_sprite);
            }
        }
    }

    private class FallState extends PlayerState {
        public String name = "Fall";
        boolean running;

        public FallState(boolean running) {
            this.running = running;
        }

        @Override
        String getState() {
            return name;
        }

        @Override
        void update() {
            velocity = applyGravity(velocity, gravity, max_fall_speed);

            // Input checks
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                velocity = applyLateralMovement(velocity, null, walk_accel, max_run_speed);
            }
            else {
                velocity = applyLateralMovement(velocity, null, walk_accel, max_walk_speed);
            }

            dieInPits();
        }

        @Override
        void handleCollisionEvent(Collision c, GroundType c_ground_type) {
            if(c_ground_type != GroundType.NONE) {
                state.setNextState(new WalkState());
            }
            else {
                super.handleCollisionEvent(c, c_ground_type);
            }
        }

        @Override
        void draw() {
            if(running) {
                drawSprite(run_jump_sprite);
            }
            else if(velocity.y > 0) {
                drawSprite(fall_sprite);
            }
            else {
                drawSprite(jump_sprite);
            }
        }
    }

    private class DuckState extends PlayerState {
        public String name = "Duck";

        @Override
        String getState() {
            return name;
        }

        @Override
        void update() {
            Collision ground = sweepForCollision(short_down);
            GroundType ground_type = checkGroundType(ground.normal_reject);
            if(ground_type != GroundType.NONE) {
                Vector2 local_velocity = velocity.difference(ground.collided_with.velocity);

                // Friction
                local_velocity = applyFriction(local_velocity, friction);

                // Gravity
                local_velocity = applyGravity(local_velocity, slide_gravity, max_fall_speed);

                // Cap speed
                if(local_velocity.abs() > max_slide_speed) {
                    local_velocity = local_velocity.normalize().multiply(max_slide_speed);
                }

                // Set global velocity
                velocity = local_velocity.sum(ground.collided_with.velocity);
            }
            else {
                velocity = applyGravity(velocity, gravity, max_fall_speed);
            }

            /*if(ground_type == GroundType.SLOPE) {
                // Transition to slide
            }*/

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_type != GroundType.NONE) {
                    setNextState(new WalkState());
                }
                else {
                    setNextState(new FallState(false));
                }
            }

            dieInPits();
        }

        @Override
        void draw() {
            drawSprite(duck_sprite);
        }
    }

    private class DieState extends State {
        public String name = "Die";

        private int timer;

        @Override
        String getState() {
            return name;
        }

        @Override
        void enter() {
            Game.setSuspendTier(Mario.hitpause_suspend_tier);
            GameController.releaseCamera();
            collider.disable();
            velocity = Vector2.zero();
            timer = (int)(Mario.fps*die_pause_time);
            die_sprite.setFrameTime(Mario.fps/6);
        }

        @Override
        void update() {
            timer--;
            if(timer == 0) {
                velocity.y = die_jump_speed;
            }
            if(timer <= 0) {
                velocity = applyGravity(velocity, die_gravity, die_max_fall_speed);
                if(!isOnScreen(Mario.getGridScale(), Mario.getGridScale()*1.5, 0)) {
                    GameController.respawnPlayer();
                }
                die_sprite.incrementFrame();
            }
        }

        @Override
        void draw() {
            drawSprite(die_sprite.getCurrentFrame());
        }
    }
}
