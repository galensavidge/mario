import java.awt.*;

public class Block extends PhysicsObject {

    private static final String sprite_file = "./sprites/hardblock.png";
    private static final Image sprite = Toolkit.getDefaultToolkit().createImage(sprite_file);

    public Block(double x, double y) {
        super(1, 1, x, y);
        this.collider = new BoxCollider(this,16,16,0,0);
    }

    public void update() {
        super.update();
        if(collider.checkCollision(Vector2.zero(), false) != null) {
            System.out.println("Block collided with something!");
        }
    }

    public void draw() {
        GameGraphics.drawSprite((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
    }
}