package mario.objects;

import engine.objects.Collider;
import engine.objects.PhysicsObject;
import engine.util.Vector2;
import mario.Mario;

/**
 * Slope object with configurable size.
 *
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class Slope extends PhysicsObject {
    public static final String type_name = "Slope";

    /**
     * Defaults to sloping from bottom left up to top right.
     *
     * @param width Width in grid squares.
     * @param height Height in grid squares.
     */
    public Slope(double x, double y, int width, int height, boolean flip_horizontal, boolean flip_vertical) {
        super(1, 1, x, y);
        double width_px = Mario.getGridScale()*width;
        double height_px = Mario.getGridScale()*height;
        Vector2[] vertices = {new Vector2(0, height_px), // Bottom left
                new Vector2(width_px, 0), // Top right
                new Vector2(width_px, height_px)}; // Bottom right
        if(flip_horizontal) {
            vertices[1].x = 0; // Move top right to top left

            if(flip_vertical) {
                vertices[2].y = 0; // Move bottom right to top right
            }
        }
        else if(flip_vertical) {
            vertices[0].y = 0; // Move bottom left to top left
        }
        this.collider = new Collider(this, vertices);
        this.collider.setPosition(position);
        this.collider.draw_self = true;
        this.solid = true;
        this.type = Slope.type_name;
    }

    @Override
    public void collisionEvent(PhysicsObject object) {

    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        collider.draw();
    }
}