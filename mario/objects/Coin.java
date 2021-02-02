package mario.objects;

import engine.graphics.GameGraphics;
import engine.graphics.AnimatedSprite;
import engine.collider.Collider;
import mario.GameController;
import mario.Mario;

import java.util.HashMap;

/**
 * A single coin.
 *
 * @author Galen Savidge
 * @version 6/8/2020
 */
public class Coin extends Pickup {
    public static final String type_name = "Coin";

    /* Coin animation */
    private static final String[] sprite_files = {Mario.sprite_path + "coin-1.png", Mario.sprite_path + "coin-2.png",
            Mario.sprite_path + "coin-3.png", Mario.sprite_path + "coin-4.png"};
    private static final AnimatedSprite sprite = new AnimatedSprite(sprite_files, Mario.hitpause_suspend_tier,
                                                                 Mario.fps/6);

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
        collider = Collider.newPolygon(this, 8, Mario.getPixelSize()*4,
                Mario.getPixelSize()*4, Mario.getPixelSize()*4, 0);
    }

    @Override
    public void collect() {
        GameController.coins++;
        this.delete();
    }

    @Override
    public void draw() {
        if(isOnScreen(Mario.getGridScale(), Mario.getGridScale(), 0)) {
            GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, sprite.getCurrentFrame());
        }
    }
}
