import java.awt.*;

public class Block extends GameObject{

    public Block(int x, int y) {
        priority = 0;
        layer= 1;
        this.x = x;
        this.y = y;

        Game.addObject(this);
    }
    public void update() {

    }

    public void draw() {
        GameGraphics.drawRectangle(x, y, 50, 50, Color.BLUE);
    }
}
