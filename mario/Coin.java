package mario;

import engine.objects.Collider;
import engine.objects.PhysicsObject;

public class Coin extends PhysicsObject {
    public static final String type_name = "Coin";

    public Coin(double x, double y) {
        super(0, 10, x, y);

        collider = Collider.newPolygon(this, 8, 0, 0, 8,0);
        assert collider != null;
        collider.draw_self = true;
        type = Coin.type_name;
    }

    @Override
    public void draw() {

    }
}
