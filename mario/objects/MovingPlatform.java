package mario.objects;

import engine.Game;
import engine.GameGraphics;
import engine.objects.Collider;
import mario.Mario;

import java.awt.*;

/**
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class MovingPlatform extends WorldObject {
    public static final String type_name = "MovingPlatform";
    Image left_sprite = GameGraphics.getImage("./sprites/movingplatform-1.png");
    Image center_sprite = GameGraphics.getImage("./sprites/movingplatform-2.png");
    Image right_sprite = GameGraphics.getImage("./sprites/movingplatform-3.png");

    private static double speed = 200;

    private final int size;
    private final double initial_x;
    private final double farthest_position;

    /**
     * @param move_distance Number of grid squares to move before turning around.
     */
    public MovingPlatform(double x, double y, int size, int move_distance) {
        super(0, 5, x, y);
        this.type = type_name;
        this.size = Math.max(2, size);
        this.initial_x = position.x;
        this.farthest_position = initial_x + move_distance*Mario.getGridScale() - size*Mario.getGridScale();
        this.velocity.x = speed;

        collider = Collider.newBox(this, 0, 0, size*Mario.getGridScale(),
                Mario.getGridScale()*11.0/16.0);
    }

    @Override
    public void update() {
        position.x += velocity.x*Game.stepTimeSeconds();
        if(velocity.x > 0 && position.x > farthest_position) {
            position.x = farthest_position;
            velocity.x *= -1;
        }
        else if(velocity.x < 0 && position.x < initial_x) {
            position.x = initial_x;
            velocity.x *= -1;
        }

        collider.setPosition(position);
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)position.x, (int)position.y, false, left_sprite);
        GameGraphics.drawImage((int)(position.x + (size-1)*Mario.getGridScale()), (int)position.y,
                false, right_sprite);

        for(int i = 1;i < size-1;i++) {
            GameGraphics.drawImage((int)(position.x + i*Mario.getGridScale()), (int)position.y,
                    false, center_sprite);
        }
    }
}
