package mario.objects;

import engine.Game;
import engine.GameGraphics;
import engine.objects.Collider;
import engine.objects.PhysicsObject;
import mario.Mario;

import java.awt.*;

/**
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class MovingPlatform extends PhysicsObject {
    public static final String type_name = "MovingPlatform";
    Image left_sprite = GameGraphics.getImage(Mario.sprite_path+"movingplatform-1.png");
    Image center_sprite = GameGraphics.getImage(Mario.sprite_path+"movingplatform-2.png");
    Image right_sprite = GameGraphics.getImage(Mario.sprite_path+"movingplatform-3.png");

    private static final double speed = 200;

    private final int size;
    private final double initial_x;
    private final double farthest_position;

    /**
     * @param move_distance Number of grid squares to move before turning around.
     */
    public MovingPlatform(double x, double y, int size, int move_distance) {
        super(Mario.gizmo_priority, Mario.gizmo_layer, x, y);
        this.type = type_name;
        this.type_group = Types.semisolid_type_group;
        this.size = Math.max(2, size);
        this.initial_x = position.x;
        this.farthest_position = initial_x + move_distance*Mario.getGridScale() - size*Mario.getGridScale();
        this.velocity.x = speed;

        collider = Collider.newBox(this, 0, 0, size*Mario.getGridScale(),
                Mario.getGridScale()*11.0/16.0);
    }

    @Override
    public void update() {
        double t = Game.stepTimeSeconds();
        if(position.x == farthest_position) {
            velocity.x = -speed;
        }
        else if(position.x == initial_x) {
            velocity.x = speed;
        }
        position.x += velocity.x*t;
        if(position.x > farthest_position) {
            velocity.x = (farthest_position - position.x)/t;
            position.x = farthest_position;
        }
        else if(position.x < initial_x) {
            velocity.x = (initial_x - position.x)/t;
            position.x = initial_x;
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
