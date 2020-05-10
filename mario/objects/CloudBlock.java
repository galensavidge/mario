package mario.objects;

import engine.GameGraphics;
import mario.Mario;

import java.awt.*;

public class CloudBlock extends Block {

    public static final String type_name = "CloudBlock";

    private static final Image sprite = GameGraphics.getImage(Mario.sprite_path+"cloudblock.png");

    public CloudBlock(double x, double y) {
        super(x, y);
        this.type = type_name;
        this.type_group = Types.semisolid_type_group;
        this.solid = false;
    }

    @Override
    public void draw() {
        if(isOnScreen()) {
            GameGraphics.drawImage((int) Math.round(position.x), (int) Math.round(position.y), false, sprite);
        }
    }
}
