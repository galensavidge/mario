package mario;

import engine.*;
import engine.collider.Intersection;
import engine.graphics.AnimatedSprite;
import engine.graphics.GameGraphics;
import engine.collider.Collider;
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
 * @version 6/6/2020
 */
public class Player extends PlatformingObject {

    public static final String type_name = "Player";


    /* State names */
    public static final String walkStateName = "Walk";
    public static final String jumpStateName = "Jump";
    public static final String fallStateName = "Fall";
    public static final String duckStateName = "Duck";
    public static final String slideStateName = "Slide";
    public static final String dieStateName = "Die";


    /* Physics constants */
    private static final double max_fall_speed = 900;
    private static final double fall_gravity = 2700;
    private static final double walk_gravity = 400;
    private static final double friction = 600;
    private static final double slide_gravity = 1500;
    private static final double slide_friction = 400;

    private static final double max_walk_speed = 300;
    private static final double walk_accel = 1000;
    private static final double run_speed = 550;
    private static final double max_run_speed = 650;
    private static final double run_accel = 1200;
    private static final double max_slide_speed = 700;

    private static final double high_jump_xspeed_threshold = 200;
    private static final double jump_speed = -700;
    private static final int high_jump_time = (int)(Mario.fps*0.4);
    private static final int low_jump_time = Mario.fps/4;
    private static final double duck_jump_xspeed = 75;

    private static final double die_pause_time = 1; // In seconds
    private static final double die_jump_speed = -600;
    private static final double die_gravity = 1000;
    private static final double die_max_fall_speed = 600;


    /* Colliders */
    private final Collider default_collider = Collider.newBox(this, Mario.getPixelSize(), Mario.getPixelSize()*8,
            Mario.getPixelSize()*14, Mario.getPixelSize()*16);
    private final Collider duck_collider = Collider.newBox(this, Mario.getPixelSize(), Mario.getPixelSize()*12,
            Mario.getPixelSize()*14, Mario.getPixelSize()*12);
    private static final double duck_collider_height_difference = Mario.getPixelSize()*4;


