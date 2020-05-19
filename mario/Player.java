package mario;

import engine.*;
import engine.graphics.AnimatedSprite;
import engine.graphics.GameGraphics;
import engine.objects.Collider;
import engine.objects.Collider.Collision;
import engine.objects.PhysicsObject;
import engine.util.Vector2;
import mario.enemies.Enemy;
import mario.objects.Pickup;
import mario.objects.Types;

import java.awt.*;
import java.util.HashMap;

/**
 * The physical object that the player controls.
 *
 * @author Galen Savidge
 * @version 5/18/2020
 */
public class Player extends PlatformingObject {

    /* Class constants */

    public static final String type_name = "Player";

    private static final double max_fall_speed = 900;
    private static final double gravity = 2700;
    private static final double friction = 600;
    private static final double slide_gravity = 1500;
    private static final double slide_friction = 400;

    private static final double max_walk_speed = 300;
    private static final double walk_accel = 900;
    private static final double run_speed = 550;
    private static final double max_run_speed = 650;
    private static final double run_accel = 1200;
    private static final double max_slide_speed = 700;

    private static final double high_jump_xspeed_threshold = 200;
    private static final double jump_speed = -700;
    private static final int high_jump_time = (int)(Mario.fps*0.4);
    private static final int low_jump_time = Mario.fps/4;

    private static final double die_pause_time = 1; // In seconds
    private static final double die_jump_speed = -600;
    private static final double die_gravity = 1000;
    private static final double die_max_fall_speed = 600;

    private static final Vector2 short_down = new Vector2(0, 2*Collider.reject_separation);

    private final Collider default_collider = Collider.newBox(this, 0, Mario.getGridScale()*0.5, Mario.getGridScale()
            , Mario.getGridScale());
    private final Collider duck_collider = Collider.newBox(this, 0, Mario.getGridScale()*0.75, Mario.getGridScale(),
            Mario.getGridScale()*0.75);
    private static final double duck_collider_height_difference = Mario.getGridScale()*0.25;

