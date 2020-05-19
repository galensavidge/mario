package mario.objects;

import engine.graphics.GameGraphics;
import mario.Mario;

import java.awt.Image;
import java.util.HashMap;

/**
 * The base class for ground and other types of blocks.
 *
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class HardBlock extends Block {
    public static final String type_name = "HardBlock";

    private static final Image sprite = GameGraphics.getImage(Mario.sprite_path + "hardblock.png");

    public HardBlock(double x, double y) {
        super(x, y);
        this.type = HardBlock.type_name;
    }

    public HardBlock(HashMap<String, Object> args) {
        super(args);
        this.type = HardBlock.type_name;
    }

    public void draw() {
        if(isOnScreen(Mario.getGridScale(), Mario.getGridScale(), 0)) {
            GameGraphics.drawImage((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
        }
    }
}