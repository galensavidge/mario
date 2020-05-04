package mario;

import engine.*;
import engine.objects.*;
import engine.util.Vector2;
import engine.objects.Collider.Collision;

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

    private static final Vector2 gravity = new Vector2(0,3000); // Pixels/s^2
    private static final double max_fall_speed = 700;
    private static final double friction = 400;

    private static final double walk_xspeed = 350;
    private static final double p_speed = 500;
    private static final double walk_accel = 800;
    private static final double run_accel = 1200;

    private static final double air_accel = 600;
    private static final double jump_speed = -700;
    private static final int max_jump_time = Mario.fps/2;

    private static final Vector2 up = new Vector2(0, -1);
    private static final Vector2 down = new Vector2(0, Mario.getGridScale()/2.0);

    private static final String jump_sprite_file = "./sprites/mario-jump.png";
    private static final Image jump_sprite = GameGraphics.getImage(jump_sprite_file);
    private static final String fall_sprite_file = "./sprites/mario-fall.png";
    private static final Image fall_sprite = GameGraphics.getImage(fall_sprite_file);

    /* Instance variables */

    private final PlayerStateMachine state_machine;

    private Collision ground;
    private Direction direction_facing = Direction.RIGHT;

    public Player(double x, double y) {
        super(10, 10, x, y);
        collider = Collider.newBox(this,0,
                Mario.getGridScale()/2.0, Mario.getGridScale(), Mario.getGridScale());
        collider.active_check = true;
        this.type = type_name;
        state_machine = new PlayerStateMachine(States.FALL);
    }

    @Override
    protected boolean collideWith(PhysicsObject o) {
        if(o.solid) {
            return true;
        }
        else if(o.getType().equals(CloudBlock.type_name) &&
                position.y + height - Collider.edge_separation < o.position.y && velocity.y >= 0) {
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
                System.out.println("Coins +1!");
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

    private enum States {
        WALK,
        RUN,
        JUMP,
        FALL
    }

    private class PlayerStateMachine {
        Dictionary<States, PlayerState> state_objects = new Hashtable<>();
        PlayerState state;

        PlayerStateMachine(States state) {
            state_objects.put(States.WALK, new WalkState());
            state_objects.put(States.JUMP, new JumpState());
            state_objects.put(States.FALL, new FallState());

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

        protected Vector2 getVelocityParallelToGround() {
            return velocity.normalComponent(ground.normal_reject);
        }

        protected void gravity() {
            velocity = velocity.sum(gravity.multiply(Game.stepTimeSeconds()));
        }

        protected boolean isGround(Vector2 normal) {
            // Check if normal is pointing up and slope is less than about 46 degrees
            return normal.y < 0 && Math.abs(normal.y/normal.x) >= 0.95;
        }

        protected Collision getGroundNormal() {
            Collision collision = sweepForCollision(down);
            if(collision.collision_found) {
                // Check that normal points up and is within 45 degrees of vertical
                if(isGround(collision.normal_reject)) {
                    // Snap to ground
                    position = position.sum(collision.to_contact);
                }
            }
            return collision;
        }

        protected void friction() {
            if(!ground.collision_found) {
                return;
            }

            double friction_delta = friction*Game.stepTimeSeconds();
            Vector2 vx = getVelocityParallelToGround();
            if(vx.abs() > friction_delta) {
                velocity = velocity.difference(velocity.normalComponent(ground.normal_reject).normalize()
                        .multiply(friction_delta));
            }
            else {
                velocity = velocity.difference(velocity.normalComponent(ground.normal_reject));
            }
        }

        protected void limitSpeed(double max) {
            if(Math.abs(velocity.x) > max) {
                velocity.x = Math.signum(velocity.x)*max;
            }
        }

        protected void updatePositionWithCollisions() {
            // Cap fall speed
            if(velocity.y > max_fall_speed) {
                velocity.y = max_fall_speed;
            }

            // Integrate velocity
            Vector2 delta_position = velocity.multiply(Game.stepTimeSeconds());

            // Move, colliding with objects
            ArrayList<Collider.Collision> collisions = collideWithObjects(delta_position);

            // Subtract velocity when colliding with things
            for(Collider.Collision c : collisions) {
                if(isGround(c.normal_reject)) {
                    velocity.y = 0;
                }
                else {
                    velocity = velocity.difference(velocity.projection(c.normal_reject));
                }
            }
        }

        protected void movement(double accel, double max_speed, Vector2 normal) {
            if(normal == null) {
                normal = up;
            }

            Vector2 vx = velocity.normalComponent(normal);
            Vector2 axis = normal.RHNormal().normalize();

            if(vx.abs() >= -max_speed) {
                if (InputManager.getDown(InputManager.K_LEFT)) {
                    velocity = velocity.difference(axis.multiply(accel*Game.stepTimeSeconds()));
                    direction_facing = Direction.LEFT;
                }
                limitSpeed(max_speed);
            }
            else {
                System.out.println(vx);
            }
            if(vx.abs() <= max_speed) {
                if (InputManager.getDown(InputManager.K_RIGHT)) {
                    velocity = velocity.sum(axis.multiply(accel*Game.stepTimeSeconds()));
                    direction_facing = Direction.RIGHT;
                }

                limitSpeed(max_speed);
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

        private final Sprite sprite;
        Vector2 last_normal;

        WalkState() {
            String[] sprite_files = {"./sprites/mario-walk-1.png", "./sprites/mario-walk-2.png"};
            sprite = new Sprite(sprite_files);
        }

        @Override
        void update() {
            ground = getGroundNormal();
            friction();
            movement(walk_accel, walk_xspeed, ground.normal_reject);
            if(ground.collision_found) {
                velocity = velocity.difference(velocity.projection(ground.normal_reject));
            }
            updatePositionWithCollisions();

            if(InputManager.getPressed(InputManager.K_JUMP)) {
                state_machine.changeState(States.JUMP);
            }
            if(!ground.collision_found) {
                state_machine.changeState(States.FALL);
            }
        }

        @Override
        void draw() {
            sprite.setFrameTime((int)(walk_xspeed - Math.abs(velocity.x)/2)/40);
            if(Math.abs(velocity.x) < walk_xspeed/10) {
                sprite.reset();
            }
           drawSprite(sprite.getCurrentFrame());
        }

        @Override
        void enter() {
            last_normal = new Vector2(0, -1);
            ground = getGroundNormal();

            // Normal points up and is within 45 degrees of vertical
            if(ground.collision_found) {
                velocity.y = 0;
                velocity = ground.normal_reject.RHNormal().normalize().multiply(velocity.x);
            }
        }

        @Override
        void exit() {

        }
    }

    private class RunState extends PlayerState {

        @Override
        void update() {
            ground = getGroundNormal();
            //gravity();
            friction();
            movement(run_accel, p_speed, null);
            updatePositionWithCollisions();

            if(InputManager.getPressed(InputManager.K_JUMP)) {
                state_machine.changeState(States.JUMP);
            }
            if(ground.normal_reject == null) {
                state_machine.changeState(States.FALL);
            }
        }

        @Override
        void draw() {

        }

        @Override
        void enter() {

        }

        @Override
        void exit() {

        }
    }

    private class JumpState extends PlayerState {

        private int timer;

        @Override
        void update() {
            movement(walk_accel, walk_xspeed, null);
            updatePositionWithCollisions();

            timer++;
            if(timer == max_jump_time || InputManager.getReleased(InputManager.K_JUMP) || velocity.y >= 0) {
                state_machine.changeState(States.WALK);
            }
        }

        @Override
        void draw() {
            drawSprite(jump_sprite);
        }

        @Override
        void enter() {
            velocity.y = jump_speed;
            timer = 0;
        }

        @Override
        void exit() {

        }
    }

    private class FallState extends PlayerState {

        @Override
        void update() {
            gravity();
            movement(walk_accel, walk_xspeed, null);

            updatePositionWithCollisions();
            if(touchingCollidable(down)) {
                state_machine.changeState(States.WALK);
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
}
