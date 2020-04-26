import java.awt.*;

public class Block extends GameObject{
    public CircleCollider collider = new CircleCollider(this, 25, 0, 0);

    public Block(double x, double y) {
        super(0, 1);

        this.x = x;
        this.y = y;
    }
    public void update() {
        if(collider.checkCollision(0, 0, false)) {
            System.out.println("Collided with something!");
        }
    }

    public void draw() {
        GameGraphics.drawRectangle((int)Math.round(x), (int)Math.round(y), 50, 50, false,
                Color.BLUE);
    }
}