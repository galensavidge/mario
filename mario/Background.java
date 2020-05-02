package mario;

import engine.GameGraphics;
import engine.objects.GameObject;

import java.awt.*;

public class Background extends GameObject {
    String sprite_name = "./sprites/bg-snow.png";
    Image sprite = GameGraphics.getImage(sprite_name);

    public Background() {
        super(0, -10);
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        GameGraphics.drawSprite(GameGraphics.camera_x/2, GameGraphics.camera_y/2, false, sprite);
    }
}
