package mario.objects;

import engine.Game;
import engine.collider.Intersection;
import engine.graphics.GameGraphics;
import engine.collider.Collider;
import engine.objects.PhysicsObject;
import mario.Mario;

import java.awt.*;
import java.util.HashMap;

/**
 * @author Galen Savidge
 * @version 5/14/2020
 */
public class MovingPlatform extends PhysicsObject {
    public static final String type_name = "MovingPlatform";
    Image left_sprite = GameGraphics.getImage(Mario.sprite_path + "movingplatform-1.png");
    Image center_sprite = GameGraphics.getImage(Mario.sprite_path + "movingplatform-2.png");
    Image right_sprite = GameGraphics.getImage(Mario.sprite_path + "movingplatform-3.png");

    private static final double speed = 200;

    private int size;
    private double left_position;
    private double move_distance;
    private boolean moves_left;
    private double right_position;

    /**
     * @param move_distance Number of grid squares to move before turning around.
     */
    public MovingPlatform(double x, double y, int size, int move_distance) {
        super(Mario.gizmo_priority, Mario.gizmo_layer, x, y);
        this.move_distance = move_distance;
        this.size = size;
        init();
    }

    public MovingPlatform(HashMap<String, Object> args) {
        super(Mario.gizmo_priority, Mario.gizmo_layer, args);
        init();
    }

    @Override
    protected void parseArgs(HashMap<String, Object> args) {
        super.parseArgs(args);
        Object size = args.get("size");
        if(size != null) this.size = (int)(long)size;
        else this.size = 3;
        Object move_distance = args.get("move distance");
        if(move_distance != null) this.move_distance = (int)(long)move_distance;
        else this.move_distance = 6;
        Object moves_left = args.get("moves left");
        if(moves_left != null) this.moves_left = (boolean)moves_left;
        else this.moves_left = false;
    }

    private void init() {
        this.type = type_name;
        this.type_group = Types.semisolid_type_group;
        this.tags.add(Types.semisolid_tag);
        this.size = Math.max(2, this.size);
        double distance_px = move_distance*Mario.getGridScale() - size*Mario.getGridScale();
        if(moves_left) {
            this.left_position = getPosition().x - distance_px;
            this.right_position = getPosition().x;
            this.velocity.x = -speed;
        }
        else {
            this.left_position = getPosition().x;
            this.right_position = getPosition().x + distance_px;
            this.velocity.x = speed;
        }
        collider = Collider.newBox(this, 0, 0, size*Mario.getGridScale(), Mario.getGridScale()*11.0/16.0);
    }

    @Override
    public void prePhysicsUpdate() {
        if(getPosition().x == right_position) {
            velocity.x = -speed;
        }
        else if(getPosition().x == left_position) {
            velocity.x = speed;
        }

        double t = Game.stepTimeSeconds();
        if(getPosition().x > right_position) {
            velocity.x = (right_position - getPosition().x)/t;
            setPosition(right_position, getPosition().y);
        }
        else if(getPosition().x < left_position) {
            velocity.x = (left_position - getPosition().x)/t;
            setPosition(left_position, getPosition().y);
        }

        collider.setPosition(getPosition());
    }

    @Override
    protected boolean collidesWith(Intersection i) {
        return false;
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, left_sprite);
        GameGraphics.drawImage((int)(pixelPosition().x + (size - 1)*Mario.getGridScale()), (int)pixelPosition().y,
                false, right_sprite);

        for(int i = 1;i < size - 1;i++) {
            GameGraphics.drawImage((int)(pixelPosition().x + i*Mario.getGridScale()), (int)pixelPosition().y,
                    false, center_sprite);
        }
    }
}
