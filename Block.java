import java.awt.*;

public class Block extends GameObject{
    public CircleCollider collider;

    public Block(int x, int y) {
        super(0, 1);

        this.x = x;
        this.y = y;

        this.collider = new CircleCollider(this, 25);
    }
    public void update() {
        if(collider.checkCollision(0, 0, false)) {
            System.out.println("Collided with something!");
        }
    }

    public void draw() {
        GameGraphics.drawRectangle(x, y, 50, 50, false, Color.BLUE);
    }
}