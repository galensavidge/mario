import java.awt.*;

public class Block extends PhysicsObject{

    public Block(double x, double y) {
        super(0, 1, x, y);
        this.collider = new CircleCollider(this, 25, 0, 0);
    }

    public void update() {
        super.update();
        if(collider.checkCollision(0, 0, false) != null) {
            System.out.println("Collided with something!");
        }
    }

    public void draw() {
        GameGraphics.drawRectangle((int)Math.round(x), (int)Math.round(y), 50, 50, false,
                Color.BLUE);
    }
}