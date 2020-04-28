import java.awt.*;

public class RoundBlock extends PhysicsObject {

    public RoundBlock(double x, double y) {
        super(1, 1, x, y);
        this.collider = new CircleCollider(this,8,0,0);
    }

    public void update() {
        super.update();
        if(collider.checkCollision(Vector2.zero(), false) != null) {
            System.out.println("Round block collided with something!");
        }
    }

    public void draw() {
        GameGraphics.drawCircle((int)Math.round(position.x), (int)Math.round(position.y), 8, false,
                Color.BLACK);
    }
}