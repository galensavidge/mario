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
        collider = PolygonCollider.newBox(this,2,8,12,16);
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();

        /* Check if on ground */
        boolean on_ground = false;

        /* Acceleration */
        // Gravity

        // Input
        if(InputManager.getDown(InputManager.K_LEFT)) {
            position.x -= 0.77;
        }
        if(InputManager.getDown(InputManager.K_RIGHT)) {
            position.x += 0.77;
        }
        if(InputManager.getDown(InputManager.K_UP)) {
            position.y -= 0.77;
        }
        if(InputManager.getDown(InputManager.K_DOWN)) {
            position.y += 0.77;
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

        /* Move based on velocity and handle collisions */
        //Vector2 delta_p = velocity.multiply(t);

        // Check for collisions at new point

        // Handle collisions

        // Update position
        /*if(delta_p != null) {
            position = position.add(delta_p);
        }*/

        collider.setPosition(position);
    }

    @Override
    public void draw() {
        GameGraphics.drawSprite((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
        collider.draw();
    }
}
