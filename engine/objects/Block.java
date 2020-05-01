package engine.objects;

import engine.GameGraphics;

import java.awt.*;

public class Block extends PhysicsObject {
    public static final String type_name = "Block";

    private static final String sprite_file = "./sprites/hardblock.png";
    private static final Image sprite = Toolkit.getDefaultToolkit().createImage(sprite_file);

    public Block(double x, double y) {
        super(1, 1, x, y);
        this.collider = Collider.newBox(this,0,0,16,16);
        this.solid = true;
    }

    @Override
    public void collisionEvent(PhysicsObject object) {

    }

    public void update() {
        super.update();
    }

    public void draw() {
        GameGraphics.drawSprite((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
        collider.draw();
    }
}