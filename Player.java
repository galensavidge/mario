import javax.swing.*;
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
        collider = new BoxCollider(this,12,16,2,8);
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();

        Vector2[] corners = ((BoxCollider)collider).getCorners(position);

        /* Check if on ground */
        boolean on_ground = false;
        ArrayList<PhysicsObject> collisions = collider.getCollisions(Vector2.zero(), false,true);
        for(PhysicsObject o : collisions) {
            if(o.solid && o.position.y == corners[2].y) {
                on_ground = true;
                break;
            }
        }

        /* Acceleration */
        // Gravity
        if(!on_ground) {
            velocity = velocity.add(gravity.multiply(t));
        }

        // Input
        if(InputManager.getDown(InputManager.K_LEFT)) {
            velocity.x -= 6;
        }
        if(InputManager.getDown(InputManager.K_RIGHT)) {
            velocity.x += 6;
        }
        if(InputManager.getPressed(InputManager.K_JUMP) && on_ground) {
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
        Vector2 delta_p = velocity.multiply(t);

        // Check for collisions at new point
        collisions = collider.getCollisions(delta_p,false,false);

        // Handle collisions
        PhysicsObject closest = null;
        if(collisions.size() > 0) {
            delta_p = null;

            // Find closest collision
            for (PhysicsObject o : collisions) {
                Vector2 to_contact = collider.vectorToContact(o.collider, velocity);
                if (to_contact != null) {
                    if (delta_p == null) {
                        delta_p = to_contact;
                        closest = o;
                    } else if (to_contact.abs() < delta_p.abs()) {
                        delta_p = to_contact;
                        closest = o;
                    }
                }
            }
        }

        // Update position
        if(delta_p != null) {
            position = position.add(delta_p);
        }

        // Get new corners
        corners = ((BoxCollider) collider).getCorners(position);

        // Change velocity depending on point of contact
        if(closest != null) {
            // Find point of contact with the thing we just collided with
            Vector2 contact = collider.pointOfContact(closest.collider);

            if(contact != null) {
                // Check if point is on a side edge
                if (contact.x == corners[0].x || contact.x == corners[2].x) {
                    velocity.x = 0;
                    System.out.println("x = 0");
                }

                // Check if point is on the top/bottom edge
                if (contact.y == corners[0].y || contact.y == corners[2].y) {
                    velocity.y = 0;
                    System.out.println("y = 0");
                }
            }
        }
    }

    @Override
    public void draw() {
        GameGraphics.drawSprite((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);

        Vector2[] corners = ((BoxCollider)collider).getCorners(position);
        GameGraphics.drawRectangle((int)Math.round(corners[0].x), (int)Math.round(corners[0].y),
                (int)Math.round(((BoxCollider) collider).width), (int)Math.round(((BoxCollider) collider).height),
                false,Color.BLUE);
    }
}
