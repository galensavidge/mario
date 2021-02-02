package mario.objects;

import engine.graphics.AnimatedSprite;
import engine.graphics.GameGraphics;
import mario.Mario;

import java.util.HashMap;

/**
 * A spike block.
 *
 * @author Galen Savidge
 * @version 6/8/2020
 */
public class Spike extends Block {

    public static final String type_name = "Spike";

    public static final String[] sprite_files = {Mario.sprite_path+"spike-1.png", Mario.sprite_path+"spike-2.png"};
    public static final AnimatedSprite sprite = new AnimatedSprite(sprite_files, Mario.hitpause_suspend_tier,
            Mario.fps/3);

    public Spike(double x, double y) {
        super(x, y);
        this.type = type_name;
        this.tags.add(Types.damage_tag);
    }

    public Spike(HashMap<String, Object> args) {
        super(args);
        this.type = type_name;
        this.tags.add(Types.damage_tag);
    }

    @Override
    public void draw() {
        if(isOnScreen(Mario.getGridScale(), Mario.getGridScale(), 0)) {
            GameGraphics.drawImage((int)pixelPosition().x, (int)pixelPosition().y, false, sprite.getCurrentFrame());
        }
    }
}
