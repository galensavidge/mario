package mario.objects;

import engine.GameGraphics;
import engine.Sprite;
import engine.objects.Collider;
import engine.objects.PhysicsObject;
import mario.Mario;

/**
 * A single coin.
 *
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class Coin extends WorldObject {
    public static final String type_name = "Coin";
    private final Sprite sprite;

    public Coin(double x, double y) {
        super(0, 0, x, y);
        this.type = Coin.type_name;
        this.type_group = Types.pickup_type_group;

        collider = Collider.newPolygon(this, 8,
                Mario.getGridScale()/6.0, Mario.getGridScale()/6.0,
                Mario.getGridScale()/3.0,0);

        // Coin animation
        String[] sprite_files = {"./sprites/coin-1.png","./sprites/coin-2.png","./sprites/coin-3.png"};
        sprite = new Sprite(sprite_files);
        sprite.setFrameTime(10);
    }

    @Override
    public void update() {
        sprite.incrementFrame();
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)position.x, (int)position.y, false, sprite.getCurrentFrame());
    }
}
