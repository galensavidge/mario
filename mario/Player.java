package mario;

import engine.*;
import engine.objects.*;
import engine.util.Vector2;

import java.awt.*;
import java.util.ArrayList;

/**
 * The physical object that the player controls.
 *
 * @author Galen Savidge
 * @version 4/24/2020
 */
public class Player extends PhysicsObject {
    public static final String type_name = "Player";

    private static final String sprite_file = "./sprites/mario-walk-1.png";
    private static final Image sprite = GameGraphics.getImage(sprite_file);

    private static final Vector2 gravity = new Vector2(0,300); // Pixels/s^2
    private static final double max_yspeed = 120;
    private static final double max_xspeed = 120;

    private static final double ground_acceleration = 300;
    private static final double air_acceleration = 150;
    private static final double friction = 100;

    public Player(double x, double y) {
        super(10, 10, x, y);
        collider = Collider.newBox(this,0,8,16,16);
        collider.active_check = true;
        this.type = type_name;
    }

    @Override
    protected boolean collideWith(PhysicsObject o) {
        return o.solid && position.y + 24 - Collider.edge_separation < o.position.y;
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
        double t = Game.stepTimeSeconds();

        boolean on_ground = false;
        if(touchingSolid(new Vector2(0, 1))){
            on_ground = true;
        }

        /* Acceleration */
        // Gravity
        velocity = velocity.add(gravity.multiply(t));

        // Friction
        if(on_ground) {
            double friction_delta = friction*t;
            if(Math.abs(velocity.x) > friction_delta) {
                velocity.x -= Math.signum(velocity.x) * friction_delta;
            }
            else {
                velocity.x = 0;
            }
        }

        // Input
        if(InputManager.getDown(InputManager.K_LEFT)) {
            if(on_ground) {
                velocity.x -= ground_acceleration*t;
            }
            else {
                velocity.x -= air_acceleration*t;
            }
        }
        if(InputManager.getDown(InputManager.K_RIGHT)) {
            if(on_ground) {
                velocity.x += ground_acceleration*t;
            }
            else {
                velocity.x += air_acceleration*t;
            }
        }
        if(InputManager.getDown(InputManager.K_UP)) {
            velocity.y -= ground_acceleration*t;
        }
        if(InputManager.getDown(InputManager.K_DOWN)) {
            velocity.y += ground_acceleration*t;
        }
        if(InputManager.getPressed(InputManager.K_JUMP)) {
            if(on_ground) {
                velocity.y -= 150;
            }
        }

        // Cap speed
        if(Math.abs(velocity.x) > max_xspeed) {
            velocity.x = max_xspeed*Math.signum(velocity.x);
        }
        if(velocity.y > max_yspeed) {
            velocity.y = max_yspeed;
        }

        Vector2 delta_position = velocity.multiply(t);

        ArrayList<Collider.Collision> collisions = collideWithObjects(delta_position);
        for(Collider.Collision c : collisions) {
            velocity = velocity.subtract(velocity.projection(c.normal));
        }
    }

    @Override
    public void draw() {
        GameGraphics.drawSprite((int)position.x, (int)position.y, false, sprite);
        collider.draw();
    }

    private enum StateMachine {
        STOPPED,
    }
}
