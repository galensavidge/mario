package mario.objects;

import engine.graphics.GameGraphics;
import engine.graphics.AnimatedSprite;
import engine.objects.Collider;
import mario.GameController;
import mario.Mario;

import java.util.HashMap;

/**
 * A single coin.
 *
 * @author Galen Savidge
 * @version 5/14/2020
 */
public class Coin extends Pickup {
    public static final String type_name = "Coin";
    private AnimatedSprite sprite;

    public Coin(double x, double y) {
        super(x, y);
        init();
    }

    public Coin(HashMap<String, Object> args) {
        super(args);
        init();
    }

    private void init() {
        this.type = Coin.type_name;

        collider = Collider.newPolygon(this, 8,
                Mario.getGridScale()/6.0, Mario.getGridScale()/6.0,
                Mario.getGridScale()/3.0, 0);

        this.suspend_tier = Mario.hitpause_suspend_tier;

        // Coin animation
        String[] sprite_files = {Mario.sprite_path + "coin-1.png", Mario.sprite_path + "coin-2.png",
                Mario.sprite_path + "coin-3.png", Mario.sprite_path + "coin-4.png"};
        sprite = new AnimatedSprite(sprite_files);
        sprite.setFrameTime(10);
    }

    @Override
    public void collect() {
        GameController.coins++;
        this.delete();
    }

    @Override
    public void update() {
        sprite.incrementFrame();
    }

    @Override
    public void draw() {
        if(isOnScreen(Mario.getGridScale(), Mario.getGridScale(), 0)) {
            GameGraphics.drawImage((int)position.x, (int)position.y, false, sprite.getCurrentFrame());
        }
    }
}