    private static final String sprite_sub = "";
    private static final String[] walk_sprite_files = {Mario.sprite_path + sprite_sub + "mario-walk-1.png",
            Mario.sprite_path + sprite_sub + "mario-walk-2.png"};
    private static final AnimatedSprite walk_sprite = new AnimatedSprite(walk_sprite_files);
    private static final String[] run_sprite_files = {Mario.sprite_path + sprite_sub + "mario-run-1.png",
            Mario.sprite_path + sprite_sub + "mario-run-2.png"};
    private static final AnimatedSprite run_sprite = new AnimatedSprite(run_sprite_files);
    private static final Image skid_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-skid.png");
    private static final Image jump_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-jump.png");
    private static final Image fall_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-fall.png");
    private static final Image run_jump_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-run" +
            "-jump.png");
    private static final Image duck_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-duck.png");
    private static final Image slide_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-slide.png");
    private static final String[] die_sprite_files = {Mario.sprite_path + sprite_sub + "mario-die-1.png",
            Mario.sprite_path + sprite_sub + "mario-die-2.png"};
    private static final AnimatedSprite die_sprite = new AnimatedSprite(die_sprite_files);


    /* Constructors */

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
        default_collider.active_check = true;
        duck_collider.active_check = true;
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
        PhysicsObject raycast_obj = getObjectInDirection(new Vector2(0, 10));
        if(raycast_obj != null) {
            raycast_obj.collider.draw_self = true;
            raycast_obj.collider.draw();
            raycast_obj.collider.draw_self = false;
        }
        //collider.draw_self = true;
        /*for(Collider c : collider.getCollidersInNeighboringZones()) {
            c.draw_self = true;
            c.draw();
            c.draw_self = false;
        }*/
    }


    /* State machine */

    private abstract class PlayerState extends State {

        @Override
        protected void handleIntersectionEvent(Collision c) {
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

        /* Helper functions */

        protected void useDefaultCollider() {
            if(collider != default_collider) {
                collider = default_collider;
                default_collider.enable();
                duck_collider.disable();
                position.y += duck_collider_height_difference;
                moveAndCollide(new Vector2(0, -duck_collider_height_difference));
            }
        }

        protected void useDuckCollider() {
            if(collider != duck_collider) {
                collider = duck_collider;
                duck_collider.enable();
                default_collider.disable();
            }
        }

        protected void checkPitDeath() {
            if(position.y - Mario.getGridScale() > World.getHeight()) {
                die();
            }
        }

        protected GroundType groundPhysics(Vector2 local_velocity, double friction, double gravity,
                                           double max_fall_speed,
                                           double walk_accel, double run_accel, double max_walk_speed,
                                           double max_run_speed) {
            // Ground check
            Collision ground = snapToGround();
            GroundType ground_type = checkGroundType(ground.normal_reject);

            if(ground_type != GroundType.NONE) {
                // Left/right input and running check
                Vector2 new_lv = applyLateralMovement(local_velocity, ground.normal_reject, walk_accel, run_accel,
                        max_walk_speed, max_run_speed);

                // Stick to slope corners
                if(new_lv.x <= 0) {
                    new_lv = ground.normal_reject.LHNormal().normalize().multiply(new_lv.abs());
                }
                else {
                    new_lv = ground.normal_reject.RHNormal().normalize().multiply(new_lv.abs());
                }

                // Friction and gravity
                new_lv = applyFriction(new_lv, friction);
                new_lv = applyGravity(new_lv, gravity, max_fall_speed);

                // Set global velocity
                velocity = new_lv.sum(ground.collided_with.velocity);

                // Set local_velocity to new_lv
                new_lv.copyTo(local_velocity);
            }
            return ground_type;
        }

        protected Vector2 applyLateralMovement(Vector2 v, Vector2 ground_normal, double no_sprint_accel,
                                               double with_sprint_accel,
                                               double no_sprint_max_speed, double with_sprint_max_speed) {
            if(ground_normal == null) {
                ground_normal = up;
            }

            double accel = no_sprint_accel;
            double max_speed = no_sprint_max_speed;
            if(InputManager.getDown(InputManager.K_SPRINT)) {
                accel = with_sprint_accel;
                max_speed = with_sprint_max_speed;
            }


            Vector2 axis = ground_normal.RHNormal().normalize();
            Vector2 vx = v.projection(axis);
            double vx_abs = vx.abs();
            Vector2 new_vx = vx.copy();

            if(InputManager.getDown(InputManager.K_LEFT)) {
                if(vx_abs < max_speed || vx.x > 0) {
                    new_vx = new_vx.difference(axis.multiply(accel*Game.stepTimeSeconds()));
                    if(vx_abs <= max_speed && new_vx.abs() > max_speed) {
                        new_vx = axis.multiply(-max_speed);
                    }
                    direction_facing = Direction.LEFT;
                }
            }
            else if(InputManager.getDown(InputManager.K_RIGHT)) {
                if(vx_abs < max_speed || vx.x < 0) {
                    new_vx = new_vx.sum(axis.multiply(accel*Game.stepTimeSeconds()));
                    if(vx_abs <= max_speed && new_vx.abs() > max_speed) {
                        new_vx = axis.multiply(max_speed);
                    }
                    direction_facing = Direction.RIGHT;
                }
            }

            return new_vx.sum(v.projection(ground_normal));
        }

        protected int jumpTime(double horizontal_speed) {
            if(Math.abs(horizontal_speed) >= high_jump_xspeed_threshold) {
                return high_jump_time;
            }
            else {
                return low_jump_time;
            }
        }
    }

    private class WalkState extends PlayerState {

        public String name = "Walk";
        Vector2 local_velocity; // Velocity relative to ground
        boolean skidding;
        boolean running;
        AnimatedSprite s = walk_sprite;

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            useDefaultCollider();

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
        protected void handleCollisionEvent(Collision collision, GroundType c_ground_type) {
            if(c_ground_type == GroundType.NONE) {
                super.handleCollisionEvent(collision, c_ground_type);
                local_velocity = inelasticCollision(local_velocity, collision);
            }
        }

        @Override
        public void update() {
            GroundType ground_type = groundPhysics(local_velocity, friction, 0, 0, walk_accel, run_accel,
                    max_walk_speed, max_run_speed);


            // Check for running
            running = local_velocity.abs() >= run_speed;

            // Check for skidding
            if(running && ((InputManager.getDown(InputManager.K_RIGHT) && local_velocity.x < 0) ||
                    (InputManager.getDown(InputManager.K_LEFT) && local_velocity.x > 0))) {
                skidding = true;
            }

            double speed = local_velocity.abs();
            if(skidding && Math.abs(speed) < high_jump_xspeed_threshold) {
                skidding = false;
            }

            if(ground_type == GroundType.NONE) {
                setNextState(new FallState(running, false));
            }


            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                setNextState(new JumpState(jumpTime(speed), running, false));
            }

            // Slide
            if(InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_type == GroundType.FLAT) {
                    setNextState(new DuckState());
                }
                else if(ground_type == GroundType.SLOPE) {
                    setNextState(new SlideState());
                }
            }

            // Update animation
            if(running) {
                s = run_sprite;
            }
            else {
                s = walk_sprite;
            }
            s.setFrameTime((int)(max_run_speed - speed/2)/100);
            if(speed < max_walk_speed/20) {
                s.reset();
            }
            s.incrementFrame();

            checkPitDeath();
        }

        @Override
        public void draw() {
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
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            velocity.y = jump_speed;

            if(crouching) {
                useDuckCollider();
            }
            else {
                useDefaultCollider();
            }
        }

        @Override
        public void update() {
            velocity = applyLateralMovement(velocity, null, walk_accel, run_accel, max_walk_speed, max_run_speed);

            if(crouching && !InputManager.getDown(InputManager.K_DOWN)) {
                crouching = false;
                useDefaultCollider();
            }

            timer--;
            if(timer == 0 || !InputManager.getDown(InputManager.K_JUMP) || velocity.y >= jump_speed/2) {
                setNextState(new FallState(running, crouching));
            }
        }

        @Override
        public void draw() {
            if(running) {
                drawSprite(run_jump_sprite);
            }
            else if(crouching) {
                drawSprite(duck_sprite);
            }
            else {
                drawSprite(jump_sprite);
            }
        }
    }

    private class FallState extends PlayerState {
        public String name = "Fall";
        boolean running, crouching;

        public FallState(boolean running, boolean crouching) {
            this.running = running;
            this.crouching = crouching;
        }

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            if(crouching) {
                useDuckCollider();
            }
            else {
                useDefaultCollider();
            }
        }

        @Override
        public void update() {
            velocity = applyGravity(velocity, gravity, max_fall_speed);
            velocity = applyLateralMovement(velocity, null, walk_accel, walk_accel, max_walk_speed, max_run_speed);

            if(crouching && !InputManager.getDown(InputManager.K_DOWN)) {
                crouching = false;
                useDefaultCollider();
            }

            checkPitDeath();
        }

        @Override
        public void handleCollisionEvent(Collision collision, GroundType c_ground_type) {
            if(c_ground_type != GroundType.NONE) {
                if(!InputManager.getDown(InputManager.K_DOWN)) {
                    setNextState(new WalkState());
                }
                else {
                    super.handleCollisionEvent(collision, c_ground_type);
                    if(c_ground_type == GroundType.FLAT) {
                        setNextState(new DuckState());
                    }
                    else {
                        setNextState(new SlideState());
                    }
                }
            }
            else {
                super.handleCollisionEvent(collision, c_ground_type);
            }
        }

        @Override
        public void draw() {
            if(running) {
                drawSprite(run_jump_sprite);
            }
            else if(crouching) {
                drawSprite(duck_sprite);
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
        private Vector2 local_velocity;

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            useDuckCollider();
            Collision ground = snapToGround();
            if(ground.collision_found) {
                local_velocity = velocity.difference(ground.collided_with.velocity);
            }
            else {
                local_velocity = velocity.copy();
            }
        }

        @Override
        protected void handleCollisionEvent(Collision collision, GroundType c_ground_type) {
            if(c_ground_type == GroundType.NONE) {
                super.handleCollisionEvent(collision, c_ground_type);
                local_velocity = inelasticCollision(local_velocity, collision);
            }
        }

        protected GroundType slidePhysics() {
            GroundType ground_type = groundPhysics(local_velocity, slide_friction, slide_gravity, max_slide_speed, 0,
                    0, max_slide_speed, max_slide_speed);

            if(ground_type == GroundType.NONE) {
                setNextState(new FallState(false, true));
            }

            return ground_type;
        }

        @Override
        public void update() {
            GroundType ground_type = slidePhysics();

            if(InputManager.getDown(InputManager.K_LEFT)) {
                direction_facing = Direction.LEFT;
            }
            else if(InputManager.getDown(InputManager.K_RIGHT)) {
                direction_facing = Direction.RIGHT;
            }

            if(ground_type == GroundType.SLOPE) {
                setNextState(new SlideState());
            }

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_type != GroundType.NONE) {
                    setNextState(new WalkState());
                }
                else {
                    setNextState(new FallState(false, false));
                }
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                if(ground_type == GroundType.FLAT) {
                    setNextState(new JumpState(jumpTime(local_velocity.abs()), false, true));
                }
                else if(ground_type == GroundType.SLOPE) {
                    setNextState(new JumpState(jumpTime(local_velocity.abs()), false, false));
                }
            }

            checkPitDeath();
        }

        @Override
        public void draw() {
            drawSprite(duck_sprite);
        }
    }

    private class SlideState extends DuckState {
        public String name = "Slide";

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void update() {
            GroundType ground_type = slidePhysics();

            // Switch to duck when stopped
            if(InputManager.getDown(InputManager.K_DOWN) && velocity.x == 0) {
                setNextState(new DuckState());
            }

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_type != GroundType.NONE) {
                    setNextState(new WalkState());
                }
                else {
                    setNextState(new FallState(false, false));
                }
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP) && ground_type != GroundType.NONE) {
                setNextState(new JumpState(jumpTime(velocity.x), false, false));
            }

            checkPitDeath();
        }

        @Override
        public void draw() {
            drawSprite(slide_sprite);
        }
    }

    private class DieState extends State {
        public String name = "Die";

        private int timer;

        @Override
        public String getState() {
            return name;
        }

        @Override
        public void enter() {
            Game.setSuspendTier(Mario.hitpause_suspend_tier);
            GameController.releaseCamera();
            collider.disable();
            velocity = Vector2.zero();
            timer = (int)(Mario.fps*die_pause_time);
            die_sprite.setFrameTime(Mario.fps/6);
        }

        @Override
        public void update() {
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
        public void draw() {
            drawSprite(die_sprite.getCurrentFrame());
        }
    }
}
