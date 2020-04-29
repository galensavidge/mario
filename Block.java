import java.awt.*;
import java.util.ArrayList;

public class Block extends PhysicsObject {

    private static final String sprite_file = "./sprites/hardblock.png";
    private static final Image sprite = Toolkit.getDefaultToolkit().createImage(sprite_file);

    public Block(double x, double y) {
        super(1, 1, x, y);
        this.collider = PolygonCollider.newBox(this,0,0,16,16);
        this.solid = true;
    }

    public void update() {
        super.update();
        ArrayList<PhysicsObject> collisions = collider.getCollisions();
        if(collisions.size() > 0) {
            System.out.println("Block collided with thing at: ("+collisions.get(0).position.x+","
                    +collisions.get(0).position.y+")");
        }
    }

    public void draw() {
        GameGraphics.drawSprite((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
        collider.draw();
    }
}