package mario;

import engine.GameGraphics;
import engine.objects.Collider;
import engine.objects.PhysicsObject;

import java.awt.*;

public class Block extends PhysicsObject {
    public static final String type_name = "Block";

    private static final String sprite_file = "./sprites/hardblock.png";
    private static final Image sprite = GameGraphics.getImage(sprite_file);

    public Block(double x, double y) {
        super(1, 1, x, y);
        this.collider = Collider.newBox(this,0,0,Mario.getGridScale(),Mario.getGridScale());
        this.solid = true;
        this.type = Block.type_name;
    }

    public void update() {
        super.update();
    }

    public void draw() {
        GameGraphics.drawImage((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
    }
}