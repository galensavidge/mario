package mario;

import engine.GameGraphics;
import engine.objects.GameObject;

import java.awt.*;

public class Background extends GameObject {
    private String image_name = "./sprites/bg-snow.png";
    private Image image = GameGraphics.getImage(image_name);
    private int width, height;
    private final int scale = 2;

    public Background() {
        super(0, -10);
        do {
            width = image.getWidth(null)*scale;
            height = image.getHeight(null)*scale;
        }
        while(width < 0 || height < 0);
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        int x = -GameGraphics.camera_x/2;
        do {
            int y = -GameGraphics.camera_y/2;
            do {
                GameGraphics.drawImage(x, y, true, false, false, scale, image);
                y += height;
            }
            while(y < GameGraphics.getWindowHeight());

            x += width;
        }
        while(x < GameGraphics.getWindowWidth());
    }
}
