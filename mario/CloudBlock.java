package mario;

import engine.GameGraphics;

import java.awt.*;

public class CloudBlock extends Block {

    public static final String type_name = "CloudBlock";

    private static final String sprite_file = "./sprites/cloudblock.png";
    private static final Image sprite = GameGraphics.getImage(sprite_file);

    public CloudBlock(double x, double y) {
        super(x, y);
        this.type = type_name;
        this.solid = false;
    }

    @Override
    public void draw() {
        GameGraphics.drawImage((int)Math.round(position.x), (int)Math.round(position.y), false, sprite);
    }
}