    /* Sprites */
    private static final String sprite_sub = "";
    private static final String[] walk_sprite_files = {Mario.sprite_path + sprite_sub + "mario-walk-1.png",
            Mario.sprite_path + sprite_sub + "mario-walk-2.png"};
    private final AnimatedSprite walk_sprite = new AnimatedSprite(walk_sprite_files, Mario.hitpause_suspend_tier);
    private static final String[] run_sprite_files = {Mario.sprite_path + sprite_sub + "mario-run-1.png",
            Mario.sprite_path + sprite_sub + "mario-run-2.png"};
    private final AnimatedSprite run_sprite = new AnimatedSprite(run_sprite_files, Mario.hitpause_suspend_tier);
    private static final Image skid_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-skid.png");
    private static final Image jump_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-jump.png");
    private static final Image fall_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-fall.png");
    private static final Image run_jump_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-run" +
            "-jump.png");
    private static final Image duck_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-duck.png");
    private static final Image slide_sprite = GameGraphics.getImage(Mario.sprite_path + sprite_sub + "mario-slide.png");
    private static final String[] die_sprite_files = {Mario.sprite_path + sprite_sub + "mario-die-1.png",
            Mario.sprite_path + sprite_sub + "mario-die-2.png"};
    private final AnimatedSprite die_sprite = new AnimatedSprite(die_sprite_files, Mario.hitpause_suspend_tier);


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
        this.direction_facing = Direction.RIGHT;
        this.state = new WalkState();
        this.state.enter();
    }


    /* Public methods */

    /**
     * Causes the player to bounce off of an enemy or other object.
     */
    public void bounce() {
        state.setNextState(new Player.JumpState(high_jump_time, false, false));
    }

    /**
     * Damages the player.
     */
    public void damage() {
        die();
    }

    /**
     * Kills the player.
     */
    public void die() {
        state.setNextState(new DieState());
    }


    /* Event handlers */

    @Override
    public void draw() {
        super.draw();
        /*for(Collider c : ColliderGrid.inNeighboringZones(this.getPosition())) {
            c.draw_self = true;
            c.draw();
            c.draw_self = false;
        }*/
    }


    /* State machine */

    private abstract class PlayerState extends State {

        @Override
        protected void handleCollisionEvent(PhysicsObject other) {
            switch(other.getTypeGroup()) {
                case Types.pickup_type_group:
                    ((Pickup)other).collect();
                    break;
                case Types.enemy_type_group:
                    ((Enemy)other).bounceEvent(Player.this);
                default:
                    if(other.hasTag(Types.damage_tag)) {
                        damage();
                    }
                    break;
            }
        }

        /* Helper functions */

        protected void useDefaultCollider() {
            if(collider != default_collider) {
                collider = default_collider;
                default_collider.enable();
                duck_collider.disable();
            }
        }

        protected void useDuckCollider() {
            if(collider != duck_collider) {
                collider = duck_collider;
                duck_collider.enable();
                default_collider.disable();
            }
        }

        protected boolean standUp() {
            useDefaultCollider();
            if(escapeSolids(new Vector2(0, duck_collider_height_difference))) {
                return true;
            }
            else {
                useDuckCollider();
                return false;
            }
        }

        protected void checkPitDeath() {
            if(getPosition().y - Mario.getGridScale() > World.getHeight()) {
                die();
            }
        }

        /**
         * Sets component of velocity parallel to ground to {@code velocity.x}. Sets the component of velocity normal
         * to the ground to zero.
         */
        protected void conserveHorizontalVelocity() {
            if(ground_found.type != GroundType.NONE) {
                velocity = ground_found.intersection.getNormal().RHNormal().multiply(velocity.x);
            }
        }

        protected Vector2 groundPhysics(double friction, double gravity, double max_fall_speed, double walk_accel,
                                        double run_accel, double max_walk_speed, double max_run_speed) {
            Vector2 local_velocity = velocity.copy();

            if(ground_found.type != GroundType.NONE) {
                if(last_ground.sameObject(ground_found)) {
                    local_velocity = velocity.difference(last_ground.velocity);
                }
                else {
                    local_velocity = velocity.difference(ground_found.velocity);
                }

                // Left/right input and running check
                local_velocity = applyLateralMovement(local_velocity, ground_found.intersection.getNormal(), walk_accel, run_accel,
                        max_walk_speed, max_run_speed);

                // Stick to slope corners
                if(local_velocity.x <= 0) {
                    local_velocity = ground_found.intersection.getNormal().LHNormal().normalize().multiply(local_velocity.abs());
                }
                else {
                    local_velocity = ground_found.intersection.getNormal().RHNormal().normalize().multiply(local_velocity.abs());
                }

                // Gravity
                if(local_velocity.abs() < max_fall_speed) {
                    Vector2 gravity_vector = new Vector2(0, gravity*Game.stepTimeSeconds());
                    Vector2 with_gravity =
                            local_velocity.sum(gravity_vector.normalComponent(ground_found.intersection.getNormal()));
                    if(with_gravity.abs() > max_fall_speed) {
                        local_velocity = local_velocity.normalize().multiply(max_fall_speed);
                    }
                    else {
                        local_velocity = with_gravity;
                    }
                }

                // Friction
                local_velocity = applyFriction(local_velocity, friction);

                // Set global velocity
                velocity = local_velocity.sum(ground_found.velocity);
            }
            return local_velocity;
        }

        protected Vector2 applyLateralMovement(Vector2 v, Vector2 ground_normal, double no_sprint_accel,
                                               double with_sprint_accel, double no_sprint_max_speed,
                                               double with_sprint_max_speed) {
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
            if(Math.abs(horizontal_speed) >= high_jump_xspeed_threshold && InputManager.getDown(InputManager.K_SPRINT)) {
                return high_jump_time;
            }
            else {
                return low_jump_time;
            }
        }
    }

    private class WalkState extends PlayerState {

        boolean skidding;
        boolean running;
        AnimatedSprite s = walk_sprite;

        public WalkState() {
            stick_to_ground = true;
        }

        @Override
        public String getState() {
            return walkStateName;
        }

        @Override
        public void enter() {
            useDefaultCollider();
            conserveHorizontalVelocity();
        }

        @Override
        protected void handlePhysicsCollisionEvent(GroundCollision g) {
            if(g.type == GroundType.NONE) {
                super.handlePhysicsCollisionEvent(g);
            }
        }

        @Override
        public void update() {
            // Physics
            Vector2 local_velocity = groundPhysics(friction, walk_gravity, max_run_speed, walk_accel, run_accel,
                    max_walk_speed, max_run_speed);

            if(ground_found.type == GroundType.NONE) {
                setNextState(new FallState(running, false));
                velocity = applyGravity(velocity, fall_gravity, max_fall_speed);
            }

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

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                setNextState(new JumpState(jumpTime(speed), running, false));
            }

            // Slide
            if(InputManager.getDown(InputManager.K_DOWN)) {
                if(ground_found.type == GroundType.FLAT) {
                    setNextState(new DuckState());
                }
                else if(ground_found.type == GroundType.SLOPE) {
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

            try {
                Collider c = ground_found.intersection.collided_with.collider;
                c.draw_self = true;
                c.draw();
                c.draw_self = false;
            }
            catch(NullPointerException ignored) {
            }
        }
    }

    private class JumpState extends PlayerState {

        protected int timer;
        boolean running, crouching;

        public JumpState(int timer, boolean running, boolean crouching) {
            this.timer = timer;
            this.running = running;
            this.crouching = crouching;
        }

        @Override
        public String getState() {
            return jumpStateName;
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
                if(standUp()) {
                    crouching = false;
                }
            }

            timer--;
            if(timer == 0 || !InputManager.getDown(InputManager.K_JUMP) || velocity.y >= jump_speed/2) {
                setNextState(new FallState(running, crouching));
            }
        }

        @Override
        protected void handlePhysicsCollisionEvent(GroundCollision g) {
            if(g.intersection.getNormal().x == 0) {
                if(slideAroundCorners(g.intersection, 4*Mario.getPixelSize())) {
                    return;
                }
            }
            super.handlePhysicsCollisionEvent(g);
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

        boolean running, crouching;

        public FallState(boolean running, boolean crouching) {
            this.running = running;
            this.crouching = crouching;
        }

        @Override
        public String getState() {
            return fallStateName;
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
            velocity = applyGravity(velocity, fall_gravity, max_fall_speed);
            velocity = applyLateralMovement(velocity, null, walk_accel, walk_accel, max_walk_speed, max_run_speed);

            if(crouching && !InputManager.getDown(InputManager.K_DOWN)) {
                if(standUp()) {
                    crouching = false;
                }
            }

            checkPitDeath();
        }

        @Override
        public void handlePhysicsCollisionEvent(GroundCollision g) {
            if(g.type != GroundType.NONE) {
                if(!InputManager.getDown(InputManager.K_DOWN)) {
                    setNextState(new WalkState());
                }
                else {
                    if(g.type == GroundType.FLAT) {
                        setNextState(new DuckState());
                    }
                    else {
                        setNextState(new SlideState());
                    }
                }
            }
            else {
                super.handlePhysicsCollisionEvent(g);
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

        public DuckState() {
            stick_to_ground = true;
        }

        @Override
        public String getState() {
            return duckStateName;
        }

        @Override
        public void enter() {
            useDuckCollider();
            conserveHorizontalVelocity();
        }

        @Override
        protected void handlePhysicsCollisionEvent(GroundCollision g) {
            if(g.type == GroundType.NONE) {
                super.handlePhysicsCollisionEvent(g);
            }
        }

        protected Vector2 slidePhysics() {
            Vector2 local_velocity = groundPhysics(slide_friction, slide_gravity, max_slide_speed, 0,
                    0, max_slide_speed, max_slide_speed);

            if(ground_found.type == GroundType.NONE) {
                setNextState(new FallState(false, true));
            }

            return local_velocity;
        }

        @Override
        public void update() {
            Vector2 local_velocity = slidePhysics();

            if(InputManager.getDown(InputManager.K_LEFT)) {
                direction_facing = Direction.LEFT;
            }
            else if(InputManager.getDown(InputManager.K_RIGHT)) {
                direction_facing = Direction.RIGHT;
            }

            if(ground_found.type == GroundType.SLOPE) {
                setNextState(new SlideState());
            }

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                if(standUp()) {
                    if(ground_found.type != GroundType.NONE) {
                        setNextState(new WalkState());
                    }
                    else {
                        setNextState(new FallState(false, false));
                    }
                }
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP)) {
                if(Math.abs(local_velocity.x) < duck_jump_xspeed) {
                    if(InputManager.getDown(InputManager.K_RIGHT)) {
                        velocity.x += duck_jump_xspeed;
                    }
                    else if(InputManager.getDown(InputManager.K_LEFT)) {
                        velocity.x -= duck_jump_xspeed;
                    }
                }
                if(ground_found.type == GroundType.FLAT) {
                    setNextState(new JumpState(jumpTime(local_velocity.abs()), false, true));
                }
                else if(ground_found.type == GroundType.SLOPE) {
                    if(standUp()) {
                        setNextState(new JumpState(jumpTime(local_velocity.abs()), false, false));
                    }
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

        @Override
        public String getState() {
            return slideStateName;
        }

        @Override
        public void update() {
            slidePhysics();

            // Switch to duck when stopped
            if(InputManager.getDown(InputManager.K_DOWN) && velocity.x == 0 && ground_found.type == GroundType.FLAT) {
                setNextState(new DuckState());
            }

            // Stand up
            if(!InputManager.getDown(InputManager.K_DOWN)) {
                if(standUp()) {
                    if(ground_found.type != GroundType.NONE) {
                        setNextState(new WalkState());
                    }
                    else {
                        setNextState(new FallState(false, false));
                    }
                }
            }

            // Jump
            if(InputManager.getPressed(InputManager.K_JUMP) && ground_found.type != GroundType.NONE) {
                if(standUp()) {
                    setNextState(new JumpState(jumpTime(velocity.x), false, false));
                }
            }

            checkPitDeath();
        }

        @Override
        public void draw() {
            drawSprite(slide_sprite);
        }
    }

    private class DieState extends State {

        private int timer;

        @Override
        public String getState() {
            return dieStateName;
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
            }
        }

        @Override
        public void draw() {
            drawSprite(die_sprite.getCurrentFrame());
        }
    }
}
