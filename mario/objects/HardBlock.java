package mario.objects;

import engine.GameGraphics;

import java.awt.*;

/**
 * The base class for ground and other types of blocks.
 *
 * @author Galen Savidge
 * @version 5/5/2020
 */
public class HardBlock extends Block {
    public static final String type_name = "HardBlock";

    private static final String sprite_file = "./sprites/hardblock.png";
    private static final Image sprite = GameGraphics.getImage(sprite_file);

    public HardBlock(double x, double y) {
        super(x, y);
        this.type = HardBlock.type_name;
    }

    public void update() {
        super.update();
    }

    public void draw() {
        if(isOnscreen()) {
            GameGraphics.drawImage((int) Math.round(position.x), (int) Math.round(position.y), false, sprite);
        }
    }
}