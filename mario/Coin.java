package mario;

import engine.GameGraphics;
import engine.Sprite;
import engine.objects.Collider;
import engine.objects.PhysicsObject;

public class Coin extends PhysicsObject {
    public static final String type_name = "Coin";
    private Sprite sprite;

    public Coin(double x, double y) {
        super(0, 0, x, y);

        collider = Collider.newPolygon(this, 8, 0, 0,
                Mario.getGridScale()/2.0,0);
        assert collider != null;
        type = Coin.type_name;

        String[] sprite_files = {"./sprites/coin-1.png","./sprites/coin-2.png","./sprites/coin-3.png"};
        sprite = new Sprite(sprite_files);
        sprite.setFrameTime(10);
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)position.x, (int)position.y, false, sprite.getCurrentFrame());
    }
}
