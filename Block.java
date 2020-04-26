import java.awt.*;

public class Block extends GameObject{

    public Block(int x, int y) {
        super(0, 1);

        this.x = x;
        this.y = y;
    }
    public void update() {

    }

    public void draw() {
        GameGraphics.drawRectangle(x, y, 50, 50, false, Color.BLUE);
    }
}