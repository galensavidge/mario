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

    private static final String sprite_file = "./sprites/mario-walk-1.png";
    private static final Image sprite = GameGraphics.getImage(sprite_file);

    private static final Vector2 gravity = new Vector2(0,1200); // Pixels/s^2
    private static final double max_fall_speed = 480;
    private static final double friction = 400;

    private static final double walk_xspeed = 350;
    private static final double run_xspeed = 500;
    private static final double walk_accel = 800;
    private static final double run_accel = 1200;

    private static final double air_accel = 600;
    private static final double jump_speed = -480;
    private static final int max_jump_time = Mario.fps/2;

    private static final Vector2 down = new Vector2(0, Collider.edge_separation);

    /* Instance variables */

    private final PlayerStateMachine state_machine;

    private Vector2 ground_normal = null;


    public Player(double x, double y) {
        super(10, 10, x, y);
        collider = Collider.newBox(this,0,
                Mario.getGridSize()/2.0, Mario.getGridSize(), Mario.getGridSize());
        collider.active_check = true;
        this.type = type_name;
        state_machine = new PlayerStateMachine(States.WALK);
    }

    @Override
    protected boolean collideWith(PhysicsObject o) {
        return o.solid;
        //return o.solid && position.y + 24 - Collider.edge_separation < o.position.y;
    }

    @Override
    public void collisionEvent(PhysicsObject object) {
        switch(object.getType()) {
            case Coin.type_name:
                System.out.println("Collided with coin!");
                object.delete();
                break;
            default:
                break;
        }
    }

    @Override
    public void update() {
        state_machine.state.update();
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)position.x, (int)position.y, false, true, false,
                0, sprite);
        collider.draw();
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

        protected void gravity() {
            velocity = velocity.add(gravity.multiply(Game.stepTimeSeconds()));
        }

        protected Vector2 getGroundNormal() {
            collider.setPosition(position.add(down));
            ArrayList<Collision> collisions = collider.getCollisions();
            collider.setPosition(position);
            ArrayList<Collider> other_colliders = new ArrayList<>();
            for(Collision c : collisions) {
                if(c.collided_with != null) {
                    if (collideWith(c.collided_with)) {
                        other_colliders.add(c.collided_with.collider);
                    }
                }
            }

            Collision collision = collider.getNormal(down, other_colliders);
            if(collision != null) {
                return collision.normal;
            }
            else {
                return null;
            }
        }

        protected void friction() {
            // Friction
            double friction_delta = friction*Game.stepTimeSeconds();
            if(Math.abs(velocity.x) > friction_delta) {
                velocity.x -= Math.signum(velocity.x) * friction_delta;
            }
            else {
                velocity.x = 0;
            }
        }

        protected void updatePosition() {
            // Cap speed
            if(Math.abs(velocity.x) > walk_xspeed) {
                velocity.x = walk_xspeed *Math.signum(velocity.x);
            }
            if(velocity.y > max_fall_speed) {
                velocity.y = max_fall_speed;
            }

            // Integrate velocity
            Vector2 delta_position = velocity.multiply(Game.stepTimeSeconds());

            // Move, colliding with objects
            ArrayList<Collider.Collision> collisions = collideWithObjects(delta_position);

            // Subtract velocity when colliding with things
            boolean on_ground = false;
            for(Collider.Collision c : collisions) {
                velocity = velocity.subtract(velocity.projection(c.normal));
            }
        }

        protected void walk() {
            if(InputManager.getDown(InputManager.K_LEFT)) {
                velocity.x -= walk_accel * Game.stepTimeSeconds();
            }
            if(InputManager.getDown(InputManager.K_RIGHT)) {
                velocity.x += walk_accel * Game.stepTimeSeconds();
            }
        }

        protected void run() {
            if(InputManager.getDown(InputManager.K_LEFT)) {
                velocity.x -= run_accel * Game.stepTimeSeconds();
            }
            if(InputManager.getDown(InputManager.K_RIGHT)) {
                velocity.x += run_accel * Game.stepTimeSeconds();
            }
        }

        protected void airWalk() {
            if(InputManager.getDown(InputManager.K_LEFT)) {
                velocity.x -= air_accel * Game.stepTimeSeconds();
            }
            if(InputManager.getDown(InputManager.K_RIGHT)) {
                velocity.x += air_accel * Game.stepTimeSeconds();
            }
        }


        abstract void update();

        abstract void draw();

        abstract void enter();

        abstract void exit();
    }

    private class WalkState extends PlayerState {

        @Override
        void update() {
            Vector2 normal = getGroundNormal();
            gravity();
            friction();
            walk();

            if(InputManager.getPressed(InputManager.K_JUMP)) {
                state_machine.changeState(States.JUMP);
            }

            updatePosition();

            if(normal == null) {
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
            airWalk();
            updatePosition();

            timer++;
            if(timer == max_jump_time || InputManager.getReleased(InputManager.K_JUMP) || velocity.y >= 0) {
                state_machine.changeState(States.FALL);
            }
        }

        @Override
        void draw() {

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
            airWalk();

            updatePosition();
            if(touchingSolid(down)) {
                state_machine.changeState(States.WALK);
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
}
