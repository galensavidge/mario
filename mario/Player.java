package mario;

import engine.*;
import engine.objects.*;
import engine.util.Vector2;

import java.awt.*;
import java.util.ArrayList;

public class Player extends PhysicsObject {

    private static final String sprite_file = "./sprites/mario-stand-left.png";
    private static final Image sprite = Toolkit.getDefaultToolkit().createImage(sprite_file);

    private static final Vector2 gravity = new Vector2(0,150); // Pixels/s^2
    private static final double max_yspeed = 100;
    private static final double max_xspeed = 100;

    public Player(double x, double y) {
        super(10, 10, x, y);
        collider = Collider.newBox(this,0,8,16,16);
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();

        /* Check if on ground */
        boolean on_ground = false;

        /* Acceleration */
        // Gravity
        //velocity = velocity.add(gravity.multiply(t));

        // Input
        if(InputManager.getDown(InputManager.K_LEFT)) {
            velocity.x -= 2.13;
        }
        if(InputManager.getDown(InputManager.K_RIGHT)) {
            velocity.x += 2.13;
        }
        if(InputManager.getDown(InputManager.K_UP)) {
            velocity.y -= 2.13;
        }
        if(InputManager.getDown(InputManager.K_DOWN)) {
            velocity.y += 2.13;
        }
        if(InputManager.getPressed(InputManager.K_JUMP)) {
            velocity.y -= 150;
        }

        // Cap speed
        if(Math.abs(velocity.x) > max_xspeed) {
            velocity.x = max_xspeed*Math.signum(velocity.x);
        }
        if(velocity.y > max_yspeed) {
            velocity.y = max_yspeed;
        }

        Vector2 delta_position = velocity.multiply(t);

        ArrayList<Vector2> normals = collideWithSolids(delta_position);
        for(Vector2 n : normals) {
            velocity = velocity.subtract(velocity.projection(n));
        }
    }

    @Override
    public void draw() {
        GameGraphics.drawSprite((int)position.x, (int)position.y, false, sprite);
        collider.draw();
    }
}
